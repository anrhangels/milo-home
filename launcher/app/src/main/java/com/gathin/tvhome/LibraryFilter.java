package com.gathin.tvhome;
import java.util.Locale;
final class LibraryFilter {
 static boolean technical(boolean system,boolean tv,boolean onHome,boolean backend,String pkg){
  boolean tool=pkg.endsWith(".traceur") || pkg.endsWith(".packageinstaller") || pkg.endsWith(".permissioncontroller") || pkg.endsWith(".certinstaller");
  return !onHome && (backend || system && (tool || !tv));
 }
 static String initial(String name){return name==null || name.isEmpty()?"#":new String(Character.toChars(name.codePointAt(0))).toUpperCase(Locale.getDefault());}
}
