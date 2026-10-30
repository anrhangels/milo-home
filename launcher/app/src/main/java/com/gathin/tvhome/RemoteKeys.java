package com.gathin.tvhome;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ComponentName;
import android.os.Handler;
import android.os.Looper;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import android.content.Intent;
import android.media.tv.TvContract;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Remote buttons that only the stock launcher answered, for InputKeyService and HomeKeyService: app_keys
 * (config.xml or a theme) maps a keycode to the app it opens, as "190:com.google.android.youtube.tv" or
 * "KEYCODE_BUTTON_3:com.google.android.youtube.tv", or a scancode as "scan:490:com.wbd.hbomax". A "hold:"
 * prefix makes an entry the long-press action instead: "hold:KEYCODE_BUTTON_8:org.xbmc.kodi" opens Kodi when
 * that button is held for HOLD_MS, while the unprefixed entry still opens on a tap; "hold:scan:490:..." does
 * the same for a scancode. The shared press tracker answers each event once across both services.
 * A target of "input:<input id>" switches to that TV input (an HDMI port) instead of opening an app.
 */
final class RemoteKeys {

    /** How long a button is held before its "hold:" entry wins (Android's long-press timeout). */
    static final long HOLD_MS = 500;

    private RemoteKeys() { }
    private static final Pattern NUMERIC_KEY=Pattern.compile("[0-9]{1,6}");
    private static final Pattern KEYCODE_KEY=Pattern.compile("KEYCODE_[A-Z0-9_]+");
    private static Presses presses;

    /** keycode -> package, with key names resolved by Android. */
    static Map<Integer, String> parseAppKeys(String[] entries) {
        return parseAppKeys(Arrays.asList(entries), KeyEvent::keyCodeFromString);
    }

    static Map<Integer, String> parseAppKeys(Iterable<String> entries) {
        return parseAppKeys(entries, KeyEvent::keyCodeFromString);
    }

    /**
     * keycode -> package. The key is a number or a KEYCODE_ name (resolved by {@code names}; 0, KEYCODE_UNKNOWN,
     * means it isn't one). Entries without a colon, with an unknown key or with no package are skipped; for a
     * keycode listed twice the last entry wins.
     */
    static Map<Integer, String> parseAppKeys(Iterable<String> entries, ToIntFunction<String> names) {
        Map<Integer, String> keys = new HashMap<>();
        for (String entry : entries) {
            if (entry == null) continue;
            String[] parts = entry.split(":", 2);
            if (parts.length != 2 || parts[1].trim().isEmpty()) continue;
            String key = parts[0].trim();
            int code;
            if (NUMERIC_KEY.matcher(key).matches()) {
                code = Integer.parseInt(key);
            } else if (KEYCODE_KEY.matcher(key).matches()) {
                code = names.applyAsInt(key);   // keyCodeFromString takes digits only from API 29, names always
            } else {
                continue;
            }
            if (code > 0) keys.put(code, parts[1].trim());
        }
        return keys;
    }

    /** keycode -> package from the "hold:" entries; the rest of each entry reads as it does in parseAppKeys. */
    static Map<Integer, String> parseHoldAppKeys(String[] entries) {
        return parseHoldAppKeys(Arrays.asList(entries), KeyEvent::keyCodeFromString);
    }

    static Map<Integer, String> parseHoldAppKeys(Iterable<String> entries) {
        return parseHoldAppKeys(entries, KeyEvent::keyCodeFromString);
    }

    static Map<Integer, String> parseHoldAppKeys(Iterable<String> entries, ToIntFunction<String> names) {
        return parseAppKeys(holdEntries(entries), names);
    }

    /**
     * Applies the key_filter choice (config.xml or a theme): when false the service drops
     * FLAG_REQUEST_FILTER_KEY_EVENTS at runtime, so it stays enabled for nothing but stops filtering.
     * Some firmware (TCL Google TV on Android 12) drops key auto-repeat once ANY service filters keys:
     * long-pressing volume then steps once per press. Filtering can only be skipped while the service
     * runs, since the flag is also declared in the manifest metadata.
     */
    static void applyKeyFilter(AccessibilityService service, boolean keyFilter) {
        if (keyFilter) return;
        AccessibilityServiceInfo info = service.getServiceInfo();
        if (info == null) return;
        info.flags &= ~AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        service.setServiceInfo(info);
    }

