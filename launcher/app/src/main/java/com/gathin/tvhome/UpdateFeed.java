package com.gathin.tvhome;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Metadata is embedded in the REST release body, not scraped from GitHub HTML. */
final class UpdateFeed {
    static final String START="<!-- milo-update:v1\n", END="\n-->";
    static List<UpdateRelease> parse(String json,String repo,String pkg,long installed,int sdk,
            String certificate,String track) throws Exception {
        if(json.length()>2*1024*1024) throw new IllegalArgumentException("Feed too large");
        JSONArray releases=new JSONArray(json);
        UpdateRelease stable=null,rc=null;
        for(int i=0;i<releases.length();i++) {
            try {
                JSONObject release=releases.getJSONObject(i);
                if(release.optBoolean("draft",false)) continue;
                String body=release.optString("body","");
                int start=body.indexOf(START),end=start<0?-1:body.indexOf(END,start+START.length());
                if(start<0 || end<0 || end-start>32768) continue;
                JSONObject m=new JSONObject(body.substring(start+START.length(),end));
                if(m.getInt("schema")!=1) continue;
                boolean prerelease=release.optBoolean("prerelease",false);
                String channel=m.getString("channel");
                if(!channel.equals(prerelease?"rc":"stable")) continue;
                if(!m.getString("tag").equals(release.getString("tag_name"))) continue;
                String asset=m.getString("asset"),url=null;
                JSONArray assets=release.getJSONArray("assets");
                for(int a=0;a<assets.length();a++) {
                    JSONObject candidate=assets.getJSONObject(a);
                    if(asset.equals(candidate.getString("name")) && "uploaded".equals(candidate.optString("state","uploaded"))
                            && candidate.getLong("size")==m.getLong("size")) {
                        String digest=candidate.optString("digest","");
                        if(!digest.isEmpty() && !digest.equalsIgnoreCase("sha256:"+m.getString("sha256"))) continue;
                        url=candidate.getString("browser_download_url"); break;
                    }
                }
                if(url==null || !UpdateRelease.githubAsset(url,repo)) continue;
                if(!asset.matches("[A-Za-z0-9_-][A-Za-z0-9._-]*\\.apk")
                        || !url.equals("https://github.com/"+repo+"/releases/download/"+release.getString("tag_name")+"/"+asset)) continue;
                String notes=m.optString("notes","");
                if(notes.length()>600) notes=notes.substring(0,600);
                UpdateRelease value=new UpdateRelease(m.getLong("versionCode"),m.getLong("size"),
                    m.getInt("minSdk"),m.getString("versionName"),channel,m.getString("sha256"),
                    m.getString("signerSha256"),url,notes,release.optString("published_at",""),
                    m.getString("track"),m.getString("applicationId"));
                if(!value.compatible(pkg,installed,sdk,certificate,track)) continue;
                if(channel.equals("stable") && (stable==null || value.code>stable.code)) stable=value;
                if(channel.equals("rc") && (rc==null || value.code>rc.code)) rc=value;
            } catch(Exception malformed) { /* Ignore a single unrelated or malformed release. */ }
        }
        List<UpdateRelease> result=new ArrayList<>();
        if(stable!=null) result.add(stable);
        if(rc!=null) result.add(rc);
        return result;
    }
}
