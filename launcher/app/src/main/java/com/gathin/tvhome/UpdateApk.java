package com.gathin.tvhome;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import com.android.apksig.ApkVerifier;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipFile;

/** Cryptographic APK verification is performed before giving any URI to Package Installer. */
final class UpdateApk {
    static String hex(byte[] bytes) {
        StringBuilder out=new StringBuilder(bytes.length*2);
        for(byte b:bytes) out.append(String.format(Locale.ROOT,"%02x",b&255));
        return out.toString();
    }
    static String digest(File file) throws Exception {
        MessageDigest hash=MessageDigest.getInstance("SHA-256");
        try(InputStream in=new FileInputStream(file)) {
            byte[] buffer=new byte[32768]; int n;
            while((n=in.read(buffer))!=-1) hash.update(buffer,0,n);
        }
        return hex(hash.digest());
    }
    static String installedSigner(Context context) throws Exception {
        PackageInfo info=context.getPackageManager().getPackageInfo(context.getPackageName(),Build.VERSION.SDK_INT>=28?PackageManager.GET_SIGNING_CERTIFICATES:PackageManager.GET_SIGNATURES);
        android.content.pm.Signature[] signatures=Build.VERSION.SDK_INT>=28?info.signingInfo.getApkContentsSigners():info.signatures;
        if(signatures.length!=1) throw new SecurityException("Multiple installed signers");
        return hex(MessageDigest.getInstance("SHA-256").digest(signatures[0].toByteArray()));
    }
    static void verify(Context context,File file,UpdateRelease release) throws Exception {
        if(file.length()!=release.bytes || !digest(file).equals(release.sha256)) throw new SecurityException("SHA-256 mismatch");
        ApkVerifier.Result result=new ApkVerifier.Builder(file)
            .setMinCheckedPlatformVersion(Build.VERSION.SDK_INT).setMaxCheckedPlatformVersion(Build.VERSION.SDK_INT).build().verify();
        if(!result.isVerified() || result.getSignerCertificates().size()!=1) throw new SecurityException("Invalid APK signature");
        String signer=hex(MessageDigest.getInstance("SHA-256").digest(result.getSignerCertificates().get(0).getEncoded()));
        if(!signer.equals(release.signer) || !signer.equals(installedSigner(context))) throw new SecurityException("Incompatible signing key; migration required");
        PackageInfo info=context.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(),0);
        PackageInfo installed=context.getPackageManager().getPackageInfo(context.getPackageName(),0);
        if(info==null || !context.getPackageName().equals(info.packageName)
                || ((long)info.versionCode)!=release.code || !release.name.equals(info.versionName)
                || ((long)info.versionCode)<=((long)installed.versionCode)
                || info.applicationInfo.minSdkVersion>Build.VERSION.SDK_INT)
            throw new SecurityException("Incompatible APK identity/version/Android API");
        try(ZipFile zip=new ZipFile(file)) {
            Set<String> abis=new HashSet<>(); java.util.Enumeration<? extends java.util.zip.ZipEntry> entries=zip.entries();
            while(entries.hasMoreElements()) {
                String path=entries.nextElement().getName();
                if(path.startsWith("lib/") && path.endsWith(".so")) abis.add(path.split("/")[1]);
            }
            if(!abis.isEmpty() && Collections.disjoint(abis,Arrays.asList(Build.SUPPORTED_ABIS))) throw new SecurityException("Incompatible native ABI");
        }
    }
}
