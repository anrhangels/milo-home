package com.gathin.tvhome;
import org.junit.Test;
import static org.junit.Assert.*;
public class UpdatePolicyTest {
    @Test public void manualModeNeverFetchesAutomaticallyButAllowsExplicitCheck() {
        assertFalse(UpdatePolicy.due(0,false,100000000,0,0,0));
        assertTrue(UpdatePolicy.due(0,true,100000000,0,0,0));
    }
    @Test public void backgroundModesHaveBoundedIntervalsAndDefaultDoesNotSchedule() {
        assertEquals(0,UpdatePolicy.backgroundInterval(1));
        assertEquals(0,UpdatePolicy.backgroundInterval(0));
        assertEquals(24*UpdatePolicy.HOUR,UpdatePolicy.backgroundInterval(2));
        assertEquals(6*UpdatePolicy.HOUR,UpdatePolicy.backgroundInterval(3));
        assertEquals(1,UpdatePolicy.mode(-9));
    }
    @Test public void successfulChecksRespectSelectedInterval() {
        long now=100*UpdatePolicy.HOUR;
        assertFalse(UpdatePolicy.due(3,false,now,now-5*UpdatePolicy.HOUR,0,0));
        assertTrue(UpdatePolicy.due(3,false,now,now-6*UpdatePolicy.HOUR,0,0));
        assertFalse(UpdatePolicy.due(2,false,now,now-23*UpdatePolicy.HOUR,0,0));
        assertTrue(UpdatePolicy.due(2,false,now,now-24*UpdatePolicy.HOUR,0,0));
    }
    @Test public void manualChecksStillRespectServerBlockAndOneMinuteThrottle() {
        long now=100*UpdatePolicy.HOUR;
        assertFalse(UpdatePolicy.due(0,true,now,0,0,now+1));
        assertFalse(UpdatePolicy.due(0,true,now,0,now-59999,0));
        assertTrue(UpdatePolicy.due(0,true,now,now,now-60000,0));
    }
    @Test public void failedAutomaticChecksBackOffAndClockRollbackDoesNotLockOut() {
        long now=100*UpdatePolicy.HOUR;
        assertFalse(UpdatePolicy.due(3,false,now,0,now-UpdatePolicy.HOUR+1,0));
        assertTrue(UpdatePolicy.due(3,false,now,0,now-UpdatePolicy.HOUR,0));
        assertTrue(UpdatePolicy.due(3,false,now,now+1,now+1,0));
    }
}
