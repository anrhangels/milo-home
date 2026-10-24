package com.gathin.tvhome;
import android.content.Context;
import android.content.pm.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.security.MessageDigest;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MigrationVerifyTest {
    private String hash(byte[] data)throws Exception {
        StringBuilder out=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(data))out.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return out.toString();
    }
    @Test public void lineageRetainsIdentityDataAndPairedPermission() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();PackageManager pm=context.getPackageManager();
        PackageInfo info=pm.getPackageInfo(context.getPackageName(),PackageManager.GET_SIGNING_CERTIFICATES);
        assertEquals(100,info.versionCode);
        assertEquals("preserve-through-stable-and-rc",context.getSharedPreferences("milo_e2e",0).getString("sentinel",""));
        assertEquals(5,context.getSharedPreferences("launcher_settings",0).getInt("columns",0));
        assertTrue(info.signingInfo.hasPastSigningCertificates());
        String expected=InstrumentationRegistry.getArguments().getString("expectedSigner");assertNotNull(expected);
        assertEquals(expected,hash(info.signingInfo.getApkContentsSigners()[0].toByteArray()));
        assertEquals(PackageManager.SIGNATURE_MATCH,pm.checkSignatures(context.getPackageName(),"io.github.romanvht.byedpi"));
        assertEquals(PackageManager.PERMISSION_GRANTED,pm.checkPermission("io.github.romanvht.byedpi.permission.TV_CONTROL",context.getPackageName()));
    }
}
