package com.gathin.tvhome;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BundledBackendTest {
    @Test public void bundledBackendIsValidAndTamperingIsRejected() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        File file=new File(context.getCacheDir(),"test-backend.apk");
        if(!BuildConfig.BYEDPI_ENABLED) {
            try(InputStream ignored=context.getAssets().open("byedpi/backend.apk")) { fail("Standard includes VPN payload"); }
            catch(FileNotFoundException expected) { return; }
        }
        try(InputStream in=context.getAssets().open("byedpi/backend.apk");OutputStream out=new FileOutputStream(file)) {
            byte[] buffer=new byte[32768];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
        }
        try {
            DpiInstaller.verify(context,file);
            try(RandomAccessFile data=new RandomAccessFile(file,"rw")) {
                data.seek(128);int original=data.read();data.seek(128);data.write(original^1);
            }
            try { DpiInstaller.verify(context,file);fail("Tampered backend accepted"); }
            catch(SecurityException expected) { }
        } finally { file.delete(); }
    }
}
