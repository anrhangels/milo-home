package com.gathin.tvhome;
import android.content.*;import android.content.pm.*;import java.util.*;
/** Reuses query results. Package broadcasts patch only that package; missed changes invalidate safely. */
final class AppCatalog {
 private static final Map<String,List<ResolveInfo>> lists=new HashMap<>();
 private static Intent intent(String category){return new Intent(Intent.ACTION_MAIN).addCategory(category);}
 static synchronized List<ResolveInfo> query(PackageManager pm,String category){
  List<ResolveInfo> result=lists.get(category);if(result==null){result=new ArrayList<>(pm.queryIntentActivities(intent(category),0));lists.put(category,result);}return new ArrayList<>(result);
 }
 static synchronized void changed(PackageManager pm,String pkg){
  if(pkg==null){lists.clear();return;}
  for(Map.Entry<String,List<ResolveInfo>> entry:lists.entrySet()){
   entry.getValue().removeIf(r->r.activityInfo!=null && pkg.equals(r.activityInfo.packageName));
   entry.getValue().addAll(pm.queryIntentActivities(intent(entry.getKey()).setPackage(pkg),0));
  }
 }
 static synchronized void clear(){lists.clear();}
}
