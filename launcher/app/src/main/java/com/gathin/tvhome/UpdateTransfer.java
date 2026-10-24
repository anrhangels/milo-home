package com.gathin.tvhome;
import java.io.*;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

/** Byte-accurate transfer independent of UI/network, so truncation/cancellation are reproducibly tested. */
final class UpdateTransfer {
    static long copy(InputStream input,OutputStream output,long expected,BooleanSupplier cancelled,IntConsumer progress) throws IOException {
        if(expected<=0 || expected>100L*1024*1024) throw new IOException("Invalid expected APK size");
        byte[] buffer=new byte[32768]; long total=0,last=0; int n;
        while((n=input.read(buffer))!=-1) {
            if(cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Download cancelled");
            total+=n;
            if(total>expected) throw new IOException("APK exceeds expected size");
            output.write(buffer,0,n);
            long now=System.nanoTime();
            if(now-last>=250_000_000L) { progress.accept((int)(100*total/expected)); last=now; }
        }
        if(cancelled.getAsBoolean()) throw new InterruptedIOException("Download cancelled");
        if(total!=expected) throw new IOException("Incomplete APK");
        progress.accept(100); return total;
    }
}
