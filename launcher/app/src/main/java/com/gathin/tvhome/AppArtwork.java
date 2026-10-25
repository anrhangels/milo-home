package com.gathin.tvhome;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import java.util.HashMap;
import java.util.Map;
/** Shared immutable drawable state, separate wrappers per view; bounded, no Activity references. */
final class AppArtwork {
 interface Loader{Drawable load();}
 private static final class Image {final Drawable.ConstantState state;final int bytes;Image(Drawable d){state=d.getConstantState();bytes=d instanceof android.graphics.drawable.BitmapDrawable?((android.graphics.drawable.BitmapDrawable)d).getBitmap().getAllocationByteCount():(int)Math.min(16L*1024*1024,Math.max(1024L,(long)Math.max(1,d.getIntrinsicWidth())*Math.max(1,d.getIntrinsicHeight())*4));}}
 private static final LruCache<String,Image> images=new LruCache<String,Image>(4*1024*1024){protected int sizeOf(String key,Image value){return value.bytes;}};
 private static final Map<String,Integer> colors=new HashMap<>();
 private static String theme="";
 static synchronized void theme(String value){if(!theme.equals(value)){theme=value;colors.clear();}}
 static Drawable get(Context context,ResolveInfo app,String kind,Loader loader){
  String key=app.activityInfo.packageName+"/"+app.activityInfo.name+":"+kind+":"+context.getResources().getConfiguration().hashCode();
  Image cached; synchronized(AppArtwork.class){cached=images.get(key);}
  if(cached!=null)return cached.state.newDrawable(context.getResources()).mutate();
  Drawable result=loader.load();
  if(result!=null && result.getConstantState()!=null){synchronized(AppArtwork.class){
   Image ready=images.get(key);if(ready!=null)return ready.state.newDrawable(context.getResources()).mutate();
   images.put(key,new Image(result));
  }}
  return result;
 }
 static synchronized int color(String key,java.util.function.IntSupplier sample){
  Integer value=colors.get(key);if(value==null){value=sample.getAsInt();if(colors.size()>=256)colors.clear();colors.put(key,value);}return value;
 }
 static synchronized void invalidate(String pkg){
  for(String key:images.snapshot().keySet())if(pkg==null || key.startsWith(pkg+"/"))images.remove(key);
  colors.keySet().removeIf(k->pkg==null || k.startsWith(pkg+"/"));
 }
 static synchronized void trim(){images.evictAll();}
}
