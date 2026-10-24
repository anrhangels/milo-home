package com.gathin.tvhome;
import android.content.*;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.*;
import org.junit.runner.RunWith;
import java.io.IOException;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class OfflineUpdaterTest {
    @Rule public ActivityTestRule<MainActivity> rule=new ActivityTestRule<>(MainActivity.class,false,false);
    @Test public void slowOfflineCheckDoesNotBlockHome() throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences("milo_updates",0).edit().clear().commit();
        Field factory=UpdateHttp.class.getDeclaredField("connectionFactory");factory.setAccessible(true);
        factory.set(null,(UpdateHttp.ConnectionFactory)url -> {
            try { Thread.sleep(1800); } catch(InterruptedException e) { Thread.currentThread().interrupt(); }
            throw new IOException("Simulated offline transport");
        });
        MainActivity activity=rule.launchActivity(new Intent(Intent.ACTION_MAIN));
        long start=System.currentTimeMillis();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> activity.findViewById(R.id.btnWifi).requestFocus());
        assertTrue("Network blocked main thread",System.currentTimeMillis()-start<1200);
        UpdateManager manager=UpdateManager.get(context);
        long deadline=System.currentTimeMillis()+10000;
        while(manager.state!=UpdateManager.ERROR && System.currentTimeMillis()<deadline) Thread.sleep(50);
        assertEquals(UpdateManager.ERROR,manager.state);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> assertEquals(View.GONE,activity.findViewById(R.id.btnUpdate).getVisibility()));
    }
}
