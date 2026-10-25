package com.gathin.tvhome;
import android.content.*;
import android.os.Build;
import android.os.PowerManager;
/** Registered only for a visible owner. Screen-off immediately suspends its UI work. */
final class ScreenWork {
 private final Context context;private final Runnable visible,hidden;private boolean registered;private boolean screenOn;
 ScreenWork(Context c,Runnable on,Runnable off){context=c;visible=on;hidden=off;}
 boolean interactive(){if(registered)return screenOn;PowerManager p=(PowerManager)context.getSystemService(Context.POWER_SERVICE);return p==null || p.isInteractive();}
 private final BroadcastReceiver receiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){screenOn=Intent.ACTION_SCREEN_ON.equals(i.getAction());if(interactive())visible.run();else hidden.run();}};
 void start(){if(registered)return;screenOn=interactive();registered=true;IntentFilter f=new IntentFilter(Intent.ACTION_SCREEN_ON);f.addAction(Intent.ACTION_SCREEN_OFF);
  if(Build.VERSION.SDK_INT>=33)context.registerReceiver(receiver,f,Context.RECEIVER_NOT_EXPORTED);else context.registerReceiver(receiver,f);
  if(interactive())visible.run();else hidden.run();
 }
 void stop(){if(registered){registered=false;context.unregisterReceiver(receiver);}}
}
