package com.gathin.tvhome;
/** Drop stale navigation, preserving Back, Home, volume, fresh repeats and key-up cleanup. */
final class RemoteInput {
 static boolean stale(boolean navigation,boolean down,long eventTime,long now){return navigation && down && now-eventTime>400;}
 static boolean drop(android.view.KeyEvent event){
  int k=event.getKeyCode();boolean navigation=k==19 || k==20 || k==21 || k==22 || k==23 || k==66;
  return stale(navigation,event.getAction()==android.view.KeyEvent.ACTION_DOWN,event.getEventTime(),android.os.SystemClock.uptimeMillis());
 }
}
