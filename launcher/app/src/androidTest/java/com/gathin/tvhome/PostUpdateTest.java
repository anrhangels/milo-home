package com.gathin.tvhome;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** No references to minified app internals: checks persistent data after the real installer replacement. */
@RunWith(AndroidJUnit4.class)
public class PostUpdateTest {
    @Test public void installedVersionAndDataPreserved() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        int version=context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode;
        assertTrue("Expected Stable or RC update",version==10 || version==11);
        assertEquals("preserve-through-stable-and-rc",context.getSharedPreferences("milo_e2e",0).getString("sentinel",""));
        assertTrue(context.getSharedPreferences("milo_e2e",0).getBoolean("download_verified",false));
        assertEquals(5,context.getSharedPreferences("launcher_settings",0).getInt("columns",0));
    }
}
