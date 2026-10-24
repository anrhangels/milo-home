package com.gathin.tvhome;

import java.io.*;
import java.net.*;
import java.util.Arrays;

/** HTTPS only, bounded reads and explicit redirect host allowlist; never sends authentication. */
final class UpdateHttp {
    interface ConnectionFactory { java.net.URLConnection create(URL url) throws IOException; }
    // No exported setter or IPC. Instrumentation can substitute a byte stream in its own process;
    // normal release builds always use URL.openConnection (R8 removes the unused indirection).
    private static ConnectionFactory connectionFactory=URL::openConnection;
    static final class RateLimit extends IOException {
        final long retryAt;
        RateLimit(long retryAt) { super("GitHub rate limit; retry later"); this.retryAt=retryAt; }
    }
    static HttpURLConnection open(String address) throws IOException {
        for(int redirect=0;redirect<6;redirect++) {
            URL url=new URL(address);
            if(!"https".equals(url.getProtocol()) || url.getUserInfo()!=null
                    || (url.getPort()!=-1 && url.getPort()!=443)
                    || !Arrays.asList("api.github.com","github.com","release-assets.githubusercontent.com",
                        "objects.githubusercontent.com","github-releases.githubusercontent.com").contains(url.getHost()))
                throw new IOException("Untrusted update host");
            HttpURLConnection c=(HttpURLConnection)connectionFactory.create(url);
            c.setConnectTimeout(8000); c.setReadTimeout(12000); c.setInstanceFollowRedirects(false);
            c.setRequestProperty("User-Agent","MiloHome-Updater/1");
            c.setRequestProperty("Accept",url.getHost().equals("api.github.com")?"application/vnd.github+json":"application/octet-stream");
            int status=c.getResponseCode();
            if(status==301 || status==302 || status==303 || status==307 || status==308) {
                String next=c.getHeaderField("Location"); c.disconnect();
                if(next==null) throw new IOException("Missing redirect");
                address=new URL(url,next).toString(); continue;
            }
            if(status==403 || status==429) {
                long now=System.currentTimeMillis(),retry=now+3600000L;
                try { retry=Math.max(retry,Long.parseLong(c.getHeaderField("X-RateLimit-Reset"))*1000L); } catch(Exception ignored) { }
                try { retry=Math.max(retry,now+Long.parseLong(c.getHeaderField("Retry-After"))*1000L); } catch(Exception ignored) { }
                c.disconnect(); throw new RateLimit(Math.min(now+86400000L,retry));
            }
            if(status!=200) { c.disconnect(); throw new IOException("GitHub HTTP "+status); }
            return c;
        }
        throw new IOException("Too many redirects");
    }
    static String text(String address) throws IOException {
        HttpURLConnection c=open(address);
        try(InputStream in=c.getInputStream(); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[8192]; int n;
            while((n=in.read(buffer))!=-1) {
                if(Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Cancelled");
                if(out.size()+n>2*1024*1024) throw new IOException("Feed too large");
                out.write(buffer,0,n);
            }
            return out.toString("UTF-8");
        } finally { c.disconnect(); }
    }
}
