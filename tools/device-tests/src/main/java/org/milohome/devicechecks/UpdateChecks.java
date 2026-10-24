package org.milohome.devicechecks;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import java.io.*;

/** Independent framework-only runner: does not share dependencies with a shrinking target APK. */
public class UpdateChecks extends InstrumentationTestCase {
    public void testPersistentData() throws Exception {
        Context c=getInstrumentation().getTargetContext();
        int code=c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionCode;
        assertTrue("Expected an updated fixture",code==10 || code==11 || code==100);
        assertEquals("preserve-through-stable-and-rc",c.getSharedPreferences("milo_e2e",0).getString("sentinel",""));
        assertTrue(c.getSharedPreferences("milo_e2e",0).getBoolean("download_verified",false));
        assertEquals(5,c.getSharedPreferences("launcher_settings",0).getInt("columns",0));
        ResolveInfo home=c.getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),PackageManager.MATCH_DEFAULT_ONLY);
        assertNotNull(home); assertEquals(c.getPackageName(),home.activityInfo.packageName);
    }
    public void testStageRc() throws Exception {
        Context c=getInstrumentation().getTargetContext();
        assertEquals(10,c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionCode);
        File dir=new File(c.getCacheDir(),"updates"); dir.mkdirs(); File apk=new File(dir,"rc.apk");
        try(InputStream in=getInstrumentation().getContext().getAssets().open("MiloHome-rc.apk"); OutputStream out=new FileOutputStream(apk)) {
            byte[] b=new byte[32768]; int n; while((n=in.read(b))!=-1) out.write(b,0,n);
        }
        PackageManager pm=c.getPackageManager();
        PackageInfo candidate=pm.getPackageArchiveInfo(apk.getAbsolutePath(),PackageManager.GET_SIGNING_CERTIFICATES);
        assertNotNull(candidate); assertEquals(c.getPackageName(),candidate.packageName); assertEquals(11,candidate.versionCode);
        PackageInfo current=pm.getPackageInfo(c.getPackageName(),PackageManager.GET_SIGNING_CERTIFICATES);
        assertEquals(current.signingInfo.getApkContentsSigners()[0],candidate.signingInfo.getApkContentsSigners()[0]);
        Activity activity=getInstrumentation().startActivitySync(new Intent(Intent.ACTION_MAIN).setClassName(c.getPackageName(),"com.gathin.tvhome.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        Uri uri=Uri.parse("content://"+c.getPackageName()+".updates/updates/rc.apk");
        Intent intent=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData.newRawUri("Local RC fixture",uri));
        getInstrumentation().runOnMainSync(() -> activity.startActivity(intent)); Thread.sleep(1000);
    }
    public void testRotatedPair() throws Exception {
        testPersistentData();
        Context c=getInstrumentation().getTargetContext(); PackageManager pm=c.getPackageManager();
        PackageInfo p=pm.getPackageInfo(c.getPackageName(),PackageManager.GET_SIGNING_CERTIFICATES);
        assertEquals(100,p.versionCode); assertTrue(p.signingInfo.hasPastSigningCertificates());
        byte[] digest=java.security.MessageDigest.getInstance("SHA-256").digest(p.signingInfo.getApkContentsSigners()[0].toByteArray());
        StringBuilder hex=new StringBuilder();for(byte b:digest)hex.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
        assertEquals("0eb3e4397a0f1f2c05925c47da6bebeea0e757c41967642af051c774d72b36db",hex.toString());
        assertEquals(PackageManager.SIGNATURE_MATCH,pm.checkSignatures(c.getPackageName(),"io.github.romanvht.byedpi"));
        assertEquals(PackageManager.PERMISSION_GRANTED,pm.checkPermission("io.github.romanvht.byedpi.permission.TV_CONTROL",c.getPackageName()));
    }
}
