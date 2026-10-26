package com.gathin.tvhome;
import android.content.*;import android.view.KeyEvent;import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;import androidx.test.platform.app.InstrumentationRegistry;import androidx.test.rule.ActivityTestRule;
import org.junit.*;import org.junit.runner.RunWith;import java.lang.reflect.Field;import java.util.Set;import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class OptimizationLifecycleTest {
 @Rule public ActivityTestRule<MainActivity> rule=new ActivityTestRule<>(MainActivity.class,false,false);
 private final android.app.Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
 private void main(Runnable r){instrumentation.runOnMainSync(r);instrumentation.waitForIdleSync();}
 @Test public void coveringHomeUnsubscribesMinuteWorkAndOpacityDoesNotRecreate()throws Exception{
  Context c=instrumentation.getTargetContext();c.getSharedPreferences(Config.SETTINGS_NAME,0).edit().putInt(UpdatePolicy.KEY,0).commit();
  MainActivity activity=rule.launchActivity(new Intent(Intent.ACTION_MAIN));
  Field f=MinutePulse.class.getDeclaredField("listeners");f.setAccessible(true);
  final int[] visible={0};main(()->{try{visible[0]=((Set<?>)f.get(null)).size();}catch(Exception e){throw new RuntimeException(e);}});assertTrue(visible[0]>0);
  main(()->activity.startActivity(new Intent(activity,LauncherSettingsActivity.class)));
  main(()->{try{assertEquals(0,((Set<?>)f.get(null)).size());}catch(Exception e){throw new RuntimeException(e);}});
  c.getSharedPreferences(Config.SETTINGS_NAME,0).edit().putInt(Config.DOCK_OPACITY_KEY,30).putLong(Config.SETTINGS_REVISION_KEY,c.getSharedPreferences(Config.SETTINGS_NAME,0).getLong(Config.SETTINGS_REVISION_KEY,0)+1).commit();
  instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);instrumentation.waitForIdleSync();Thread.sleep(300);
  assertFalse("Opacity must keep the existing Home activity",activity.isDestroyed());
  main(()->{try{assertTrue(((Set<?>)f.get(null)).size()>0);}catch(Exception e){throw new RuntimeException(e);}});
 }
 @Test public void screenOffStopsMinuteQueueAndWakeRestartsIt()throws Exception{
  Context c=instrumentation.getTargetContext();c.getSharedPreferences(Config.SETTINGS_NAME,0).edit().putInt(UpdatePolicy.KEY,0).commit();
  rule.launchActivity(new Intent(Intent.ACTION_MAIN));Field f=MinutePulse.class.getDeclaredField("listeners");f.setAccessible(true);
  try{
   try(java.io.InputStream input=new android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.getUiAutomation().executeShellCommand("input keyevent 223"))) { while(input.read()!=-1) {} }
   android.os.PowerManager power=c.getSystemService(android.os.PowerManager.class);
   long deadline=System.currentTimeMillis()+4000;while(power.isInteractive() && System.currentTimeMillis()<deadline)Thread.sleep(50);
   assertFalse("Sleep command must really turn off the display",power.isInteractive());Thread.sleep(200);instrumentation.waitForIdleSync();
   main(()->{try{assertEquals(0,((Set<?>)f.get(null)).size());}catch(Exception e){throw new RuntimeException(e);}});
  }finally{try(java.io.InputStream input=new android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.getUiAutomation().executeShellCommand("input keyevent 224"))) { while(input.read()!=-1) {} }}
  Thread.sleep(800);instrumentation.waitForIdleSync();main(()->{try{assertTrue(((Set<?>)f.get(null)).size()>0);}catch(Exception e){throw new RuntimeException(e);}});
 }

}
