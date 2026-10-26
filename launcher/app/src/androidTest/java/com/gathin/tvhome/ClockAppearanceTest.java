package com.gathin.tvhome;
import android.content.*;import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;import androidx.test.platform.app.InstrumentationRegistry;import androidx.test.rule.ActivityTestRule;
import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class ClockAppearanceTest {
 @Rule public ActivityTestRule<ClockActivity> rule=new ActivityTestRule<>(ClockActivity.class,false,false);
 private final android.app.Instrumentation i=InstrumentationRegistry.getInstrumentation();
 @Test public void blackAndAmbientModesPersistAndRender() {
  Context c=i.getTargetContext();android.content.SharedPreferences p=c.getSharedPreferences(Config.SETTINGS_NAME,0);
  boolean had=p.contains(ClockBackdrop.KEY);int old=p.getInt(ClockBackdrop.KEY,0);
  try {
   for(int mode:new int[]{ClockBackdrop.BLACK,ClockBackdrop.AMBIENT}) {
    p.edit().putInt(ClockBackdrop.KEY,mode).commit();
    ClockActivity a=rule.launchActivity(new Intent());i.waitForIdleSync();
    final int expected=mode==ClockBackdrop.BLACK?View.GONE:View.VISIBLE;
    i.runOnMainSync(()->assertEquals(expected,a.findViewById(R.id.dreamAmbient).getVisibility()));
    assertEquals(mode,p.getInt(ClockBackdrop.KEY,-1));rule.finishActivity();
   }
  } finally {android.content.SharedPreferences.Editor e=p.edit();if(had)e.putInt(ClockBackdrop.KEY,old);else e.remove(ClockBackdrop.KEY);e.commit();}
 }
}
