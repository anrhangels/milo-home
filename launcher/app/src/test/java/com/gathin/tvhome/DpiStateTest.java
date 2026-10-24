package com.gathin.tvhome;
import org.junit.Test;
import static org.junit.Assert.*;
public class DpiStateTest {
 @Test public void pendingCommandsCannotBeRepeated() {
  for(int state:new int[]{DpiState.STARTING,DpiState.STOPPING,DpiState.CONSENT})
   assertFalse(new DpiState(state,false,false,"").canToggle());
  assertFalse(new DpiState(DpiState.ON,true,true,"").canToggle());
 }
 @Test public void idleAndRunningCanBeChanged() {
  assertTrue(new DpiState(DpiState.ON,true,false,"").canToggle());
  assertTrue(new DpiState(DpiState.OFF,false,false,"").canToggle());
 }
 @Test public void failureDoesNotInventAnOffState() {
  DpiState value=new DpiState(DpiState.ERROR,true,false,"stop timed out");
  assertTrue(value.running); assertEquals(R.string.dpi_error,value.label());
 }
 @Test public void missingPackageIsDistinctFromStopped() {
  assertNotEquals(new DpiState(DpiState.MISSING,false,false,"").label(),new DpiState(DpiState.OFF,false,false,"").label());
 }
}
