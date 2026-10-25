package com.gathin.tvhome;

/** Same opacity math for the real dock and its settings preview. */
final class DockOpacity {
    static int alpha(int percent,int materialAlpha) {
        return Math.round(Math.max(0,Math.min(255,materialAlpha))*Math.max(30,Math.min(100,percent))/100f);
    }
}
