package org.milohome.devicechecks;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.test.InstrumentationTestCase;
import java.io.*;

/** Independent framework-only runner: does not share dependencies with a shrinking target APK. */
public class UpdateChecks extends InstrumentationTestCase {
    public void testSeedOptimizationUpgrade() throws Exception {
        Context c=getInstrumentation().getTargetContext();
        c.getSharedPreferences("launcher_settings",0).edit().putInt("columns",6).putInt("dock_opacity",70)
            .putBoolean("animations",false).putInt("milo_update_mode",0).putBoolean("milo_clock_bold",true).commit();
        c.getSharedPreferences("milo_app_names",0).edit().putString("org.milohome.demo.media","My media").commit();
        c.getSharedPreferences("milo_library",0).edit().putString("selected","org.milohome.demo.media").putInt("offset",24).putBoolean("show_all",true).commit();
        Intent home=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo r=c.getPackageManager().resolveActivity(home,PackageManager.MATCH_DEFAULT_ONLY);
        c.getSharedPreferences("upgrade_probe",0).edit().putString("home",r==null?"":r.activityInfo.packageName+"/"+r.activityInfo.name).commit();
    }
    public void testVerifyOptimizationUpgrade() throws Exception {
        Context c=getInstrumentation().getTargetContext();android.content.SharedPreferences p=c.getSharedPreferences("launcher_settings",0);
        assertEquals(6,p.getInt("columns",0));assertEquals(70,p.getInt("dock_opacity",0));assertFalse(p.getBoolean("animations",true));assertEquals(0,p.getInt("milo_update_mode",-1));assertTrue(p.getBoolean("milo_clock_bold",false));
        assertEquals("My media",c.getSharedPreferences("milo_app_names",0).getString("org.milohome.demo.media",""));
        assertEquals(24,c.getSharedPreferences("milo_library",0).getInt("offset",0));assertTrue(c.getSharedPreferences("milo_library",0).getBoolean("show_all",false));
        ResolveInfo r=c.getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),PackageManager.MATCH_DEFAULT_ONLY);
        assertEquals(c.getSharedPreferences("upgrade_probe",0).getString("home",""),r==null?"":r.activityInfo.packageName+"/"+r.activityInfo.name);
    }
    public void testConfigureShowcase() throws Exception {
        Context c=getInstrumentation().getTargetContext();
        File dir=c.getExternalFilesDir(null);assertNotNull(dir);dir.mkdirs();
        String apps="[\"com.google.android.youtube.tv\",\"com.android.vending\",\"com.mediatek.wwtv.mediaplayer\"]";
        String json="{\"dock_apps\":"+apps+",\"library_apps\":[],\"hidden_apps\":[\"com.android.traceur\"],\"columns\":3,\"fill_dock\":false}";
        try(OutputStream out=new FileOutputStream(new File(dir,"config.json"))){out.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
        c.getSharedPreferences("launcher_settings",0).edit().clear().commit();
        c.getSharedPreferences("milo_updates",0).edit().clear().commit();
    }
    public void testEditionManifest() throws Exception {
        Context c=getInstrumentation().getTargetContext();
        PackageInfo p=c.getPackageManager().getPackageInfo(c.getPackageName(),PackageManager.GET_META_DATA|PackageManager.GET_PERMISSIONS);
        String edition=p.applicationInfo.metaData.getString("com.milohome.variant");
        assertTrue("standard".equals(edition)||"byedpi".equals(edition));
        boolean control=java.util.Arrays.asList(p.requestedPermissions).contains("io.github.romanvht.byedpi.permission.TV_CONTROL");
        assertEquals("byedpi".equals(edition),control);
        assertNotNull(c.getPackageManager().getApplicationIcon(c.getPackageName()));
        android.os.Bundle result=new android.os.Bundle();result.putString("installedEdition",edition);result.putInt("installedVersionCode",p.versionCode);getInstrumentation().sendStatus(0,result);
    }
    public void testSeedEditionUpdateData() throws Exception {
        Context c=getInstrumentation().getTargetContext();
        c.getSharedPreferences("milo_e2e",0).edit().putString("edition_sentinel","preserve-edition-update").commit();
    }
    public void testEditionUpdateData() throws Exception {
        testEditionManifest();Context c=getInstrumentation().getTargetContext();
        assertEquals(103,c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionCode);
        assertEquals("preserve-edition-update",c.getSharedPreferences("milo_e2e",0).getString("edition_sentinel",""));
        ResolveInfo home=c.getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),PackageManager.MATCH_DEFAULT_ONLY);
        assertEquals(c.getPackageName(),home.activityInfo.packageName);
    }
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
    public void testDownloadedRc() throws Exception {
        Context c=getInstrumentation().getTargetContext();PackageManager pm=c.getPackageManager();
        assertEquals(10,pm.getPackageInfo(c.getPackageName(),0).versionCode);
        File apk=new File(c.getCacheDir(),"updates/update.apk");assertTrue(apk.isFile());
        PackageInfo candidate=pm.getPackageArchiveInfo(apk.getAbsolutePath(),PackageManager.GET_SIGNING_CERTIFICATES);
        assertNotNull(candidate);assertEquals(c.getPackageName(),candidate.packageName);assertEquals(11,candidate.versionCode);
        assertEquals(pm.getPackageInfo(c.getPackageName(),PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.getApkContentsSigners()[0],candidate.signingInfo.getApkContentsSigners()[0]);
        java.security.MessageDigest digest=java.security.MessageDigest.getInstance("SHA-256");
        try(InputStream in=new FileInputStream(apk)){byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)digest.update(b,0,n);}
        StringBuilder hex=new StringBuilder();for(byte b:digest.digest())hex.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
        android.os.Bundle result=new android.os.Bundle();result.putString("cachedRcSha256",hex.toString());getInstrumentation().sendStatus(0,result);
    }
}
