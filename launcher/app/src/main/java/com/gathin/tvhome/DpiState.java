package com.gathin.tvhome;
/** Wire values in protocol v1; running is independently confirmed by the engine. */
final class DpiState {
 static final int OFF=0, STARTING=1, ON=2, STOPPING=3, ERROR=4, CONSENT=5, MISSING=6;
 final int state; final boolean running, busy; final String error;
 DpiState(int state,boolean running,boolean busy,String error) {
  this.state=state; this.running=running; this.busy=busy; this.error=error;
 }
 boolean canToggle() { return !busy && state!=STARTING && state!=STOPPING && state!=CONSENT; }
 int label() {
  switch(state) {
   case OFF: return R.string.dpi_off;
   case STARTING: return R.string.dpi_starting;
   case ON: return R.string.dpi_on;
   case STOPPING: return R.string.dpi_stopping;
   case MISSING: return R.string.dpi_missing;
   default: return R.string.dpi_error;
  }
 }
}
