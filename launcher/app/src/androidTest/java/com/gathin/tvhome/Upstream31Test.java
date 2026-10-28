package com.gathin.tvhome;
import android.app.Activity;import android.content.Intent;import android.os.SystemClock;import android.view.KeyEvent;import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;import androidx.test.platform.app.InstrumentationRegistry;import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;import androidx.test.runner.lifecycle.Stage;
import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class Upstream31Test {
 @Rule public ActivityTestRule<MainActivity> rule=new ActivityTestRule<>(MainActivity.class,false,false);
 private final android.app.Instrumentation i=InstrumentationRegistry.getInstrumentation();
 private void main(Runnable r){i.runOnMainSync(r);i.waitForIdleSync();}
 private Object field(Object o,String n)throws Exception{java.lang.reflect.Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 private void flag(Object o,String n,boolean b)throws Exception{java.lang.reflect.Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.setBoolean(o,b);}
 private Activity resumed(){final Activity[] result={null};main(()->{for(Activity a:ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED))result[0]=a;});return result[0];}
 @Test public void interruptedSameCardFadeRecoversShelfAndGlow()throws Exception {
  MainActivity a=rule.launchActivity(new Intent(Intent.ACTION_MAIN));Thread.sleep(300);
  View hero=(View)field(a,"hero"),ambient=(View)field(a,"ambient");
  java.lang.reflect.Method show=MainActivity.class.getDeclaredMethod("showHero",int.class,float.class);show.setAccessible(true);
  main(()->{try{flag(a,"animations",true);flag(a,"reducedMotion",false);flag(a,"ambientGlow",true);show.invoke(a,0,500f);}catch(Exception e){throw new RuntimeException(e);}});Thread.sleep(750);
  main(()->{try{hero.animate().cancel();ambient.animate().cancel();hero.setAlpha(.2f);ambient.setAlpha(.2f);show.invoke(a,0,500f);}catch(Exception e){throw new RuntimeException(e);}});Thread.sleep(850);
  main(()->{assertEquals(1f,hero.getAlpha(),.02f);assertEquals(1f,ambient.getAlpha(),.02f);});
 }
 @Test public void mappedHoldOpensOwnActivityBeforeReleaseAndOnlyOnce()throws Exception {
  MainActivity a=rule.launchActivity(new Intent(Intent.ACTION_MAIN));long down=SystemClock.uptimeMillis();
  KeyEvent press=new KeyEvent(down,down,KeyEvent.ACTION_DOWN,195,0);
  String tap="com.gathin.tvhome/.MainActivity",hold="com.gathin.tvhome/.BugReportActivity";
  main(()->{assertTrue(RemoteKeys.handle(a,press,tap,hold));assertTrue(RemoteKeys.handle(a,press,tap,hold));});
  long end=SystemClock.uptimeMillis()+3500;Activity target;
  do{Thread.sleep(50);target=resumed();}while(!(target instanceof BugReportActivity) && SystemClock.uptimeMillis()<end);
  assertTrue("Long action must fire without waiting for UP",target instanceof BugReportActivity);
  KeyEvent release=new KeyEvent(down,SystemClock.uptimeMillis(),KeyEvent.ACTION_UP,195,0);
  main(()->{RemoteKeys.handle(a,release,tap,hold);RemoteKeys.handle(a,release,tap,hold);});Thread.sleep(150);
  assertSame("Release must not also run the tap action",target,resumed());Activity finalTarget=target;main(finalTarget::finish);
 }
}
