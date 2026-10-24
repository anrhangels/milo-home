package com.gathin.tvhome;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class UpdateFeedTest {
    static final String REPO="anrhangels/milo-home",PKG="com.gathin.tvhome",HASH=String.join("",Collections.nCopies(64,"a")),CERT=String.join("",Collections.nCopies(64,"b"));
    JSONObject release(long code,boolean rc) throws Exception {
        String tag=rc?"v1.1.0-rc.1":"v1.0.0";
        JSONObject m=new JSONObject().put("schema",1).put("tag",tag).put("versionCode",code)
            .put("versionName",tag.substring(1)).put("channel",rc?"rc":"stable").put("applicationId",PKG)
            .put("size",100).put("sha256",HASH).put("signerSha256",CERT).put("minSdk",26).put("track","production").put("asset","MiloHome.apk");
        JSONObject asset=new JSONObject().put("name","MiloHome.apk").put("size",100).put("state","uploaded")
            .put("browser_download_url","https://github.com/"+REPO+"/releases/download/"+tag+"/MiloHome.apk");
        return new JSONObject().put("draft",false).put("prerelease",rc).put("tag_name",tag)
            .put("body",UpdateFeed.START+m+UpdateFeed.END).put("assets",new JSONArray().put(asset));
    }
    List<UpdateRelease> parse(JSONObject... items) throws Exception {
        JSONArray array=new JSONArray(); for(JSONObject item:items) array.put(item);
        return UpdateFeed.parse(array.toString(),REPO,PKG,100,30,CERT,"production");
    }
    JSONObject metadata(JSONObject release) throws Exception {
        String body=release.getString("body"); return new JSONObject(body.substring(UpdateFeed.START.length(),body.length()-UpdateFeed.END.length()));
    }
    JSONObject replace(JSONObject release,String key,Object value) throws Exception {
        JSONObject m=metadata(release).put(key,value); release.put("body",UpdateFeed.START+m+UpdateFeed.END); return release;
    }
    @Test public void noUpdateHidesButtonData() throws Exception { assertTrue(parse(release(100,false)).isEmpty()); }
    @Test public void standardNeverReceivesByedpiOrLegacy() throws Exception {
        String feed=new JSONArray().put(release(101,false)).toString();
        assertTrue(UpdateFeed.parse(feed,REPO,PKG,100,30,CERT,"production","standard").isEmpty());
    }
    @Test public void editionsRemainSeparateWithinEachChannel() throws Exception {
        JSONObject standard=replace(release(102,false),"variant","standard");
        JSONObject byedpi=replace(release(103,true),"variant","byedpi");
        String feed=new JSONArray().put(standard).put(byedpi).toString();
        assertEquals(102,UpdateFeed.parse(feed,REPO,PKG,100,30,CERT,"production","standard").get(0).code);
        assertEquals(103,UpdateFeed.parse(feed,REPO,PKG,100,30,CERT,"production","byedpi").get(0).code);
    }
    @Test public void combinedReleaseSelectsOnlyOwnEdition() throws Exception {
        JSONObject r=release(104,false);
        JSONObject legacy=metadata(r).put("variant","standard");
        JSONObject standard=new JSONObject(legacy.toString());
        JSONObject byedpi=new JSONObject(legacy.toString()).put("variant","byedpi").put("asset","MiloHome-byedpi.apk");
        JSONObject extra=new JSONObject(r.getJSONArray("assets").getJSONObject(0).toString())
            .put("name","MiloHome-byedpi.apk")
            .put("browser_download_url","https://github.com/"+REPO+"/releases/download/v1.0.0/MiloHome-byedpi.apk");
        r.getJSONArray("assets").put(extra);
        legacy.put("editions",new JSONObject().put("standard",standard).put("byedpi",byedpi));
        r.put("body",UpdateFeed.START+legacy+UpdateFeed.END);
        String feed=new JSONArray().put(r).toString();
        assertTrue(UpdateFeed.parse(feed,REPO,PKG,103,30,CERT,"production","standard").get(0).url.endsWith("/MiloHome.apk"));
        assertTrue(UpdateFeed.parse(feed,REPO,PKG,103,30,CERT,"production","byedpi").get(0).url.endsWith("/MiloHome-byedpi.apk"));
        byedpi.put("signerSha256",HASH);
        r.put("body",UpdateFeed.START+legacy+UpdateFeed.END);
        feed=new JSONArray().put(r).toString();
        assertTrue(UpdateFeed.parse(feed,REPO,PKG,103,30,CERT,"production","byedpi").isEmpty());
        assertEquals(1,UpdateFeed.parse(feed,REPO,PKG,103,30,CERT,"production","standard").size());
    }
    @Test public void stableOnly() throws Exception { assertEquals("stable",parse(release(101,false)).get(0).channel); }
    @Test public void rcOnly() throws Exception { assertEquals("rc",parse(release(102,true)).get(0).channel); }
    @Test public void bothChannelsIndependent() throws Exception { assertEquals(2,parse(release(101,false),release(102,true)).size()); }
    @Test public void versionCodeWinsOverReleaseOrder() throws Exception { assertEquals(106,parse(release(106,false),release(101,false)).get(0).code); }
    @Test public void downgradeHiddenAcrossChannels() throws Exception { assertTrue(parse(release(99,true)).isEmpty()); }
    @Test public void draftNeverOffered() throws Exception { assertTrue(parse(release(101,false).put("draft",true)).isEmpty()); }
    @Test public void missingMetadataIgnored() throws Exception { assertTrue(parse(release(101,false).put("body","release notes only")).isEmpty()); }
    @Test public void malformedReleaseDoesNotHideGoodOne() throws Exception { assertEquals(1,parse(new JSONObject(),release(101,false)).size()); }
    @Test public void mismatchedPrereleaseFlagRejected() throws Exception { assertTrue(parse(release(101,true).put("prerelease",false)).isEmpty()); }
    @Test public void wrongPackageRejected() throws Exception { assertTrue(parse(replace(release(101,false),"applicationId","example.fake")).isEmpty()); }
    @Test public void newKeyNotSilentlyAccepted() throws Exception { assertTrue(parse(replace(release(101,false),"signerSha256",HASH)).isEmpty()); }
    @Test public void oldAndroidRejected() throws Exception { assertTrue(parse(replace(release(101,false),"minSdk",31)).isEmpty()); }
    @Test public void testTrackExcludedFromProduction() throws Exception { assertTrue(parse(replace(release(101,false),"track","test")).isEmpty()); }
    @Test public void tagMustMatchRelease() throws Exception { assertTrue(parse(replace(release(101,false),"tag","other")).isEmpty()); }
    @Test public void badDigestRejected() throws Exception { assertTrue(parse(replace(release(101,false),"sha256","short")).isEmpty()); }
    @Test public void missingAssetRejected() throws Exception { assertTrue(parse(release(101,false).put("assets",new JSONArray())).isEmpty()); }
    @Test public void assetSizeMustMatch() throws Exception { assertTrue(parse(replace(release(101,false),"size",101)).isEmpty()); }
    @Test public void githubDigestMustMatch() throws Exception {
        JSONObject r=release(101,false); r.getJSONArray("assets").getJSONObject(0).put("digest","sha256:"+CERT); assertTrue(parse(r).isEmpty());
    }
    @Test public void hostileUrlsRejected() {
        for(String url:new String[]{"http://github.com/"+REPO+"/releases/download/v1/a.apk","https://github.com.evil.test/"+REPO+"/releases/download/v1/a.apk",
                "https://github.com/another/repo/releases/download/v1/a.apk","https://github.com/"+REPO+"/releases/download/../a.apk",
                "https://user@github.com/"+REPO+"/releases/download/v1/a.apk"}) assertFalse(url,UpdateRelease.githubAsset(url,REPO));
    }
}
