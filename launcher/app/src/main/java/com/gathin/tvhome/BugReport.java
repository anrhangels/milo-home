package com.gathin.tvhome;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Fixed allowlist of non-identifying device facts. No log, package inventory or arbitrary extras. */
final class BugReport {
    static final String ISSUES="https://github.com/anrhangels/milo-home/issues/new";
    final String version,manufacturer,model,android;
    final int code,api;
    final boolean byedpi;
    final long totalRam,availableRam,availableStorage;
    BugReport(String version,int code,boolean byedpi,String manufacturer,String model,String android,int api,
              long totalRam,long availableRam,long availableStorage) {
        this.version=clean(version,24);this.code=code;this.byedpi=byedpi;
        this.manufacturer=clean(manufacturer,32);this.model=clean(model,48);this.android=clean(android,16);this.api=api;
        this.totalRam=totalRam>0?totalRam:-1;
        this.availableRam=availableRam>=0 && this.totalRam>0?Math.min(availableRam,this.totalRam):-1;
        this.availableStorage=availableStorage>=0?availableStorage:-1;
    }
    static String clean(String value,int limit) {
        if(value==null)return "Unknown";
        StringBuilder out=new StringBuilder();
        int count=0;
        for(int at=0;at<value.length() && count<limit;) {
            int c=value.codePointAt(at);at+=Character.charCount(c);
            if(Character.isLetterOrDigit(c) || "._-+/".indexOf(c)>=0) {
                out.appendCodePoint(c);count++;
            } else if(out.length()>0 && out.charAt(out.length()-1)!=' ') {
                out.append(' ');count++;
            }
        }
        String result=out.toString().trim();
        return result.isEmpty()?"Unknown":result;
    }
    static String mib(long value) {return value<0?"Unknown":String.format(Locale.ROOT,"%.0f MiB",value/1048576d);}
    static String gib(long value) {return value<0?"Unknown":String.format(Locale.ROOT,"%.1f GiB",value/1073741824d);}
    String body() {
        return "### Device\n"
            +"Milo Home: "+version+" ("+code+")\n"
            +"Edition: "+(byedpi?"ByeByeDPI":"Standard")+"\n"
            +"Manufacturer: "+manufacturer+"\nModel: "+model+"\n"
            +"Android: "+android+" (API "+api+")\n"
            +"RAM: "+mib(totalRam)+" total; "+mib(availableRam)+" available\n"
            +"Storage available: "+gib(availableStorage)+"\n"
            +"\n### What happened?\n\n"
            +"### Expected behaviour\n\n"
            +"### Steps to reproduce\n\n";
    }
    String issueUrl() {
        String url=ISSUES+"?title="+encode("[Bug] Milo Home "+version)+"&body="+encode(body());
        if(url.length()>1800)throw new IllegalStateException("Report link exceeds QR budget");
        return url;
    }
    private static String encode(String text) {
        try {return URLEncoder.encode(text,StandardCharsets.UTF_8.name());}
        catch(java.io.UnsupportedEncodingException impossible){throw new AssertionError(impossible);}
    }
}
