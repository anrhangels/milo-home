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

/** Application-scoped bounded worker. Optional system-scheduled checks; no permanent service, boot receiver or tight polling. */
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
    private final ThreadPoolExecutor worker=new ThreadPoolExecutor(1,1,15L,TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(2),task -> new Thread(task,"Milo-updates"));
    private final Handler main=new Handler(Looper.getMainLooper());
    volatile List<UpdateRelease> releases=Collections.emptyList();
    volatile int state=IDLE,percent=-1;
    volatile String error="";
    volatile File readyFile;
    volatile UpdateRelease selected;
    private volatile boolean cancelled,checking;
    private Listener listener;
    private UpdateManager(Context context) { this.context=context; prefs=context.getSharedPreferences("milo_updates",Context.MODE_PRIVATE); worker.allowCoreThreadTimeOut(true); }
    boolean isChecking() { return checking; }
    void attach(Listener value) { listener=value; UpdateSchedule.sync(context); check(false); notifyChanged(); }
    void detach(Listener value) { if(listener==value) listener=null; cancelDownload(); }
    private void notifyChanged() { main.post(() -> { if(listener!=null) listener.changed(); }); }
    void cancelDownload() { if(state==DOWNLOADING || state==VERIFYING) cancelled=true; }
    void check(boolean force) { check(force,null); }
    synchronized void check(boolean force,Runnable completion) {
        if(checking || state==DOWNLOADING || state==VERIFYING) {
            if(completion!=null) main.post(completion);
            return;
        }
        checking=true;
        worker.execute(() -> {
            try {
                String certificate=UpdateApk.installedSigner(context);
                PackageInfo installed=context.getPackageManager().getPackageInfo(context.getPackageName(),0);
                long code=installed.versionCode,now=System.currentTimeMillis();
                if(prefs.getLong("partial_code",0)<=code || System.currentTimeMillis()-prefs.getLong("partial_at",0)>7L*86400000L)clearPartial();
                UpdateCache.clean(new File(context.getCacheDir(),"updates"),code,file -> {
                    PackageInfo archive=context.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(),0);
                    return archive!=null && context.getPackageName().equals(archive.packageName)?archive.versionCode:-1;
                },hasPartial());
                String cache=prefs.getString("feed","");
                if(!cache.isEmpty()) releases=UpdateFeed.parse(cache,BuildConfig.UPDATE_REPO,context.getPackageName(),code,Build.VERSION.SDK_INT,certificate,BuildConfig.UPDATE_TRACK,BuildConfig.UPDATE_VARIANT);
                restoreVerifiedUpdate();
                if(!UpdatePolicy.due(UpdateSchedule.mode(context),force,now,prefs.getLong("success",0),
                        prefs.getLong("attempt",0),prefs.getLong("blocked_until",0))) return;
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
                error=failure.getMessage()==null?"Update check failed":failure.getMessage(); state=readyFile!=null && readyFile.isFile()?READY:ERROR;
            } finally { checking=false; notifyChanged(); if(completion!=null) main.post(completion); }
        });
    }
    private void restoreVerifiedUpdate(){
        if(readyFile!=null && readyFile.isFile()){state=READY;return;}
        File target=new File(context.getCacheDir(),"updates/update.apk");if(!target.isFile())return;
        for(UpdateRelease release:releases){
            if(target.length()!=release.bytes)continue;
            try{UpdateApk.verify(context,target,release);selected=release;readyFile=target;state=READY;return;}
            catch(Exception invalid){/* Try the other channel; never offer an unverified cached APK. */}
        }
    }
    boolean hasPartial(){return !prefs.getString("partial_identity","").isEmpty() && new File(context.getCacheDir(),"updates/download.part").isFile() && new File(context.getCacheDir(),"updates/download.part").length()>0;}
    private void clearPartial(){prefs.edit().remove("partial_identity").remove("partial_etag").remove("partial_at").remove("partial_code").apply();}
    private static final class StorageFailure extends IOException{StorageFailure(String message){super(message);}}
    static long requiredSpace(long bytes,long already){return bytes*2-Math.min(bytes,Math.max(0,already))+10L*1024*1024;}
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
                String identity=ResumeDownload.identity(release);
                long offset=identity.equals(prefs.getString("partial_identity","")) && part.isFile()?part.length():0;
                if(offset<0 || offset>release.bytes)offset=0;
                long needed=requiredSpace(release.bytes,offset),available=directory.getUsableSpace();
                if(available<needed)throw new StorageFailure(context.getString(R.string.opt_storage,
                    android.text.format.Formatter.formatShortFileSize(context,needed),android.text.format.Formatter.formatShortFileSize(context,available)));
                if(offset==0 && part.exists() && !part.delete())throw new IOException("Cannot reset partial APK");
                prefs.edit().putString("partial_identity",identity).putLong("partial_code",release.code).putLong("partial_at",System.currentTimeMillis()).apply();
                if(!UpdateRelease.githubAsset(release.url,BuildConfig.UPDATE_REPO)) throw new SecurityException("Unexpected update URL");
                if(offset<release.bytes){
                    connection=UpdateHttp.open(release.url,offset,prefs.getString("partial_etag",""));
                    long start=ResumeDownload.start(connection.getResponseCode(),offset,connection.getHeaderField("Content-Range"),release.bytes,connection.getContentLengthLong());
                    String etag=connection.getHeaderField("ETag");prefs.edit().putString("partial_etag",etag==null?"":etag).apply();
                    percent=(int)(100*start/release.bytes);notifyChanged();
                    try(InputStream input=connection.getInputStream(); OutputStream output=new FileOutputStream(part,start>0)) {
                        UpdateTransfer.copy(input,output,release.bytes,start,() -> cancelled,value -> { percent=value; notifyChanged(); });
                    }
                }
                if(cancelled) throw new InterruptedIOException("Download cancelled");
                percent=100; state=VERIFYING; notifyChanged();
                UpdateApk.verify(context,part,release);
                if(cancelled) throw new InterruptedIOException("Download cancelled");
                if(target.exists() && !target.delete()) throw new IOException("Cannot replace cached update");
                if(!part.renameTo(target)) throw new IOException("Cannot save verified APK");
                clearPartial();
                readyFile=target; state=READY; notifyChanged();
                main.post(() -> { if(listener!=null && readyFile!=null) listener.ready(readyFile); });
            } catch(Exception failure) {
                if(failure instanceof SecurityException || failure instanceof ResumeDownload.RangeFailure || state==VERIFYING){part.delete();clearPartial();}
                error=failure instanceof StorageFailure?failure.getMessage():hasPartial()?context.getString(R.string.opt_interrupted):(failure.getMessage()==null?"Download failed":failure.getMessage());
                state=ERROR; notifyChanged();
            } finally { if(connection!=null) connection.disconnect(); }
        });
    }
}
