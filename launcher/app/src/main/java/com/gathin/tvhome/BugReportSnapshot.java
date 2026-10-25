package com.gathin.tvhome;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;

/** Single local snapshot. Never reads identifiers, accounts, network state, app names or logs. */
final class BugReportSnapshot {
    static BugReport capture(Context context) {
        long total=-1,available=-1,storage=-1;
        try {
            ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            if(manager!=null) {
                ActivityManager.MemoryInfo memory=new ActivityManager.MemoryInfo();
                manager.getMemoryInfo(memory);total=memory.totalMem;available=memory.availMem;
            }
        } catch(RuntimeException unavailable) { /* Display Unknown, not a misleading zero. */ }
        try {storage=new StatFs(context.getFilesDir().getAbsolutePath()).getAvailableBytes();}
        catch(RuntimeException unavailable) { }
        return new BugReport(BuildConfig.VERSION_NAME,BuildConfig.VERSION_CODE,BuildConfig.BYEDPI_ENABLED,
                Build.MANUFACTURER,Build.MODEL,Build.VERSION.RELEASE,Build.VERSION.SDK_INT,total,available,storage);
    }
}
