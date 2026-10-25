package com.gathin.tvhome;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.EnumMap;
import java.util.Map;

/** Offline QR encoding only. No camera/scanner activity, permissions or networking. */
final class BugReportQr {
    static BitMatrix encode(String url,int size) throws com.google.zxing.WriterException {
        if(!url.startsWith(BugReport.ISSUES+"?title=") || url.length()>1800)
            throw new IllegalArgumentException("Unexpected report URL");
        Map<EncodeHintType,Object> hints=new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN,4);
        hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.M);
        return new QRCodeWriter().encode(url,BarcodeFormat.QR_CODE,Math.max(320,Math.min(640,size)),
                Math.max(320,Math.min(640,size)),hints);
    }
}
