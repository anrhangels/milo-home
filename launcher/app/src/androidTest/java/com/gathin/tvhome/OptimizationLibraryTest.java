package com.gathin.tvhome;
import android.content.*;import android.content.pm.*;import android.graphics.drawable.*;import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;import androidx.test.platform.app.InstrumentationRegistry;import androidx.test.rule.ActivityTestRule;import androidx.recyclerview.widget.RecyclerView;
import org.junit.*;import org.junit.runner.RunWith;import java.lang.reflect.Field;import java.util.concurrent.atomic.AtomicInteger;import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class OptimizationLibraryTest {
 @Rule public ActivityTestRule<MainActivity> rule=new ActivityTestRule<>(MainActivity.class,false,false);
 private final android.app.Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
 private void main(Runnable r){instrumentation.runOnMainSync(r);instrumentation.waitForIdleSync();}
 private Object field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 private AppLibraryDialog library(MainActivity activity)throws Exception{long end=System.currentTimeMillis()+8000;AppLibraryDialog dialog;do{dialog=(AppLibraryDialog)field(activity,"appLibrary");if(dialog!=null && dialog.popup.isShowing())return dialog;Thread.sleep(40);}while(System.currentTimeMillis()<end);throw new AssertionError("Library did not open");}
 @Test public void sharedIconsHaveDistinctWrappersAndOneDecodedBitmap()throws Exception{
  Context c=instrumentation.getTargetContext();ResolveInfo info=new ResolveInfo();info.activityInfo=c.getPackageManager().getActivityInfo(new ComponentName(c,MainActivity.class),0);
  AppArtwork.invalidate(c.getPackageName());AtomicInteger loads=new AtomicInteger();
  Drawable a=AppArtwork.get(c,info,"icon",()->{loads.incrementAndGet();return info.loadIcon(c.getPackageManager());});
  Drawable b=AppArtwork.get(c,info,"icon",()->{loads.incrementAndGet();return info.loadIcon(c.getPackageManager());});
  assertEquals(1,loads.get());assertNotSame(a,b);if(a instanceof BitmapDrawable && b instanceof BitmapDrawable)assertSame(((BitmapDrawable)a).getBitmap(),((BitmapDrawable)b).getBitmap());
  AppArtwork.invalidate(c.getPackageName());AppArtwork.get(c,info,"icon",()->{loads.incrementAndGet();return info.loadIcon(c.getPackageManager());});assertEquals(2,loads.get());
 }
 @Test public void colorIsSampledOnceUntilPackageInvalidation(){AtomicInteger samples=new AtomicInteger();String key="test.app/.Main";AppArtwork.invalidate("test.app");assertEquals(7,AppArtwork.color(key,()->{samples.incrementAndGet();return 7;}));assertEquals(7,AppArtwork.color(key,()->{samples.incrementAndGet();return 8;}));assertEquals(1,samples.get());AppArtwork.invalidate("test.app");assertEquals(8,AppArtwork.color(key,()->{samples.incrementAndGet();return 8;}));assertEquals(2,samples.get());}
 @Test public void closingLibraryCancelsLoadsAndRestoresSelection()throws Exception{
  Context c=instrumentation.getTargetContext();c.getSharedPreferences(Config.SETTINGS_NAME,0).edit().putInt(UpdatePolicy.KEY,0).commit();c.getSharedPreferences("milo_library",0).edit().putBoolean("show_all",true).commit();
  MainActivity activity=rule.launchActivity(new Intent(Intent.ACTION_MAIN));main(activity::onLibraryRequested);AppLibraryDialog first=library(activity);
  RecyclerView catalog=(RecyclerView)field(first,"catalog");main(()->catalog.scrollToPosition(2));Thread.sleep(200);instrumentation.waitForIdleSync();
  main(()->{RecyclerView.ViewHolder holder=catalog.findViewHolderForAdapterPosition(2);assertNotNull(holder);holder.itemView.requestFocus();});
  instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_DPAD_UP);instrumentation.waitForIdleSync();
  View filter=(View)field(first,"letterControl");main(()->assertTrue("D-pad must reach the new filters",filter.isFocused()));
  instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_DPAD_DOWN);instrumentation.waitForIdleSync();
  main(()->assertNotNull("D-pad must return to the selected card",catalog.getFocusedChild()));
  main(first::close);String selected=c.getSharedPreferences("milo_library",0).getString("selected","");assertFalse(selected.isEmpty());assertTrue((Boolean)field(first,"closed"));assertTrue(((java.util.List<?>)field(first,"tasks")).isEmpty());
  main(activity::onLibraryRequested);AppLibraryDialog second=library(activity);assertEquals(selected,field(second,"selectedPackage"));main(second::close);
 }
}
