package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.*;
import org.json.JSONArray;

/** Application-scoped bounded worker. No service, alarms, boot receiver or background polling. */
final class UpdateManager {
    static final int IDLE=0,CHECKING=1,DOWNLOADING=2,VERIFYING=3,READY=4,ERROR=5;
    interface Listener { void changed(); void ready(File file); }
    private static UpdateManager instance;
    static synchronized UpdateManager get(Context context) {
        if(instance==null) instance=new UpdateManager(context.getApplicationContext());
        return instance;
    }
    private final Context context;
    private final SharedPreferences prefs;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    volatile List<UpdateRelease> releases=Collections.emptyList();
    volatile int state=IDLE,percent=-1;
    volatile String error="";
    volatile File readyFile;
    volatile UpdateRelease selected;
    private volatile boolean cancelled,checking;
    private Listener listener;
    private UpdateManager(Context context) { this.context=context; prefs=context.getSharedPreferences("milo_updates",Context.MODE_PRIVATE); }
    void attach(Listener value) { listener=value; check(false); notifyChanged(); }
    void detach(Listener value) { if(listener==value) listener=null; cancelDownload(); }
    private void notifyChanged() { main.post(() -> { if(listener!=null) listener.changed(); }); }
    void cancelDownload() { if(state==DOWNLOADING || state==VERIFYING) cancelled=true; }
    void check(boolean force) {
        if(checking || state==DOWNLOADING || state==VERIFYING) return;
        checking=true;
        worker.execute(() -> {
            try {
                String certificate=UpdateApk.installedSigner(context);
                PackageInfo installed=context.getPackageManager().getPackageInfo(context.getPackageName(),0);
                long code=installed.versionCode,now=System.currentTimeMillis();
                String cache=prefs.getString("feed","");
                if(!cache.isEmpty()) releases=UpdateFeed.parse(cache,BuildConfig.UPDATE_REPO,context.getPackageName(),code,Build.VERSION.SDK_INT,certificate,BuildConfig.UPDATE_TRACK,BuildConfig.UPDATE_VARIANT);
                if(now<prefs.getLong("blocked_until",0)) return;
                if(!force && now>=prefs.getLong("success",0) && now-prefs.getLong("success",0)<86400000L) return;
                // Failed automatic checks back off for one hour. Manual checks cannot be spammed within a minute.
                long interval=force?60000L:3600000L;
                if(now>=prefs.getLong("attempt",0) && now-prefs.getLong("attempt",0)<interval) return;
                prefs.edit().putLong("attempt",now).apply(); state=CHECKING; notifyChanged();
                JSONArray combined=new JSONArray();
                for(int page=1;page<=3;page++) {
                    String address="https://api.github.com/repos/"+BuildConfig.UPDATE_REPO+"/releases?per_page=100&page="+page;
                    JSONArray part=new JSONArray(UpdateHttp.text(address));
                    for(int i=0;i<part.length();i++) combined.put(part.getJSONObject(i));
                    if(part.length()<100) break;
                }
                String feed=combined.toString();
                if(feed.length()>2*1024*1024) throw new IOException("Feed too large");
                releases=UpdateFeed.parse(feed,BuildConfig.UPDATE_REPO,context.getPackageName(),code,Build.VERSION.SDK_INT,certificate,BuildConfig.UPDATE_TRACK,BuildConfig.UPDATE_VARIANT);
                prefs.edit().putString("feed",feed).putLong("success",now).apply();
                error=""; state=readyFile!=null&&readyFile.isFile()?READY:IDLE;
            } catch(Exception failure) {
                if(failure instanceof UpdateHttp.RateLimit) prefs.edit().putLong("blocked_until",((UpdateHttp.RateLimit)failure).retryAt).apply();
                error=failure.getMessage()==null?"Update check failed":failure.getMessage(); state=ERROR;
            } finally { checking=false; notifyChanged(); }
        });
    }
    /** Called only after explicit download confirmation. Popup dismissal alone does not cancel it. */
    synchronized void download(UpdateRelease release) {
        if(state==DOWNLOADING || state==VERIFYING || checking) return;
        selected=release; cancelled=false; percent=0; error=""; state=DOWNLOADING; readyFile=null; notifyChanged();
        worker.execute(() -> {
            File directory=new File(context.getCacheDir(),"updates");
            File part=new File(directory,"download.part"),target=new File(directory,"update.apk");
            HttpURLConnection connection=null;
            try {
                if(!directory.isDirectory() && !directory.mkdirs()) throw new IOException("Cannot create update cache");
                if(directory.getUsableSpace()<release.bytes*2+10*1024*1024) throw new IOException("Not enough free storage");
                if(!UpdateRelease.githubAsset(release.url,BuildConfig.UPDATE_REPO)) throw new SecurityException("Unexpected update URL");
                connection=UpdateHttp.open(release.url);
                long contentLength=connection.getContentLengthLong();
                if(contentLength>=0 && contentLength!=release.bytes) throw new IOException("APK size changed");
                try(InputStream input=connection.getInputStream(); OutputStream output=new FileOutputStream(part)) {
                    UpdateTransfer.copy(input,output,release.bytes,() -> cancelled,value -> { percent=value; notifyChanged(); });
                }
                if(cancelled) throw new InterruptedIOException("Download cancelled");
                percent=100; state=VERIFYING; notifyChanged();
                UpdateApk.verify(context,part,release);
                if(cancelled) throw new InterruptedIOException("Download cancelled");
                if(target.exists() && !target.delete()) throw new IOException("Cannot replace cached update");
                if(!part.renameTo(target)) throw new IOException("Cannot save verified APK");
                readyFile=target; state=READY; notifyChanged();
                main.post(() -> { if(listener!=null && readyFile!=null) listener.ready(readyFile); });
            } catch(Exception failure) {
                part.delete(); error=failure.getMessage()==null?"Download failed":failure.getMessage(); state=ERROR; notifyChanged();
            } finally { if(connection!=null) connection.disconnect(); }
        });
    }
}
