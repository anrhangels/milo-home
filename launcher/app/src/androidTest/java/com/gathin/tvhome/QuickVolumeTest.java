package com.gathin.tvhome;
import android.content.Intent;import android.media.AudioManager;import android.widget.SeekBar;
import androidx.test.ext.junit.runners.AndroidJUnit4;import androidx.test.platform.app.InstrumentationRegistry;import androidx.test.rule.ActivityTestRule;
import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class QuickVolumeTest {
 @Rule public ActivityTestRule<QuickSettingsActivity> rule=new ActivityTestRule<>(QuickSettingsActivity.class,false,false);
 @Test public void systemVolumeChangesRefreshWithoutActivityKeyDispatch()throws Exception {
  android.app.Instrumentation i=InstrumentationRegistry.getInstrumentation();
  AudioManager audio=i.getTargetContext().getSystemService(AudioManager.class);Assume.assumeFalse(audio.isVolumeFixed());
  int old=audio.getStreamVolume(AudioManager.STREAM_MUSIC),max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);Assume.assumeTrue(max>0);
  int target=old<max?old+1:old-1;
  QuickSettingsActivity a=rule.launchActivity(new Intent());i.waitForIdleSync();
  java.lang.reflect.Field f=QuickSettingsActivity.class.getDeclaredField("volume");f.setAccessible(true);SeekBar bar=(SeekBar)f.get(a);
  try {
   audio.setStreamVolume(AudioManager.STREAM_MUSIC,target,0);
   long deadline=System.currentTimeMillis()+3000;final int[] seen={-1};
   do {i.runOnMainSync(()->seen[0]=bar.getProgress());if(seen[0]==target)break;Thread.sleep(50);}while(System.currentTimeMillis()<deadline);
   assertEquals("Visible slider must follow a system volume event",target,seen[0]);
  } finally {audio.setStreamVolume(AudioManager.STREAM_MUSIC,old,0);}
 }
}
