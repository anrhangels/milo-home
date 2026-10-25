package com.gathin.tvhome;

import android.app.job.JobParameters;
import android.app.job.JobService;

/** Android starts this short-lived service only for an opted-in periodic check. Never downloads APKs. */
public final class UpdateJobService extends JobService {
    private JobParameters running;
    @Override public boolean onStartJob(JobParameters params) {
        if(UpdatePolicy.backgroundInterval(UpdateSchedule.mode(this))==0) return false;
        running=params;
        UpdateManager.get(this).check(false,()->{
            if(running==params) { running=null; jobFinished(params,false); }
        });
        return true;
    }
    @Override public boolean onStopJob(JobParameters params) { running=null; return false; }
}
