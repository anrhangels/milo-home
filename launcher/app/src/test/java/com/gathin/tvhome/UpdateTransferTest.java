package com.gathin.tvhome;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class UpdateTransferTest {
    @Test public void progressComesFromReceivedBytes() throws Exception {
        byte[] bytes=new byte[100000]; Arrays.fill(bytes,(byte)42);
        ByteArrayOutputStream out=new ByteArrayOutputStream(); List<Integer> progress=new ArrayList<>();
        assertEquals(bytes.length,UpdateTransfer.copy(new ByteArrayInputStream(bytes),out,bytes.length,()->false,progress::add));
        assertArrayEquals(bytes,out.toByteArray()); assertEquals(Integer.valueOf(100),progress.get(progress.size()-1));
        for(int value:progress) assertTrue(value>=0 && value<=100);
    }
    @Test(expected=IOException.class) public void truncatedDownloadCannotInstall() throws Exception {
        UpdateTransfer.copy(new ByteArrayInputStream(new byte[10]),new ByteArrayOutputStream(),20,()->false,p->{});
    }
    @Test(expected=IOException.class) public void oversizedDownloadRejected() throws Exception {
        UpdateTransfer.copy(new ByteArrayInputStream(new byte[20]),new ByteArrayOutputStream(),10,()->false,p->{});
    }
    @Test(expected=InterruptedIOException.class) public void cancellationStopsWriting() throws Exception {
        UpdateTransfer.copy(new ByteArrayInputStream(new byte[10]),new ByteArrayOutputStream(),10,()->true,p->{});
    }
    @Test(expected=IOException.class) public void unknownSizeNotGivenFakePercent() throws Exception {
        UpdateTransfer.copy(new ByteArrayInputStream(new byte[10]),new ByteArrayOutputStream(),-1,()->false,p->{});
    }
    @Test(expected=IOException.class) public void diskWriteFailurePropagates() throws Exception {
        UpdateTransfer.copy(new ByteArrayInputStream(new byte[10]),new OutputStream(){public void write(int b)throws IOException {throw new IOException("disk full");}},10,()->false,p->{});
    }
    @Test(expected=IOException.class) public void networkFailurePropagates() throws Exception {
        UpdateTransfer.copy(new InputStream(){public int read()throws IOException {throw new IOException("offline");}},new ByteArrayOutputStream(),10,()->false,p->{});
    }
}
