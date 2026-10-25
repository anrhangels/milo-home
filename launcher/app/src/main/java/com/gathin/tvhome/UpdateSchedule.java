package com.gathin.tvhome;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

/** Only the two explicit background modes register a network-constrained system job. */
final class UpdateSchedule {
    static final int JOB=60417;
    static int mode(Context context) {
        return UpdatePolicy.mode(context.getSharedPreferences(Config.SETTINGS_NAME,0)
                .getInt(UpdatePolicy.KEY,UpdatePolicy.ON_OPEN));
    }
    static void sync(Context context) {
        JobScheduler scheduler=(JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if(scheduler==null) return;
        long interval=UpdatePolicy.backgroundInterval(mode(context));
        if(interval==0) { scheduler.cancel(JOB); return; }
        JobInfo previous=scheduler.getPendingJob(JOB);
        if(previous!=null && previous.getIntervalMillis()==interval) return;
        scheduler.schedule(new JobInfo.Builder(JOB,new ComponentName(context,UpdateJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(interval)
                .setBackoffCriteria(UpdatePolicy.HOUR,JobInfo.BACKOFF_POLICY_LINEAR)
                .build());
    }
}