    /**
     * scancode -> package, from the "scan:490:com.wbd.hbomax" entries of the same list. For remotes that send
     * several app buttons as one keycode and tell them apart by scancode (Sony BRAVIA: YouTube, Prime Video and
     * Disney+ are all keycode 196, scancodes 485, 490 and 491). Find it with `getevent -lt` while pressing the
     * button: the EV_KEY code, in hex. Other entries are skipped; for a scancode listed twice the last entry wins.
     */
    static Map<Integer, String> parseAppScancodes(Iterable<String> entries) {
        Map<Integer, String> keys = new HashMap<>();
        for (String entry : entries) {
            if (entry == null) continue;
            String[] parts = entry.split(":", 3);
            if (parts.length != 3 || !parts[0].trim().equals("scan") || parts[2].trim().isEmpty()) continue;
            String code = parts[1].trim();
            if (!NUMERIC_KEY.matcher(code).matches()) continue;
            int scan = Integer.parseInt(code);
            if (scan > 0) keys.put(scan, parts[2].trim());
        }
        return keys;
    }

    /** scancode -> package from the "hold:scan:..." entries; the rest reads as it does in parseAppScancodes. */
    static Map<Integer, String> parseHoldAppScancodes(Iterable<String> entries) {
        return parseAppScancodes(holdEntries(entries));
    }

    /** The entries with the "hold:" prefix removed, so the normal parsers can read them. */
    private static Iterable<String> holdEntries(Iterable<String> entries) {
        List<String> holds = new ArrayList<>();
        for (String entry : entries) {
            if (entry == null) continue;
            String text = entry.trim();
            if (text.startsWith("hold:")) holds.add(text.substring(5).trim());
        }
        return holds;
    }

