package com.gathin.tvhome;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
final class AppLaunchFailure {
 static void show(Context context,String pkg,String label,Runnable changed){
  if(!(context instanceof Activity))return;Activity activity=(Activity)context;
  if(activity.isFinishing() || activity.isDestroyed())return;
  int reason=R.string.opt_launch_blocked;
  try{if(!context.getPackageManager().getApplicationInfo(pkg,0).enabled)reason=R.string.opt_launch_disabled;}
  catch(PackageManager.NameNotFoundException missing){reason=R.string.opt_launch_removed;}
  new AlertDialog.Builder(activity).setTitle(label).setMessage(reason)
   .setNegativeButton(android.R.string.cancel,null).setPositiveButton(R.string.opt_remove_shortcut,(d,w)->{
    if(LibraryApps.save(context,pkg,false))changed.run();else android.widget.Toast.makeText(context,R.string.library_save_failed,android.widget.Toast.LENGTH_LONG).show();
   }).show();
 }
}
