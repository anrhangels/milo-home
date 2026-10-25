package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** On-demand panel; no polling, background service or privileged power control. */
public final class QuickSettingsActivity extends Activity {
    private AudioManager audio;
    private SeekBar volume;
    
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_quick_settings);
        findViewById(R.id.scrim).setOnClickListener(v -> finish());
        findViewById(R.id.scrim).setFocusable(false);
        LinearLayout rows = findViewById(R.id.rows);
        TextView heading = new TextView(this);
        heading.setText(R.string.settings); heading.setTextSize(26); heading.setTextColor(-1);
        heading.setTypeface(UiFont.get(this,R.string.font_medium));
        heading.setPadding(dp(8),dp(2),0,dp(18)); rows.addView(heading);
        LinearLayout first = grid(rows), second = grid(rows);
        View wifi = tile(first,R.string.milo_wifi,R.drawable.ic_wifi,
            () -> launch(new Intent(this,MainActivity.class).setAction(MainActivity.ACTION_NETWORK_PANEL)));
        View bt = tile(first,R.string.milo_bluetooth,R.drawable.ic_milo_bluetooth,
            () -> launch(SystemSettings.intent(this,Settings.ACTION_BLUETOOTH_SETTINGS)));
        View inputs = tile(second,R.string.inputs,R.drawable.ic_input,
            () -> launch(new Intent(this,MainActivity.class).setAction(MainActivity.ACTION_VIEW_INPUTS)));
        View dark = tile(second,R.string.milo_dark_screen,R.drawable.ic_milo_power,
            () -> new android.app.AlertDialog.Builder(this).setTitle(R.string.milo_dark_screen)
                .setMessage(R.string.milo_dark_hint).setPositiveButton(android.R.string.ok,(d,w) ->
                    launch(new Intent(this,ClockActivity.class).putExtra("dark_screen",true)))
                .setNegativeButton(android.R.string.cancel,null).show());
        audio = (AudioManager)getSystemService(AUDIO_SERVICE);
        volume = new VolumeSlider(this); volume.setId(View.generateViewId());
        volume.setContentDescription(getString(R.string.milo_volume));
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,dp(54));
        vp.setMargins(dp(4),dp(16),dp(4),dp(12)); rows.addView(volume,vp);
        volume.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volume.setEnabled(!audio.isVolumeFixed()); refreshVolume();
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar,int value,boolean user) {
                if(user) { try { audio.setStreamVolume(AudioManager.STREAM_MUSIC,value,0); }
                    catch(SecurityException ignored) {} refreshVolume(); }
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
        TextView launcher = row(rows,R.string.milo_launcher_settings,R.drawable.ic_home_settings,
            () -> launch(new Intent(this,MainActivity.class).setAction(MainActivity.ACTION_LAUNCHER_SETTINGS)));
        TextView all = row(rows,R.string.milo_all_settings,R.drawable.ic_settings,
            () -> launch(SystemSettings.intent(this,Settings.ACTION_SETTINGS)));
        View target = volume.isEnabled()?volume:launcher;
        link(wifi,wifi,bt,wifi,inputs); link(bt,wifi,bt,bt,dark);
        link(inputs,inputs,dark,wifi,target); link(dark,inputs,dark,bt,target);
        link(volume,volume,volume,inputs,launcher);
        link(launcher,launcher,launcher,volume.isEnabled()?volume:inputs,all);
        link(all,all,all,launcher,all);
        wifi.requestFocus();
        View panel=findViewById(R.id.panel); panel.setTranslationX(dp(32)); panel.setAlpha(0);
        panel.animate().translationX(0).alpha(1).setDuration(180).start();
    }
    private LinearLayout grid(LinearLayout parent) {
        LinearLayout line=new LinearLayout(this);
        parent.addView(line,new LinearLayout.LayoutParams(-1,dp(82))); return line;
    }
    private View tile(LinearLayout parent,int title,int icon,Runnable action) {
        LinearLayout tile=new LinearLayout(this);
        tile.setId(View.generateViewId()); tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setPadding(dp(12),0,dp(8),0); tile.setBackgroundResource(R.drawable.milo_quick_tile);
        tile.setFocusableInTouchMode(true); tile.setFocusable(true); tile.setClickable(true);
        tile.setContentDescription(getString(title)); tile.setOnClickListener(v->action.run());
        ImageView badge=new ImageView(this);
        badge.setImageResource(icon); badge.setImageTintList(getColorStateList(R.color.milo_quick_text));
        badge.setBackgroundResource(R.drawable.milo_quick_badge); badge.setDuplicateParentStateEnabled(true);
        badge.setPadding(dp(10),dp(10),dp(10),dp(10));
        tile.addView(badge,new LinearLayout.LayoutParams(dp(40),dp(40)));
        TextView text=new TextView(this); text.setText(title); text.setTextSize(15);
        text.setTypeface(UiFont.get(this,R.string.font_medium));
        text.setTextColor(getColorStateList(R.color.milo_quick_text)); text.setDuplicateParentStateEnabled(true);
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1); tp.setMarginStart(dp(10));
        tile.addView(text,tp);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);
        lp.setMargins(dp(4),dp(4),dp(4),dp(4)); parent.addView(tile,lp); return tile;
    }
    private TextView row(LinearLayout parent,int title,int icon,Runnable action) {
        TextView view=button(title,icon,action);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));
        p.setMargins(dp(4),dp(8),dp(4),0); parent.addView(view,p); return view;
    }
    private TextView button(int title,int icon,Runnable action) {
        TextView v=new TextView(this); v.setId(View.generateViewId()); v.setText(title);
        v.setTextSize(16); v.setTypeface(UiFont.get(this,R.string.font_regular)); v.setTextColor(getColorStateList(R.color.milo_quick_text));
        v.setBackgroundResource(R.drawable.milo_quick_tile); v.setPadding(dp(14),0,dp(10),0);
        v.setGravity(Gravity.CENTER_VERTICAL);
        android.graphics.drawable.Drawable drawable=getDrawable(icon);
        drawable.setBounds(0,0,dp(22),dp(22));
        v.setCompoundDrawablesRelative(drawable,null,null,null);
        v.setCompoundDrawableTintList(getColorStateList(R.color.milo_quick_text));
        v.setCompoundDrawablePadding(dp(10)); v.setFocusableInTouchMode(true); v.setFocusable(true); v.setClickable(true);
        v.setOnClickListener(w -> action.run()); return v;
    }
    private void link(View v,View l,View r,View u,View d) {
        v.setNextFocusLeftId(l.getId()); v.setNextFocusRightId(r.getId());
        v.setNextFocusUpId(u.getId()); v.setNextFocusDownId(d.getId());
    }
    private void refreshVolume() {
        int value=audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume.setProgress(value);
        volume.setContentDescription(getString(R.string.milo_volume)+", "+Math.round(100f*value/Math.max(1,volume.getMax()))+"%");
    }
    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        if(RemoteInput.drop(e))return true;
        int key=e.getKeyCode();
        if(key==KeyEvent.KEYCODE_VOLUME_UP || key==KeyEvent.KEYCODE_VOLUME_DOWN || key==KeyEvent.KEYCODE_VOLUME_MUTE) {
            if(e.getAction()==KeyEvent.ACTION_DOWN) {
                try { audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,key==KeyEvent.KEYCODE_VOLUME_UP?
                    AudioManager.ADJUST_RAISE:key==KeyEvent.KEYCODE_VOLUME_DOWN?
                    AudioManager.ADJUST_LOWER:AudioManager.ADJUST_TOGGLE_MUTE,0); }
                catch(SecurityException ignored) {}
                refreshVolume();
            } return true;
        } return super.dispatchKeyEvent(e);
    }
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private void launch(Intent intent) {
        try { startActivity(intent); }
        catch(android.content.ActivityNotFoundException|SecurityException e) {
            Toast.makeText(this,R.string.not_available,Toast.LENGTH_SHORT).show();
        }
    }
    @Override protected void onNewIntent(Intent i) { super.onNewIntent(i); finish(); }
    @Override protected void onPause() {
        findViewById(R.id.panel).animate().cancel(); super.onPause(); finish(); overridePendingTransition(0,0);
    }
}
