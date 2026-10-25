package com.gathin.tvhome;
import android.os.Handler;
import android.os.Looper;
import java.util.LinkedHashSet;
import java.util.Set;
/** One minute-aligned queue for visible Home and clock scenes; no subscribers means no work. */
final class MinutePulse {
 private static final Handler MAIN=new Handler(Looper.getMainLooper());
 private static final Set<Runnable> listeners=new LinkedHashSet<>();
 static long delay(long now){return 60000-Math.floorMod(now,60000);}
 private static final Runnable tick=new Runnable(){public void run(){
  for(Runnable r:listeners.toArray(new Runnable[0]))if(listeners.contains(r))r.run();
  schedule();
 }};
 static void add(Runnable r){listeners.add(r);schedule();}
 static void remove(Runnable r){listeners.remove(r);schedule();}
 private static void schedule(){MAIN.removeCallbacks(tick);if(!listeners.isEmpty())MAIN.postDelayed(tick,delay(System.currentTimeMillis()));}
}
