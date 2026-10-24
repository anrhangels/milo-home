package com.gathin.tvhome;

import java.net.URI;
import java.util.Locale;

/** Validated, immutable update metadata; APK bytes are verified independently before installation. */
final class UpdateRelease {
    final long code, bytes;
    final int minSdk;
    final String name, channel, sha256, signer, url, notes, date, track, packageName;
    UpdateRelease(long code, long bytes, int minSdk, String name, String channel, String hash,
            String signer, String url, String notes, String date, String track, String packageName) {
        this.code=code; this.bytes=bytes; this.minSdk=minSdk; this.name=name; this.channel=channel;
        this.sha256=hash.toLowerCase(Locale.ROOT); this.signer=signer.toLowerCase(Locale.ROOT);
        this.url=url; this.notes=notes; this.date=date; this.track=track; this.packageName=packageName;
        if(code<=0 || bytes<=0 || bytes>100L*1024*1024 || minSdk<1 || name.length()>80
                || !this.sha256.matches("[a-f0-9]{64}") || !this.signer.matches("[a-f0-9]{64}"))
            throw new IllegalArgumentException("Invalid update metadata");
    }
    boolean compatible(String pkg,long installed,int sdk,String certificate,String wantedTrack) {
        return packageName.equals(pkg) && code>installed && minSdk<=sdk
                && signer.equalsIgnoreCase(certificate) && track.equals(wantedTrack);
    }
    static boolean githubAsset(String url,String repo) {
        try {
            URI uri=new URI(url);
            return "https".equals(uri.getScheme()) && "github.com".equals(uri.getHost())
                && uri.getUserInfo()==null && uri.getPort()==-1 && uri.getQuery()==null
                && uri.getFragment()==null && uri.getRawPath().startsWith("/"+repo+"/releases/download/")
                && !uri.getRawPath().contains("..") && !uri.getRawPath().toLowerCase(Locale.ROOT).contains("%2f");
        } catch(Exception ignored) { return false; }
    }
}
