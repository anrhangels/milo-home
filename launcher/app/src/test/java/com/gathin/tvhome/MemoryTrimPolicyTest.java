package com.gathin.tvhome;
import org.junit.Test;
import static org.junit.Assert.*;
public class MemoryTrimPolicyTest {
 @Test public void visibleHomeNeverLosesTiles(){for(int n:new int[]{5,10,15,20,40,60,80})assertFalse(MemoryTrimPolicy.shouldRelease(n,true));}
 @Test public void ordinaryBackgroundReturnRetainsCache(){assertFalse(MemoryTrimPolicy.shouldRelease(20,false));assertFalse(MemoryTrimPolicy.shouldRelease(5,false));}
 @Test public void hiddenMemoryPressureReleases(){for(int n:new int[]{10,15,40,60,80})assertTrue(MemoryTrimPolicy.shouldRelease(n,false));}
}
