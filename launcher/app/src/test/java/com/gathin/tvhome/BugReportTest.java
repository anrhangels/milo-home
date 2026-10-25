package com.gathin.tvhome;

import org.junit.Test;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import com.google.zxing.*;
import com.google.zxing.common.*;
import com.google.zxing.qrcode.QRCodeReader;
import static org.junit.Assert.*;

public class BugReportTest {
    private BugReport sample() {return new BugReport("0.0.8",109,true,"Haier","haierATVnippori","11",30,
            951268147L,381891379L,3430547456L);}
    private Map<String,String> query(String url) throws Exception {
        Map<String,String> values=new LinkedHashMap<>();
        for(String part:new URI(url).getRawQuery().split("&")) {
            String[] pair=part.split("=",2);
            assertNull(values.put(URLDecoder.decode(pair[0],"UTF-8"),URLDecoder.decode(pair[1],"UTF-8")));
        }
        return values;
    }
    @Test public void issueUrlHasOneCanonicalDestinationAndOnlyTitleAndBody() throws Exception {
        BugReport report=sample();URI uri=new URI(report.issueUrl());
        assertEquals("https",uri.getScheme());assertEquals("github.com",uri.getHost());
        assertEquals("/anrhangels/milo-home/issues/new",uri.getPath());
        assertNull(uri.getFragment());assertNull(uri.getUserInfo());
        Map<String,String> values=query(report.issueUrl());
        assertEquals(new HashSet<>(Arrays.asList("title","body")),values.keySet());
        assertEquals(report.body(),values.get("body"));
        assertTrue(values.get("body").contains("### Steps to reproduce"));
        assertTrue(values.get("body").contains("Edition: ByeByeDPI"));
    }
    @Test public void deviceStringsCannotInjectFieldsMarkdownOrQueryParameters() throws Exception {
        BugReport report=new BugReport("0.0.8",109,false,"Acme\n### Secret\u202e","TV &body=OTHER#x `evil`","11",30,100,500,0);
        Map<String,String> values=query(report.issueUrl());
        assertEquals(2,values.size());
        assertFalse(report.manufacturer.contains("\n"));assertFalse(report.manufacturer.contains("\u202e"));
        assertFalse(report.model.contains("&"));assertFalse(report.model.contains("`"));
        assertTrue(values.get("body").contains("Edition: Standard"));
        assertEquals(100,report.availableRam);
    }
    @Test public void unavailableMeasurementsStayUnknown() {
        BugReport report=new BugReport(null,109,false,null,"","",30,-1,-1,-1);
        assertEquals("Unknown",report.model);
        assertTrue(report.body().contains("RAM: Unknown total; Unknown available"));
        assertTrue(report.body().contains("Storage available: Unknown"));
        assertEquals("0.0 GiB",BugReport.gib(0));
    }
    @Test public void privacyAllowlistDoesNotContainNetworkAccountsLogsOrPackageInventory() {
        String body=sample().body();
        for(String excluded:new String[]{"SSID","BSSID","serial","account","192.168.","com.google.","logcat","fingerprint"})
            assertFalse(excluded,body.contains(excluded));
        assertTrue(body.startsWith("### Device\n"));
    }
    private String decode(String url) throws Exception {
        BitMatrix bits=BugReportQr.encode(url,600);
        int w=bits.getWidth(),h=bits.getHeight();int[] pixels=new int[w*h];
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)pixels[y*w+x]=bits.get(x,y)?0xff000000:0xffffffff;
        BinaryBitmap bitmap=new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(w,h,pixels)));
        Map<DecodeHintType,Object> hints=new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.PURE_BARCODE,Boolean.TRUE);
        return new QRCodeReader().decode(bitmap,hints).getText();
    }
    @Test public void generatedQrDecodesToTheExactPrefilledUrl() throws Exception {
        assertEquals(sample().issueUrl(),decode(sample().issueUrl()));
    }
    @Test public void longUnicodeNamesRemainBoundedAndScannable() throws Exception {
        String longName=String.join("",Collections.nCopies(200,"電視📺"));
        BugReport report=new BugReport(longName,Integer.MAX_VALUE,false,longName,longName,longName,35,
                Long.MAX_VALUE,Long.MAX_VALUE,Long.MAX_VALUE);
        String url=report.issueUrl();
        assertTrue(url.length()<=1800);
        assertEquals(url,decode(url));
    }
    @Test(expected=IllegalArgumentException.class) public void qrRejectsOtherDestinations() throws Exception {
        BugReportQr.encode("https://example.com/?title=report",500);
    }
}
