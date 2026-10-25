package com.gathin.tvhome;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;import org.junit.runner.RunWith;
import java.io.*;import java.net.*;import java.lang.reflect.Field;import java.security.MessageDigest;import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class ResumeDownloadTest {
 private void await(UpdateManager manager)throws Exception{long end=System.currentTimeMillis()+15000;while(manager.state!=UpdateManager.ERROR && manager.state!=UpdateManager.READY && System.currentTimeMillis()<end)Thread.sleep(30);assertEquals(manager.error,UpdateManager.ERROR,manager.state);}
 @Test public void interruptedBytesResumeAfterManagerRecreationThenInvalidApkIsRejected()throws Exception{
  Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
  c.getSharedPreferences("milo_updates",0).edit().clear().commit();
  File dir=new File(c.getCacheDir(),"updates");dir.mkdirs();new File(dir,"download.part").delete();new File(dir,"update.apk").delete();
  byte[] bytes=new byte[200000];for(int i=0;i<bytes.length;i++)bytes[i]=(byte)i;
  String hash=UpdateApk.hex(MessageDigest.getInstance("SHA-256").digest(bytes));
  UpdateRelease release=new UpdateRelease(BuildConfig.VERSION_CODE+1,bytes.length,26,"0.0.9-rc.2","rc",hash,UpdateApk.installedSigner(c),"https://github.com/anrhangels/milo-home/releases/download/v0.0.9-rc.2/Milo-Home.apk","fixture","2026-10-26","test",c.getPackageName());
  Field factory=UpdateHttp.class.getDeclaredField("connectionFactory");factory.setAccessible(true);Object previous=factory.get(null);
  AtomicInteger calls=new AtomicInteger();
  try{
   factory.set(null,(UpdateHttp.ConnectionFactory)url->new HttpURLConnection(url){
    final int call=calls.getAndIncrement();
    public void disconnect(){} public boolean usingProxy(){return false;} public void connect(){}
    public int getResponseCode(){if(call==1){assertEquals("bytes=100000-",getRequestProperty("Range"));assertEquals("\"fixture\"",getRequestProperty("If-Range"));return 206;}return 200;}
    public long getContentLengthLong(){return call==0?200000:100000;}
    public String getHeaderField(String name){if(name.equals("ETag"))return "\"fixture\"";if(name.equals("Content-Range"))return "bytes 100000-199999/200000";return null;}
    public InputStream getInputStream(){if(call>0)return new ByteArrayInputStream(bytes,100000,100000);return new InputStream(){int at;public int read()throws IOException{if(at>=100000)throw new IOException("simulated connection drop");return bytes[at++]&255;}public int read(byte[] b,int off,int len)throws IOException{if(at>=100000)throw new IOException("simulated connection drop");int n=Math.min(len,100000-at);System.arraycopy(bytes,at,b,off,n);at+=n;return n;}};}
   });
   UpdateManager first=UpdateManager.get(c);first.download(release);await(first);
   assertEquals(100000,new File(dir,"download.part").length());assertTrue(first.hasPartial());
   Field singleton=UpdateManager.class.getDeclaredField("instance");singleton.setAccessible(true);singleton.set(null,null);
   UpdateManager second=UpdateManager.get(c);assertTrue(second.hasPartial());second.download(release);await(second);
   assertEquals(2,calls.get());assertEquals(100,second.percent);assertFalse(new File(dir,"download.part").exists());assertFalse(new File(dir,"update.apk").exists());
  }finally{factory.set(null,previous);}
 }
 @Test public void genuineSignedApkResumesVerifiesAndSurvivesManagerRecreation()throws Exception{
  org.junit.Assume.assumeTrue("Explicit local APK fixture required","true".equals(InstrumentationRegistry.getArguments().getString("resumeFixture")));
  Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
  c.getSharedPreferences("milo_updates",0).edit().clear().commit();c.getSharedPreferences(Config.SETTINGS_NAME,0).edit().putInt(UpdatePolicy.KEY,0).commit();
  byte[] bytes;try(InputStream in=InstrumentationRegistry.getInstrumentation().getContext().getAssets().open("resume-next.apk");ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] block=new byte[32768];int n;while((n=in.read(block))!=-1)out.write(block,0,n);bytes=out.toByteArray();}
  File candidate=new File(c.getCacheDir(),"resume-candidate.apk");try(FileOutputStream output=new FileOutputStream(candidate)){output.write(bytes);}
  android.content.pm.PackageInfo candidateInfo=c.getPackageManager().getPackageArchiveInfo(candidate.getAbsolutePath(),0);assertNotNull(candidateInfo);candidate.delete();assertTrue("Fixture must be newer than target",candidateInfo.versionCode>BuildConfig.VERSION_CODE);
  int half=bytes.length/2;String hash=UpdateApk.hex(MessageDigest.getInstance("SHA-256").digest(bytes));String signer=UpdateApk.installedSigner(c);
  String asset=BuildConfig.BYEDPI_ENABLED?"Milo-Home-ByeByeDPI.apk":"Milo-Home.apk",tag="v"+candidateInfo.versionName;
  String address="https://github.com/"+BuildConfig.UPDATE_REPO+"/releases/download/"+tag+"/"+asset;
  UpdateRelease release=new UpdateRelease(candidateInfo.versionCode,bytes.length,26,candidateInfo.versionName,"rc",hash,signer,address,"local test","2026-10-26","test",c.getPackageName());
  org.json.JSONObject metadata=new org.json.JSONObject().put("schema",1).put("variant",BuildConfig.UPDATE_VARIANT).put("channel","rc").put("track","test").put("tag",tag).put("asset",asset).put("size",bytes.length).put("sha256",hash).put("signerSha256",signer).put("versionCode",candidateInfo.versionCode).put("versionName",release.name).put("minSdk",26).put("applicationId",c.getPackageName());
  String feed=new org.json.JSONArray().put(new org.json.JSONObject().put("tag_name",tag).put("prerelease",true).put("body",UpdateFeed.START+metadata+UpdateFeed.END).put("assets",new org.json.JSONArray().put(new org.json.JSONObject().put("name",asset).put("size",bytes.length).put("browser_download_url",address)))).toString();
  File dir=new File(c.getCacheDir(),"updates");dir.mkdirs();new File(dir,"download.part").delete();new File(dir,"update.apk").delete();
  Field singleton=UpdateManager.class.getDeclaredField("instance");singleton.setAccessible(true);singleton.set(null,null);
  Field factory=UpdateHttp.class.getDeclaredField("connectionFactory");factory.setAccessible(true);Object previous=factory.get(null);AtomicInteger calls=new AtomicInteger();
  try{
   factory.set(null,(UpdateHttp.ConnectionFactory)url->new HttpURLConnection(url){
    final int call=calls.getAndIncrement();public void disconnect(){}public boolean usingProxy(){return false;}public void connect(){}
    public int getResponseCode(){if(call==1){assertEquals("bytes="+half+"-",getRequestProperty("Range"));return 206;}return 200;}
    public long getContentLengthLong(){return call==0?bytes.length:bytes.length-half;}
    public String getHeaderField(String name){return name.equals("Content-Range")?"bytes "+half+"-"+(bytes.length-1)+"/"+bytes.length:null;}
    public InputStream getInputStream(){if(call>0)return new ByteArrayInputStream(bytes,half,bytes.length-half);return new InputStream(){int at;public int read()throws IOException{if(at>=half)throw new IOException("drop");return bytes[at++]&255;}public int read(byte[] b,int off,int len)throws IOException{if(at>=half)throw new IOException("drop");int n=Math.min(len,half-at);System.arraycopy(bytes,at,b,off,n);at+=n;return n;}};}
   });
   UpdateManager first=UpdateManager.get(c);first.download(release);await(first);assertEquals(half,new File(dir,"download.part").length());
   singleton.set(null,null);UpdateManager second=UpdateManager.get(c);second.download(release);long end=System.currentTimeMillis()+20000;while(second.state!=UpdateManager.READY && second.state!=UpdateManager.ERROR && System.currentTimeMillis()<end)Thread.sleep(40);
   assertEquals(second.error,UpdateManager.READY,second.state);assertEquals(hash,UpdateApk.digest(second.readyFile));assertEquals(2,calls.get());
   c.getSharedPreferences("milo_updates",0).edit().putString("feed",feed).commit();singleton.set(null,null);
   UpdateManager recovered=UpdateManager.get(c);recovered.check(false);end=System.currentTimeMillis()+20000;while(recovered.isChecking() && System.currentTimeMillis()<end)Thread.sleep(40);
   assertEquals(recovered.error,UpdateManager.READY,recovered.state);assertNotNull(recovered.readyFile);assertEquals(hash,UpdateApk.digest(recovered.readyFile));assertEquals(2,calls.get());assertEquals(BuildConfig.VERSION_CODE,c.getPackageManager().getPackageInfo(c.getPackageName(),0).versionCode);
   factory.set(null,(UpdateHttp.ConnectionFactory)url->{throw new IOException("offline after verified download");});
   recovered.check(true);end=System.currentTimeMillis()+10000;while(recovered.isChecking() && System.currentTimeMillis()<end)Thread.sleep(30);
   assertEquals("An offline feed must not lose the verified installer",UpdateManager.READY,recovered.state);assertEquals(hash,UpdateApk.digest(recovered.readyFile));
  }finally{factory.set(null,previous);new File(dir,"update.apk").delete();new File(dir,"download.part").delete();c.getSharedPreferences("milo_updates",0).edit().clear().commit();singleton.set(null,null);}
 }

}
