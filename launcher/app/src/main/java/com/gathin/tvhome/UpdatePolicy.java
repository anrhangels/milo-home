package com.gathin.tvhome;

/** Pure scheduling policy shared by foreground checks and Android's optional background job. */
final class UpdatePolicy {
    static final int MANUAL=0, ON_OPEN=1, DAILY=2, SIX_HOURS=3;
    static final String KEY="milo_update_mode";
    static final long HOUR=3600000L, DAY=24*HOUR;
    static int mode(int value) { return value>=MANUAL && value<=SIX_HOURS?value:ON_OPEN; }
    static long backgroundInterval(int value) {
        return mode(value)==DAILY?DAY:mode(value)==SIX_HOURS?6*HOUR:0;
    }
    static boolean due(int value,boolean manual,long now,long success,long attempt,long blocked) {
        if(!manual && mode(value)==MANUAL) return false;
        if(now<blocked) return false;
        long interval=mode(value)==SIX_HOURS?6*HOUR:DAY;
        if(!manual && success>0 && now>=success && now-success<interval) return false;
        long retry=manual?60000L:HOUR;
        return attempt<=0 || now<attempt || now-attempt>=retry;
    }
}
