package com.gathin.tvhome;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.android.apksig.ApkVerifier;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipFile;

/** User-initiated offline installer. Never uninstalls apps or grants VPN consent. */
final class DpiInstaller {
    static final int PERMISSION=2008, INSTALL=2009;
    private static final AtomicBoolean BUSY=new AtomicBoolean();
    static void start(Activity activity) {
        if(!BuildConfig.BYEDPI_ENABLED || !BUSY.compareAndSet(false,true)) return;
        Toast.makeText(activity,R.string.dpi_preparing,Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                try {
                    activity.getPackageManager().getPackageInfo(DpiClient.PACKAGE,0);
                    if(activity.getPackageManager().checkSignatures(activity.getPackageName(),DpiClient.PACKAGE)!=PackageManager.SIGNATURE_MATCH)
                        throw new SecurityException("Installed backend signer differs");
                } catch(PackageManager.NameNotFoundException absent) { /* Fresh installation. */ }
                File dir=new File(activity.getCacheDir(),"updates");
                if(!dir.isDirectory() && !dir.mkdirs()) throw new IOException("Cache unavailable");
                File file=new File(dir,"byedpi-paired.apk");
                try(InputStream in=activity.getAssets().open("byedpi/backend.apk");OutputStream out=new FileOutputStream(file)) {
                    byte[] buffer=new byte[32768];int n;long total=0;
                    while((n=in.read(buffer))!=-1) { total+=n;if(total>20L*1024*1024)throw new IOException("Backend too large");out.write(buffer,0,n); }
                }
                verify(activity,file);
                activity.runOnUiThread(() -> {
                    BUSY.set(false);
                    if(activity.isFinishing() || activity.isDestroyed()) return;
                    if(!activity.getPackageManager().canRequestPackageInstalls()) {
                        new AlertDialog.Builder(activity).setTitle(R.string.dpi_install_title)
                            .setMessage(R.string.dpi_allow_install).setNegativeButton(android.R.string.cancel,null)
                            .setPositiveButton(R.string.settings,(d,w) -> {
                                try { activity.startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:"+activity.getPackageName())),PERMISSION); }
                                catch(RuntimeException unavailable) { fail(activity); }
                            }).show();
                        return;
                    }
                    install(activity,file);
                });
            } catch(Exception failure) { BUSY.set(false); activity.runOnUiThread(() -> {
                if(!activity.isFinishing() && !activity.isDestroyed()) new AlertDialog.Builder(activity)
                    .setTitle(R.string.dpi_install_title).setMessage(R.string.dpi_install_failed)
                    .setPositiveButton(android.R.string.ok,null).show();
            }); }
        },"Milo-backend-installer").start();
    }
    static void cleanInstalled(Context context) {
        try {
            PackageInfo installed=context.getPackageManager().getPackageInfo(DpiClient.PACKAGE,0);
            if(installed.versionCode>=1821 && context.getPackageManager().checkSignatures(context.getPackageName(),DpiClient.PACKAGE)==PackageManager.SIGNATURE_MATCH)
                new File(context.getCacheDir(),"updates/byedpi-paired.apk").delete();
        } catch(PackageManager.NameNotFoundException absent) { /* Keep a staged installer if installation was cancelled. */ }
    }
    static void verify(Context activity,File file) throws Exception {
        ApkVerifier.Result verified=new ApkVerifier.Builder(file).setMinCheckedPlatformVersion(Build.VERSION.SDK_INT)
            .setMaxCheckedPlatformVersion(Build.VERSION.SDK_INT).build().verify();
        if(!verified.isVerified() || verified.getSignerCertificates().size()!=1) throw new SecurityException("Invalid backend signature");
        String signer=UpdateApk.hex(MessageDigest.getInstance("SHA-256").digest(verified.getSignerCertificates().get(0).getEncoded()));
        if(!signer.equals(UpdateApk.installedSigner(activity))) throw new SecurityException("Wrong paired signer");
        PackageInfo info=activity.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(),0);
        if(info==null || !DpiClient.PACKAGE.equals(info.packageName) || info.versionCode!=1821
                || info.applicationInfo.minSdkVersion>Build.VERSION.SDK_INT
                || (info.applicationInfo.flags&ApplicationInfo.FLAG_DEBUGGABLE)!=0) throw new SecurityException("Wrong backend");
        Set<String> abis=new HashSet<>();
        try(ZipFile zip=new ZipFile(file)) {
            Enumeration<? extends java.util.zip.ZipEntry> entries=zip.entries();
            while(entries.hasMoreElements()) { String name=entries.nextElement().getName();
                if(name.startsWith("lib/") && name.endsWith(".so")) abis.add(name.split("/")[1]); }
        }
        if(abis.isEmpty() || Collections.disjoint(abis,Arrays.asList(Build.SUPPORTED_ABIS))) throw new SecurityException("Unsupported backend ABI");
    }
    private static void install(Activity activity,File file) {
        Uri uri=FileProvider.getUriForFile(activity,activity.getPackageName()+".updates",file);
        Intent intent=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData.newRawUri("ByeByeDPI installation",uri));
        for(ResolveInfo handler:activity.getPackageManager().queryIntentActivities(intent,0)) {
            if((handler.activityInfo.applicationInfo.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))==0)continue;
            intent.setComponent(new ComponentName(handler.activityInfo.packageName,handler.activityInfo.name));
            try { activity.startActivityForResult(intent,INSTALL);return; } catch(ActivityNotFoundException ignored) { }
        }
        fail(activity);
    }
    private static void fail(Activity activity) { Toast.makeText(activity,R.string.update_installer_missing,Toast.LENGTH_LONG).show(); }
}
