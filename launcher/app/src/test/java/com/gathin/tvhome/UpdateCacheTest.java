package com.gathin.tvhome;
import org.junit.Test;
import java.io.*;
import java.nio.file.Files;
import static org.junit.Assert.*;
public class UpdateCacheTest {
    @Test public void deletesOnlyInstalledUpdateAndInterruptedDownload() throws Exception {
        File dir=Files.createTempDirectory("milo-cache").toFile();
        try {
            for(String name:new String[]{"download.part","update.apk","theme.json","byedpi-paired.apk"}) new File(dir,name).createNewFile();
            UpdateCache.clean(dir,106,file -> 106);
            assertFalse(new File(dir,"download.part").exists());assertFalse(new File(dir,"update.apk").exists());
            assertTrue(new File(dir,"theme.json").exists());assertTrue(new File(dir,"byedpi-paired.apk").exists());
        } finally { for(File f:dir.listFiles())f.delete();dir.delete(); }
    }
    @Test public void keepsPendingNewerOrUnreadableApk() throws Exception {
        File dir=Files.createTempDirectory("milo-cache").toFile(),apk=new File(dir,"update.apk");
        try {
            apk.createNewFile();UpdateCache.clean(dir,105,file -> 106);assertTrue(apk.exists());
            UpdateCache.clean(dir,105,file -> {throw new IOException();});assertTrue(apk.exists());
            UpdateCache.clean(dir,105,file -> -1);assertTrue(apk.exists());
        } finally {apk.delete();dir.delete();}
    }
}
