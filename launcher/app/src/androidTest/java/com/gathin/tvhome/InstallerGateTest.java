package com.gathin.tvhome;
import android.content.*;
import android.net.Uri;
import android.provider.Settings;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.File;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/** Leaves Android's genuine settings/installer UI for the host's emulator UI test to confirm. */
@RunWith(AndroidJUnit4.class)
public class InstallerGateTest {
    @Rule public ActivityTestRule<MainActivity> rule=new ActivityTestRule<>(MainActivity.class,false,false);
    @Test public void openPermissionSettings() throws Exception {
        MainActivity activity=rule.launchActivity(new Intent(Intent.ACTION_MAIN));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> activity.startActivity(
            new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+activity.getPackageName()))));
        Thread.sleep(800);
    }
    @Test public void openVerifiedInstaller() throws Exception {
        MainActivity activity=rule.launchActivity(new Intent(Intent.ACTION_MAIN));
        assertTrue(activity.getPackageManager().canRequestPackageInstalls());
        long deadline=System.currentTimeMillis()+5000;
        while(!activity.hasWindowFocus() && System.currentTimeMillis()<deadline) Thread.sleep(40);
        File apk=new File(activity.getCacheDir(),"updates/update.apk");assertTrue(apk.isFile());
        assertTrue(activity.getSharedPreferences("milo_e2e",0).getBoolean("download_verified",false));
        String feed=activity.getSharedPreferences("milo_updates",0).getString("feed","");
        UpdateRelease release=UpdateFeed.parse(feed,BuildConfig.UPDATE_REPO,activity.getPackageName(),9,30,UpdateApk.installedSigner(activity),"test").get(0);
        UpdateApk.verify(activity,apk,release);
        Field field=MainActivity.class.getDeclaredField("updateUi");field.setAccessible(true);
        UpdateUi ui=(UpdateUi)field.get(activity);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> ui.ready(apk));
        Thread.sleep(800);
    }
}