    /**
     * A hold fires after HOLD_MS, a tap on release. Shared tracking prevents duplicate service launches.
     */
    static boolean handle(Context ctx, KeyEvent event, String app, String holdApp) {
        if (app == null && holdApp == null) return false;
        if (presses == null) {
            Handler handler = new Handler(Looper.getMainLooper());
            presses = new Presses(new Presses.Timer() {
                @Override public void post(Runnable hold, long delayMs) { handler.postDelayed(hold, delayMs); }
                @Override public void cancel(Runnable hold) { handler.removeCallbacks(hold); }
            });
        }
        Context appContext=ctx.getApplicationContext();
        Consumer<String> open=target->{
            android.os.PowerManager power=appContext.getSystemService(android.os.PowerManager.class);
            if(power!=null && power.isInteractive()) launch(appContext,target);
        };
        String button = event.getDeviceId() + ":" + event.getKeyCode() + ":" + event.getScanCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            presses.down(button, event.getEventTime(), event.getRepeatCount(), holdApp, ctx, open);
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            presses.up(button, event.getEventTime(), event.getDownTime(), event.isCanceled(), app, holdApp,
                    open);
        }
        return true;
    }

    /**
     * Each button's presses, kept per device, keycode and scancode: KeyEvent.getDownTime() can't tell presses apart,
     * since a key pressed together with another can report the other's. Both services get every event, so a DOWN
     * or UP whose event time was already seen for that button is the other service's copy and is skipped. One small
     * record per mapped button that has been pressed, not a history.
     */
    static void release(Context helper) { if (presses != null) presses.release(helper); }

    static final class Presses {
        interface Timer {
            void post(Runnable hold, long delayMs);
            void cancel(Runnable hold);
        }

        private static final class Press {
            long downAt = -1, upAt = -1;
            boolean fired;
            Runnable hold;
            /** The helpers that saw this DOWN while its hold is pending, each with the way it opens a target. */
            final Map<Object, Consumer<String>> owners = new LinkedHashMap<>();
        }

        private final Map<String, Press> buttons = new HashMap<>();
        private final Timer timer;

        Presses(Timer timer) {
            this.timer = timer;
        }

        void down(String button, long eventTime, int repeatCount, String holdApp, Consumer<String> open) {
            down(button,eventTime,repeatCount,holdApp,this,open);
        }

        void down(String button, long eventTime, int repeatCount, String holdApp, Object owner,
                Consumer<String> open) {
            if (repeatCount != 0) return;
            Press press = buttons.computeIfAbsent(button, b -> new Press());
            if (press.downAt == eventTime) {
                // The other helper's copy: it can answer the hold too, should the first one leave.
                if (press.hold != null) press.owners.putIfAbsent(owner, open);
                return;
            }
            if (press.hold != null) timer.cancel(press.hold);   // its UP never came
            press.downAt = eventTime;
            press.fired = false;
            press.hold = null;
            press.owners.clear();
            if (holdApp == null) return;
            press.owners.put(owner, open);
            press.hold = () -> {
                Consumer<String> first = press.owners.values().iterator().next();
                press.hold = null;
                press.owners.clear();
                press.fired = true;
                first.accept(holdApp);
            };
            timer.post(press.hold, HOLD_MS);
        }

        /** Drops this helper from every pending hold, cancelling a hold it was the last to own. */
        void release(Object owner) {
            for (Press press : buttons.values()) {
                if (press.hold == null || press.owners.remove(owner) == null || !press.owners.isEmpty()) continue;
                timer.cancel(press.hold);
                press.hold = null;
                press.fired = true;   // the press is over: an UP that still comes later opens nothing
            }
        }

        void up(String button, long eventTime, long downTime, boolean canceled, String app, String holdApp,
                Consumer<String> open) {
            Press press = buttons.computeIfAbsent(button, b -> new Press());
            if (press.upAt == eventTime) return;
            boolean sawDown = press.downAt > press.upAt;
            press.upAt = eventTime;
            if (press.hold != null) {
                timer.cancel(press.hold);
                press.hold = null;
                press.owners.clear();
            }
            if (canceled || (sawDown && press.fired)) return;
            // Without its DOWN (the service started while the button was held), the event's own down time will do.
            open.accept(target(app, holdApp, eventTime - (sawDown ? press.downAt : downTime)));
        }
    }

    /** The app a mapped key opens: its hold entry after HOLD_MS, otherwise its normal one. */
    static String target(String app, String holdApp, long heldMs) {
        return heldMs >= HOLD_MS && holdApp != null ? holdApp : app;
    }

    /** The TV input id of an "input:<id>" target, e.g. "input:com.tcl.tvinput/.passthroughinput.TvPassThroughService/HW1413743104"; null for a package. */
    static String inputId(String target) {
        if (target == null || !target.startsWith("input:")) return null;
        String id = target.substring("input:".length()).trim();
        return id.isEmpty() ? null : id;
    }

    static boolean isActivity(String target) {
        return target != null && inputId(target) == null && target.indexOf('/') > 0;
    }

    /** Opens an app from a remote button: its TV entry if it has one, else its phone entry. An "input:<id>" target
     *  switches to that TV input (an HDMI port) instead. */
    static void launch(Context ctx, String pkg) {
        if (pkg == null) return;
        String inputId = inputId(pkg);
        if (inputId != null) {
            // The same passthrough URI the Inputs menu opens, which the Live TV app handles.
            Intent view = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(TvContract.buildChannelUriForPassthroughInput(inputId),
                            "vnd.android.cursor.item/channel")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                ctx.startActivity(view);
            } catch (ActivityNotFoundException | SecurityException ignored) {
            }
            return;
        }
        ComponentName activity=isActivity(pkg)?ComponentName.unflattenFromString(pkg):null;
        if(activity!=null) {
            try {ctx.startActivity(new Intent().setComponent(activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}
            catch(ActivityNotFoundException | SecurityException ignored) {}
            return;
        }
        Intent open = ctx.getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
        if (open == null) open = ctx.getPackageManager().getLaunchIntentForPackage(pkg);
        if (open == null) return;
        try {
            ctx.startActivity(open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException | SecurityException ignored) {
        }
    }
}
