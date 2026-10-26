package com.gathin.tvhome;
import android.app.Activity;import android.content.*;import android.provider.Settings;import android.view.View;import android.widget.PopupWindow;
import androidx.test.ext.junit.runners.AndroidJUnit4;import androidx.test.platform.app.InstrumentationRegistry;import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;import androidx.test.runner.lifecycle.Stage;
import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class MenuReturnTest {
 @Rule public ActivityTestRule<MainActivity> rule=new ActivityTestRule<>(MainActivity.class,false,false);
 private final android.app.Instrumentation i=InstrumentationRegistry.getInstrumentation();
 private void main(Runnable r){i.runOnMainSync(r);i.waitForIdleSync();}
 private QuickSettingsActivity quick()throws Exception {
  final QuickSettingsActivity[] q={null};long end=System.currentTimeMillis()+3000;
  do {main(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))if(a instanceof QuickSettingsActivity)q[0]=(QuickSettingsActivity)a;});if(q[0]!=null)return q[0];Thread.sleep(40);}while(System.currentTimeMillis()<end);
  throw new AssertionError("Quick panel did not resume");
 }
 @Test public void panelReopensAfterAndroidSettings()throws Exception {
  MainActivity a=rule.launchActivity(new Intent(Intent.ACTION_MAIN));main(()->a.findViewById(R.id.btnSettings).performClick());QuickSettingsActivity first=quick();
  java.lang.reflect.Method launch=QuickSettingsActivity.class.getDeclaredMethod("launch",Intent.class);launch.setAccessible(true);
  main(()->{try{launch.invoke(first,SystemSettings.intent(first,Settings.ACTION_SETTINGS));}catch(Exception e){throw new RuntimeException(e);}});
  Thread.sleep(600);i.getTargetContext().startActivity(new Intent(i.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));i.waitForIdleSync();
  main(()->a.findViewById(R.id.btnSettings).performClick());QuickSettingsActivity next=quick();assertNotSame(first,next);main(next::finish);
 }
 @Test public void closingNetworkReturnsToItsAnchorAfterLibrary()throws Exception {
  Assume.assumeTrue(BuildConfig.BYEDPI_ENABLED);MainActivity a=rule.launchActivity(new Intent(Intent.ACTION_MAIN));
  java.lang.reflect.Field library=MainActivity.class.getDeclaredField("appLibrary");library.setAccessible(true);
  main(a::onLibraryRequested);Thread.sleep(200);main(()->{try{((AppLibraryDialog)library.get(a)).close();}catch(Exception e){throw new RuntimeException(e);}});Thread.sleep(200);
  View wifi=a.findViewById(R.id.btnWifi);main(()->wifi.requestFocus());
  java.lang.reflect.Method open=MainActivity.class.getDeclaredMethod("openNetworkPill");open.setAccessible(true);main(()->{try{open.invoke(a);}catch(Exception e){throw new RuntimeException(e);}});
  java.lang.reflect.Field popup=MainActivity.class.getDeclaredField("openPopup");popup.setAccessible(true);main(()->{try{((PopupWindow)popup.get(a)).dismiss();}catch(Exception e){throw new RuntimeException(e);}});Thread.sleep(200);main(()->assertTrue(wifi.hasFocus()));
 }
}
