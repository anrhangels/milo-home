package com.gathin.tvhome;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import org.junit.*;
import org.junit.runner.RunWith;
import org.json.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.security.MessageDigest;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

/** Real Android UI and APK verification, with an explicitly labelled local delivery fixture. */
@RunWith(AndroidJUnit4.class)
public class UpdaterUiTest {
    @Rule public ActivityTestRule<MainActivity> rule=new ActivityTestRule<>(MainActivity.class,false,false);
    private final Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
    private final Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
    private MainActivity activity;
    private UpdateManager manager;
    private UpdateUi ui;
    private byte[] apk;
    private void main(Runnable action) { instrumentation.runOnMainSync(action); instrumentation.waitForIdleSync(); }
    private void await(BooleanSupplier condition) throws Exception {
        long deadline=System.currentTimeMillis()+12000;
        while(!condition.getAsBoolean() && System.currentTimeMillis()<deadline) Thread.sleep(40);
        assertTrue("Timed out waiting for UI/worker condition; state="+(manager==null?-1:manager.state),condition.getAsBoolean());
    }
    private void shot(String name) throws Exception {
        Bitmap image=instrumentation.getUiAutomation().takeScreenshot(); assertNotNull(image);
        File dir=new File(context.getFilesDir(),"milo-demo-screenshots");dir.mkdirs();
        try(OutputStream out=new FileOutputStream(new File(dir,name+".png"))) { image.compress(Bitmap.CompressFormat.PNG,100,out); }
        image.recycle();
    }
    private Object field(Object value,String name) throws Exception { Field f=value.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(value); }
    private JSONObject release(int code,boolean rc,String hash) throws Exception {
        String tag=rc?"test-v1.1.0-rc.1":"test-v1.0.0";
        String version=rc?"1.1.0-rc.1":"1.0.0";
        JSONObject metadata=new JSONObject().put("schema",1).put("tag",tag).put("versionCode",code).put("versionName",version)
            .put("channel",rc?"rc":"stable").put("applicationId",context.getPackageName()).put("size",apk.length).put("sha256",hash)
            .put("signerSha256",UpdateApk.installedSigner(context)).put("minSdk",26).put("track","test").put("asset","MiloHome.apk")
            .put("notes","DEMO — local test manifest; not a published GitHub release.");
        JSONObject asset=new JSONObject().put("name","MiloHome.apk").put("size",apk.length).put("state","uploaded")
            .put("browser_download_url","https://github.com/"+BuildConfig.UPDATE_REPO+"/releases/download/"+tag+"/MiloHome.apk");
        return new JSONObject().put("draft",false).put("prerelease",rc).put("tag_name",tag).put("published_at","2026-10-24T00:00:00Z")
            .put("body",UpdateFeed.START+metadata+UpdateFeed.END).put("assets",new JSONArray().put(asset));
    }
    private AlertDialog confirmation() throws Exception { return (AlertDialog)field(ui,"confirmation"); }
    private void clickChannel(int index) throws Exception {
        PopupWindow popup=(PopupWindow)field(ui,"popup");assertNotNull(popup);
        LinearLayout rows=popup.getContentView().findViewById(R.id.rows);
        main(() -> rows.getChildAt(index+1).performClick());
    }
    @Test public void realUiAndVerifiedDownload() throws Exception {
        try(InputStream in=instrumentation.getContext().getAssets().open("MiloHome.apk");ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[32768];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);apk=out.toByteArray();
        }
        context.getSharedPreferences("milo_updates",0).edit().clear().putString("feed","[]").putLong("success",System.currentTimeMillis()).commit();
        context.getSharedPreferences("milo_e2e",0).edit().putString("sentinel","preserve-through-stable-and-rc").commit();
        context.getSharedPreferences("launcher_settings",0).edit().putInt("columns",5).commit();
        activity=rule.launchActivity(new Intent(Intent.ACTION_MAIN));manager=UpdateManager.get(context);ui=(UpdateUi)field(activity,"updateUi");
        await(() -> !manager.releases.isEmpty() || manager.state!=UpdateManager.CHECKING);
        main(() -> assertEquals(View.GONE,activity.findViewById(R.id.btnUpdate).getVisibility()));
        Thread.sleep(400);shot("home");
        String hash=UpdateApk.hex(MessageDigest.getInstance("SHA-256").digest(apk));
        JSONArray feed=new JSONArray().put(release(10,false,hash)).put(release(11,true,hash));
        context.getSharedPreferences("milo_updates",0).edit().putString("feed",feed.toString()).putLong("success",System.currentTimeMillis()).commit();
        main(() -> manager.check(false));await(() -> manager.releases.size()==2);
        main(() -> activity.findViewById(R.id.btnUpdate).requestFocus());shot("update-available");
        main(() -> activity.findViewById(R.id.btnUpdate).performClick());Thread.sleep(250);shot("update-channels");
        clickChannel(0);shot("update-confirm");
        AlertDialog dialog=confirmation();assertNotNull(dialog);
        main(() -> dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick());
        assertNotEquals(UpdateManager.DOWNLOADING,manager.state);
        clickChannel(1);AlertDialog rc=confirmation();assertNotNull(rc);
        main(() -> rc.getButton(AlertDialog.BUTTON_NEGATIVE).performClick());
        // Only this in-process instrumentation swaps the transport; no production test server/IPC exists.
        Field factory=UpdateHttp.class.getDeclaredField("connectionFactory");factory.setAccessible(true);
        factory.set(null,(UpdateHttp.ConnectionFactory)url -> new HttpURLConnection(url) {
            public void disconnect() { } public boolean usingProxy() {return false;} public void connect() { }
            public int getResponseCode(){return 200;} public long getContentLengthLong(){return apk.length;}
            public InputStream getInputStream(){return new ByteArrayInputStream(apk){
                public synchronized int read(byte[] buffer,int off,int len){
                    try{Thread.sleep(18);}catch(InterruptedException e){Thread.currentThread().interrupt();}
                    return super.read(buffer,off,Math.min(len,4096));
                }
            };}
        });
        clickChannel(0);AlertDialog yes=confirmation();assertNotNull(yes);
        main(() -> yes.getButton(AlertDialog.BUTTON_POSITIVE).performClick());
        await(() -> manager.state==UpdateManager.DOWNLOADING && manager.percent>5 && manager.percent<90);
        instrumentation.waitForIdleSync(); Thread.sleep(150);
        shot("update-progress");
        await(() -> manager.state==UpdateManager.READY || manager.state==UpdateManager.ERROR);
        assertEquals(manager.error,UpdateManager.READY,manager.state);
        assertTrue(manager.readyFile.isFile());assertEquals(hash,UpdateApk.digest(manager.readyFile));
        context.getSharedPreferences("milo_e2e",0).edit().putBoolean("download_verified",true).commit();
        Thread.sleep(400);shot("install-permission-or-installer");
    }
}
