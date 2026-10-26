package com.gathin.tvhome;
/** Persisted screensaver appearance; unknown values retain the existing ambient style. */
final class ClockBackdrop {
    static final String KEY="milo_clock_backdrop";
    static final int AMBIENT=0, BLACK=1;
    static int normalize(int value) { return value==BLACK?BLACK:AMBIENT; }
    private ClockBackdrop() {}
}
