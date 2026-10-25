package com.gathin.tvhome;
import android.content.Context;
final class AppNames {
 static String clean(String value){
  if(value==null)return "";StringBuilder out=new StringBuilder();int count=0;
  for(int at=0;at<value.length() && count<40;){int c=value.codePointAt(at);at+=Character.charCount(c);
   if(Character.isISOControl(c)||Character.getType(c)==Character.FORMAT)continue;
   out.appendCodePoint(c);count++;
  }return out.toString().trim();
 }
 static String get(Context c,String pkg,String fallback){String n=clean(c.getSharedPreferences("milo_app_names",0).getString(pkg,""));return n.isEmpty()?fallback:n;}
 static void save(Context c,String pkg,String value){c.getSharedPreferences("milo_app_names",0).edit().putString(pkg,clean(value)).apply();}
}
