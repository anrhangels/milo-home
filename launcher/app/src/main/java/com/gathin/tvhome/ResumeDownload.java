package com.gathin.tvhome;
import java.io.IOException;
/** Never append a server's wrong range; a full response deliberately restarts from zero. */
final class ResumeDownload {
 static final class RangeFailure extends IOException{RangeFailure(String message){super(message);}}
 static long start(int status,long requested,String range,long expected,long contentLength)throws IOException{
  if(status==200){if(contentLength>=0 && contentLength!=expected)throw new RangeFailure("APK size changed");return 0;}
  String wanted="bytes "+requested+"-"+(expected-1)+"/"+expected;
  if(status!=206 || requested<=0 || requested>=expected || !wanted.equals(range)
    || (contentLength>=0 && contentLength!=expected-requested))throw new RangeFailure("Invalid APK range");
  return requested;
 }
 static String identity(UpdateRelease r){return r.code+":"+r.sha256+":"+r.bytes;}
}
