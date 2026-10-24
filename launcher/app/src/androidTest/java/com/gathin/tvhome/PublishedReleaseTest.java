package com.gathin.tvhome;

import android.app.*;
import android.content.*;
import android.view.View;
import android.widget.LinearLayout;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/** Opt-in online test: requires genuine same-key test Stable10/RC11 Releases, never injects metadata or transport. */
@RunWith(AndroidJUnit4.class)
public class PublishedReleaseTest {
    @Rule public ActivityTestRule<MainActivity> rule=new ActivityTestRule<>(MainActivity.class,false,false);
    private final Instrumentation instrument=InstrumentationRegistry.getInstrumentation();
    private final Context context=instrument.getTargetContext();
    private void main(Runnable run) { instrument.runOnMainSync(run); instrument.waitForIdleSync(); }
    private Object field(Object target,String name) throws Exception {
        Field f=target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target);
    }
    @Test public void discoverBothChannelsAndDownloadStableFromGitHub() throws Exception {
        Assume.assumeTrue("Explicit online opt-in required", "true".equals(InstrumentationRegistry.getArguments().getString("publishedReleases")));
        assertEquals("test",BuildConfig.UPDATE_TRACK);
        assertEquals(9,context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode);
        context.getSharedPreferences("milo_updates",0).edit().clear().commit();
        context.getSharedPreferences("milo_e2e",0).edit().putString("sentinel","preserve-through-stable-and-rc").commit();
        context.getSharedPreferences("launcher_settings",0).edit().putInt("columns",5).commit();
        MainActivity activity=rule.launchActivity(new Intent(Intent.ACTION_MAIN));
        UpdateManager manager=UpdateManager.get(context);
        long deadline=System.currentTimeMillis()+45000;
        while(manager.releases.size()!=2 && manager.state!=UpdateManager.ERROR && System.currentTimeMillis()<deadline) Thread.sleep(50);
        assertEquals(manager.error,2,manager.releases.size());
        boolean stable=false,rc=false;
        for(UpdateRelease release:manager.releases) {
            assertTrue(release.url.startsWith("https://github.com/"+BuildConfig.UPDATE_REPO+"/releases/download/"));
            if(release.channel.equals("stable")) { assertEquals(10,release.code);stable=true; }
            if(release.channel.equals("rc")) { assertEquals(11,release.code);rc=true; }
        }
        assertTrue(stable && rc);
        main(() -> assertEquals(View.VISIBLE,activity.findViewById(R.id.btnUpdate).getVisibility()));
        main(() -> activity.findViewById(R.id.btnUpdate).performClick());
        UpdateUi ui=(UpdateUi)field(activity,"updateUi");
        PopupWindowAccess.selectStable(instrument,field(ui,"popup"));
        AlertDialog dialog=(AlertDialog)field(ui,"confirmation"); assertNotNull(dialog);
        main(() -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick());
        deadline=System.currentTimeMillis()+90000;
        while(manager.state!=UpdateManager.READY && manager.state!=UpdateManager.ERROR && System.currentTimeMillis()<deadline) Thread.sleep(50);
        assertEquals(manager.error,UpdateManager.READY,manager.state);
        assertEquals(100,manager.percent); assertEquals(10,manager.selected.code);
        assertEquals(manager.selected.sha256,UpdateApk.digest(manager.readyFile));
        context.getSharedPreferences("milo_e2e",0).edit().putBoolean("download_verified",true).commit();
    }
    private static class PopupWindowAccess {
        static void selectStable(Instrumentation i,Object popup) {
            LinearLayout rows=((android.widget.PopupWindow)popup).getContentView().findViewById(R.id.rows);
            i.runOnMainSync(() -> rows.getChildAt(1).performClick()); i.waitForIdleSync();
        }
    }
}
