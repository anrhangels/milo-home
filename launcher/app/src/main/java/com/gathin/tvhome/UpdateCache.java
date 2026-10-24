package com.gathin.tvhome;
import java.io.File;

/** Only our own obsolete updater files; never app data, themes or other apps' caches. */
final class UpdateCache {
    interface VersionReader { long version(File file) throws Exception; }
    static void clean(File directory,long installed,VersionReader reader) {
        File partial=new File(directory,"download.part");
        if(partial.isFile()) partial.delete();
        File apk=new File(directory,"update.apk");
        if(!apk.isFile()) return;
        try { long version=reader.version(apk);if(version>0 && version<=installed) apk.delete(); }
        catch(Exception unavailable) { /* Unknown files are not evidence of an installed update. */ }
    }
}
