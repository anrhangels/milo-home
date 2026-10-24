package com.gathin.tvhome;
import android.content.*;
import android.net.Uri;
import androidx.core.content.FileProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;
import static org.junit.Assert.*;

/** A local RC installer fixture, not a claim that an unpublished GitHub asset was downloaded. */
@RunWith(AndroidJUnit4.class)
public class StageRcInstallerTest {
    @Rule public ActivityTestRule<MainActivity> rule=new ActivityTestRule<>(MainActivity.class,false,false);
    @Test public void stageVerifiedRcAndOpenSystemInstaller() throws Exception {
        MainActivity activity=rule.launchActivity(new Intent(Intent.ACTION_MAIN));
        assertEquals(10,activity.getPackageManager().getPackageInfo(activity.getPackageName(),0).versionCode);
        assertTrue(activity.getPackageManager().canRequestPackageInstalls());
        File directory=new File(activity.getCacheDir(),"updates");directory.mkdirs();File apk=new File(directory,"rc.apk");
        try(InputStream input=InstrumentationRegistry.getInstrumentation().getContext().getAssets().open("MiloHome-rc.apk");OutputStream out=new FileOutputStream(apk)) {
            byte[] buffer=new byte[32768];int n;while((n=input.read(buffer))!=-1)out.write(buffer,0,n);
        }
        android.content.pm.PackageInfo candidate=activity.getPackageManager().getPackageArchiveInfo(apk.getAbsolutePath(),android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES);
        assertNotNull(candidate);assertEquals(activity.getPackageName(),candidate.packageName);assertEquals(11,candidate.versionCode);
        android.content.pm.PackageInfo current=activity.getPackageManager().getPackageInfo(activity.getPackageName(),android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES);
        assertArrayEquals(current.signingInfo.getApkContentsSigners(),candidate.signingInfo.getApkContentsSigners());
        Uri uri=FileProvider.getUriForFile(activity,activity.getPackageName()+".updates",apk);
        Intent intent=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData.newRawUri("RC fixture",uri));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> activity.startActivity(intent));Thread.sleep(800);
    }
}
