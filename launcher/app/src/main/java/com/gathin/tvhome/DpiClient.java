package com.gathin.tvhome;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;

/** Bound only while the popup is visible. No network permission, timer polling or started service. */
final class DpiClient implements AutoCloseable {
 static final String PACKAGE="io.github.romanvht.byedpi";
 interface Listener { void state(DpiState value); void consent(); }
 private final Context context;
 private Listener listener;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private Messenger remote;
 private boolean bound, closed, awaiting, timedOut;
 private boolean running;
 private final Runnable timeout=() -> {
  awaiting=false; timedOut=true;
  listener.state(new DpiState(DpiState.ERROR,running,false,"IPC timeout"));
  send(1); // One reconciliation request, never a polling loop.
 };
 private final Messenger reply=new Messenger(new Handler(Looper.getMainLooper(), message -> {
  if(closed || message.what!=100) return true;
  Bundle data=message.getData();
  if(data.getInt("version")!=1) { fail("Unsupported protocol"); return true; }
  int state=data.getInt("state"); running=data.getBoolean("running");
  boolean busy=data.getBoolean("busy");
  if(state!=DpiState.STARTING && state!=DpiState.STOPPING && !busy) {
   awaiting=false; handler.removeCallbacks(timeout);
  }
  if(timedOut && (state==DpiState.STARTING || state==DpiState.STOPPING || busy))
   listener.state(new DpiState(DpiState.ERROR,running,true,"Operation still pending"));
  else { timedOut=false; listener.state(new DpiState(state,running,busy,data.getString("error",""))); }
  if(state==DpiState.CONSENT) listener.consent();
  return true;
 }));
 private final ServiceConnection connection=new ServiceConnection() {
  public void onServiceConnected(ComponentName name,IBinder binder) { if(!closed) { remote=new Messenger(binder); send(1); } }
  public void onServiceDisconnected(ComponentName name) { remote=null; fail("Service disconnected"); }
  public void onBindingDied(ComponentName name) { remote=null; fail("Service binding died"); }
  public void onNullBinding(ComponentName name) { fail("Control service unavailable"); }
 };
 DpiClient(Context context,Listener listener) { this.context=context; this.listener=listener; }
 void connect() {
  try {
   context.getPackageManager().getPackageInfo(PACKAGE,0);
   if(context.getPackageManager().checkSignatures(context.getPackageName(),PACKAGE)!=PackageManager.SIGNATURE_MATCH) {
    fail("Install the paired local builds (signature mismatch)"); return;
   }
   Intent intent=new Intent().setComponent(new ComponentName(PACKAGE,PACKAGE+".tv.TvControlService"));
   bound=context.bindService(intent,connection,Context.BIND_AUTO_CREATE);
   if(!bound) fail("Install the paired local builds (no control API)");
   else handler.postDelayed(timeout,5000);
  } catch(PackageManager.NameNotFoundException e) { listener.state(new DpiState(DpiState.MISSING,false,false,"")); }
    catch(SecurityException e) { fail("Control permission denied"); }
 }
 void toggle(boolean enable) {
  if(awaiting || closed || remote==null) return;
  awaiting=true; timedOut=false; handler.removeCallbacks(timeout); handler.postDelayed(timeout,25000);
  listener.state(new DpiState(enable?DpiState.STARTING:DpiState.STOPPING,running,true,""));
  send(enable?2:3);
 }
 private void send(int command) {
  if(remote==null || closed) return;
  try { Message message=Message.obtain(null,command); message.replyTo=reply; remote.send(message); }
  catch(RemoteException e) { fail("Control service unavailable"); }
 }
 private void fail(String error) { if(closed) return; awaiting=false; handler.removeCallbacks(timeout); listener.state(new DpiState(DpiState.ERROR,running,false,error)); }
 public void close() {
  if(closed) return;
  send(4); closed=true; handler.removeCallbacksAndMessages(null);
  if(bound) { context.unbindService(connection); bound=false; } remote=null;
 }
}
