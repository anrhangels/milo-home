package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.util.*;

/** Two-pane TV settings: D-pad sections, saved controls and an always-visible lightweight preview. */
public final class LauncherSettingsActivity extends Activity implements UpdateManager.Listener {
    static final String EXTRA_REORDER="com.gathin.tvhome.extra.REORDER_FROM_SETTINGS";
    static final String EXTRA_HOME_ACTION="com.gathin.tvhome.extra.SETTINGS_ACTION";
    private SharedPreferences choices;
    private Config config;
    private LinearLayout content;
    private ScrollView scroll;
    private SettingsPreview preview;
    private TextView previewHint,updateStatus;
    private final TextView[] navigation=new TextView[6];
    private final List<Runnable> bindings=new ArrayList<>();
    private View first;
    private int page=-1;
    private UpdateManager updates;
    private static final String[] APPEARANCE_KEYS={"columns",Config.ROWS_BELOW_DOCK_KEY,Config.DOCK_OPACITY_KEY,
        Config.WALLPAPER_CHOICE_KEY,"animations","advanced_effects","milo_names","milo_dock_title",
        "milo_wifi_button","milo_inputs_button","milo_library_button","milo_clock_bold","milo_clock_brightness","milo_idle_minutes"};

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_launcher_settings);
        choices=getSharedPreferences(Config.SETTINGS_NAME,MODE_PRIVATE);
        config=Config.load(this);
        updates=UpdateManager.get(this);
        content=findViewById(R.id.settingsContent);
        scroll=findViewById(R.id.settingsScroll);
        preview=findViewById(R.id.settingsPreview);
        previewHint=findViewById(R.id.settingsPreviewHint);
        LinearLayout nav=findViewById(R.id.settingsNavigation);
        int[] labels={R.string.ms_appearance,R.string.ms_home,R.string.milo_screensaver,
            R.string.ms_motion,R.string.ms_updates,R.string.ms_system};
        for(int i=0;i<labels.length;i++) {
            final int section=i;
            TextView item=text(getString(labels[i]),15,true);
            item.setId(View.generateViewId()); item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(14),0,dp(8),0);
            item.setBackgroundResource(R.drawable.milo_settings_choice);
            item.setTextColor(getColorStateList(R.color.milo_quick_text));
            item.setFocusableInTouchMode(true); item.setFocusable(true); item.setClickable(true);
            item.setOnClickListener(v->{if(page!=section) render(section); else if(first!=null) first.requestFocus();});
            item.setOnFocusChangeListener((v,focused)->{if(focused && page!=section) render(section);});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(45)); lp.bottomMargin=dp(7);
            nav.addView(item,lp); navigation[i]=item;
        }
        int initial=state==null?0:Math.max(0,Math.min(5,state.getInt("page",0)));
        render(initial); navigation[initial].requestFocus();
    }
    @Override protected void onResume() { super.onResume(); refresh(); updates.attach(this); }
    @Override protected void onPause() { updates.detach(this); preview.animate().cancel(); preview.setAlpha(1f); super.onPause(); }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out);out.putInt("page",page); }
    @Override public void changed() { updateStatus(); }
    @Override public void ready(File file) { /* Installation is initiated only from the Home update button. */ }

    private void render(int section) {
        page=section; content.removeAllViews(); bindings.clear(); first=null; updateStatus=null;
        for(int i=0;i<navigation.length;i++) navigation[i].setSelected(i==page);
        if(page==0) appearance();
        else if(page==1) home();
        else if(page==2) screensaver();
        else if(page==3) motion();
        else if(page==4) updater();
        else more();
        if(first!=null) navigation[page].setNextFocusRightId(first.getId());
        scroll.scrollTo(0,0); refresh();
    }
    private void appearance() {
        chips(R.string.columns_label,R.string.ms_columns_hint,new String[]{"4","5","6","7"},
            new int[]{R.id.columns4,R.id.columns5,R.id.columns6,R.id.columns7},
            ()->config.columns-4,i->save("columns",i+4));
        chips(R.string.rows_below_dock_label,R.string.ms_rows_hint,new String[]{"0","1","2","3"},
            new int[]{R.id.rows0,R.id.rows1,R.id.rows2,R.id.rows3},
            ()->config.rowsBelowDock,i->save(Config.ROWS_BELOW_DOCK_KEY,i));
        int[] opacity={30,50,70,100};
        chips(R.string.dock_opacity_label,R.string.ms_opacity_hint,new String[]{"30%","50%","70%","100%"},
            new int[]{R.id.dock30,R.id.dock50,R.id.dock70,R.id.dock100},
            ()->Arrays.asList(30,50,70,100).indexOf(config.dockOpacity),i->save(Config.DOCK_OPACITY_KEY,opacity[i]));
        action(R.id.wallpaperChoice,R.string.wallpaper_label,R.string.ms_wallpaper_hint,this::showWallpaperPicker);
    }
    private void home() {
        toggle("milo_names",R.string.ms_names,R.string.ms_names_hint,()->config.alwaysShowNames);
        if(config.dockRow) toggle("milo_dock_title",R.string.ms_dock_title,R.string.ms_dock_title_hint,()->config.dockTitle);
        if(!config.topMenu) toggle("milo_wifi_button",R.string.ms_wifi_pill,R.string.ms_pill_hint,()->config.showNetworkPill);
        if(!config.topMenu) toggle("milo_inputs_button",R.string.ms_inputs_pill,R.string.ms_pill_hint,()->config.showInputsPill);
        if(config.appLibrary) toggle("milo_library_button",R.string.ms_library_pill,R.string.ms_pill_hint,()->config.libraryButton);
        action(R.id.reorderChoice,R.string.reorder,R.string.ms_reorder_hint,()->homeAction("reorder"));
        if(config.dockRow) action(0,R.string.dock_edit,R.string.ms_home_action_hint,()->homeAction("dock"));
    }
    private void screensaver() {
        chips(R.string.milo_clock_style,R.string.ms_clock_hint,
            new String[]{getString(R.string.milo_clock_light),getString(R.string.milo_clock_bold)},null,
            ()->choices.getBoolean("milo_clock_bold",false)?1:0,i->save("milo_clock_bold",i==1));
        int[] levels={30,50,80,100};
        chips(R.string.milo_brightness,R.string.ms_brightness_hint,new String[]{"30%","50%","80%","100%"},null,
            ()->Arrays.asList(30,50,80,100).indexOf(choices.getInt("milo_clock_brightness",80)),
            i->save("milo_clock_brightness",levels[i]));
        int[] minutes={0,1,3,5,10,15,30};
        String[] labels=new String[minutes.length];
        for(int i=0;i<labels.length;i++) labels[i]=i==0?getString(R.string.ms_disabled):getString(R.string.milo_minutes,minutes[i]);
        // Two short rows keep each delay readable on a 720p TV.
        LinearLayout card=card(R.string.milo_idle,R.string.milo_idle_hint);
        for(int start=0;start<minutes.length;start+=4) {
            final int from=start;
            int count=Math.min(4,minutes.length-start);
            String[] part=Arrays.copyOfRange(labels,start,start+count);
            chipLine(card,part,null,()->{
                int n=Arrays.binarySearch(minutes,config.homeIdleClockMinutes);
                return n>=from && n<from+count?n-from:-1;
            },i->save("milo_idle_minutes",minutes[from+i]));
        }
        action(0,R.string.milo_preview,R.string.ms_clock_hint,()->startActivity(new Intent(this,ClockActivity.class)));
    }
    private void motion() {
        toggle("animations",R.string.ms_animations,R.string.ms_motion_hint,()->config.animations);
        LinearLayout row=action(R.id.advancedChoice,R.string.advanced_effects,R.string.ms_advanced_hint,()->{
            if(config.advancedEffects) save("advanced_effects",false);
            else new AlertDialog.Builder(this).setTitle(R.string.advanced_effects)
                .setMessage(R.string.advanced_effects_warning)
                .setPositiveButton(R.string.enable,(d,w)->{ save("animations",true); save("advanced_effects",true); })
                .setNegativeButton(android.R.string.cancel,null).show();
        });
        TextView value=(TextView)row.getChildAt(1);
        bindings.add(()->{value.setText(getString(config.advancedEffects?R.string.ms_enabled:R.string.ms_disabled));});
        action(0,R.string.milo_preview,R.string.ms_motion_hint,()->{if(config.animations) preview.playMotion();});
    }
    private void updater() {
        TextView version=text(getString(R.string.ms_installed,BuildConfig.VERSION_NAME,
            getString(BuildConfig.BYEDPI_ENABLED?R.string.ms_edition_byedpi:R.string.ms_edition_standard)),15,true);
        version.setPadding(dp(4),0,0,dp(12)); content.addView(version);
        LinearLayout card=card(R.string.ms_update_mode,0);
        int[] labels={R.string.ms_manual,R.string.ms_on_open,R.string.ms_daily,R.string.ms_six_hours};
        for(int i=0;i<labels.length;i++) {
            final int mode=i;
            TextView choice=button(getString(labels[i]));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(43));lp.bottomMargin=dp(6);
            card.addView(choice,lp); register(choice);
            bindings.add(()->choice.setSelected(UpdateSchedule.mode(this)==mode));
            choice.setOnClickListener(v->{
                choices.edit().putInt(UpdatePolicy.KEY,mode).apply();
                UpdateSchedule.sync(this); refresh();
                if(mode!=UpdatePolicy.MANUAL) updates.check(false);
            });
        }
        updateStatus=text("",13,false);updateStatus.setPadding(dp(4),dp(2),dp(4),dp(12)); content.addView(updateStatus);
        action(0,R.string.update_check,0,()->{updates.check(true);updateStatus();});
        TextView safety=text(getString(R.string.ms_update_safe),12,false);
        safety.setPadding(dp(4),dp(6),dp(4),dp(12));content.addView(safety);
    }
    private void more() {
        action(0,R.string.report_title,R.string.report_settings_hint,
            ()->startActivity(new Intent(this,BugReportActivity.class)));
        action(R.id.defaultHomeChoice,R.string.home_settings,R.string.home_settings_help,
            ()->open(new Intent(Settings.ACTION_HOME_SETTINGS)));
        action(R.id.tvSettingsChoice,R.string.milo_all_settings,0,
            ()->open(SystemSettings.intent(this,Settings.ACTION_SETTINGS)));
        if(config.moduleCards) action(0,R.string.module_cards,0,()->homeAction("cards"));
        action(R.id.followThemeChoice,R.string.ms_reset,R.string.ms_reset_hint,()->
            new AlertDialog.Builder(this).setTitle(R.string.ms_reset_title).setMessage(R.string.ms_reset_hint)
                .setNegativeButton(android.R.string.cancel,null).setPositiveButton(android.R.string.ok,(d,w)->{
                    SharedPreferences.Editor e=choices.edit();
                    for(String key:APPEARANCE_KEYS)e.remove(key);
                    e.apply();markChanged();refresh();
                }).show());
    }
    private void homeAction(String action) {
        setResult(RESULT_OK,new Intent().putExtra(EXTRA_REORDER,true).putExtra(EXTRA_HOME_ACTION,action)); finish();
    }
    private void updateStatus() {
        if(updateStatus==null) return;
        SharedPreferences p=getSharedPreferences("milo_updates",0);
        long checked=p.getLong("success",0);
        String text=updates.isChecking()?getString(R.string.update_checking):
            updates.state==UpdateManager.ERROR?getString(R.string.update_failure):
            checked==0?getString(R.string.ms_not_checked):
            getString(R.string.ms_last_checked,android.text.format.DateFormat.getDateFormat(this).format(new Date(checked))
                +" "+android.text.format.DateFormat.getTimeFormat(this).format(new Date(checked)));
        if(!updates.isChecking() && !updates.releases.isEmpty()) text+="\n"+getString(R.string.ms_update_ready_hint);
        updateStatus.setText(text);
    }
    private void toggle(String key,int title,int hint,java.util.function.BooleanSupplier value) {
        LinearLayout row=action("animations".equals(key)?R.id.animationsChoice:0,title,hint,()->{
            boolean next=!value.getAsBoolean();
            if("animations".equals(key) && !next) choices.edit().putBoolean("advanced_effects",false).apply();
            save(key,next);
            if("animations".equals(key) && next)preview.playMotion();
        });
        TextView state=(TextView)row.getChildAt(1);
        bindings.add(()->state.setText(getString(value.getAsBoolean()?R.string.ms_enabled:R.string.ms_disabled)));
    }
    private LinearLayout action(int id,int title,int hint,Runnable action) {
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
        row.setId(id==0?View.generateViewId():id);row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14),dp(13),dp(14),dp(13));
        row.setBackgroundResource(R.drawable.milo_settings_choice);
        row.setFocusableInTouchMode(false);row.setFocusable(true);row.setClickable(true);
        row.setOnClickListener(v->action.run());
        LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);
        copy.setDuplicateParentStateEnabled(true);
        TextView label=text(getString(title),16,true);label.setDuplicateParentStateEnabled(true);
        label.setTextColor(getColorStateList(R.color.milo_quick_text));copy.addView(label);
        if(hint!=0) {
            TextView detail=text(getString(hint),12,false);detail.setDuplicateParentStateEnabled(true);
            detail.setTextColor(getColorStateList(R.color.milo_settings_detail));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(5);copy.addView(detail,lp);
        }
        row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        TextView state=text("›",14,true);state.setDuplicateParentStateEnabled(true);
        state.setTextColor(getColorStateList(R.color.milo_quick_text));
        state.setPadding(dp(10),0,0,0);row.addView(state);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);content.addView(row,lp);
        register(row);
        return row;
    }
    private LinearLayout card(int title,int hint) {
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.milo_quick_panel);card.setPadding(dp(14),dp(13),dp(14),dp(13));
        TextView label=text(getString(title),16,true);card.addView(label);
        if(hint!=0) {
            TextView help=text(getString(hint),12,false);
            LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.topMargin=dp(5);card.addView(help,hp);
        }
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);content.addView(card,lp);return card;
    }
    private void chips(int title,int hint,String[] labels,int[] ids,java.util.function.IntSupplier selected,
                       java.util.function.IntConsumer choose) {
        chipLine(card(title,hint),labels,ids,selected,choose);
    }
    private void chipLine(LinearLayout card,String[] labels,int[] ids,java.util.function.IntSupplier selected,
                          java.util.function.IntConsumer choose) {
        LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams ll=new LinearLayout.LayoutParams(-1,dp(40));ll.topMargin=dp(12);card.addView(line,ll);
        TextView previous=null;
        for(int i=0;i<labels.length;i++) {
            final int position=i;
            TextView chip=button(labels[i]);if(ids!=null)chip.setId(ids[i]);
            chip.setGravity(Gravity.CENTER);chip.setPadding(dp(3),0,dp(3),0);chip.setTextSize(labels.length>3?13:15);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-1,1);if(i<labels.length-1)cp.setMarginEnd(dp(6));
            line.addView(chip,cp);register(chip);
            chip.setOnClickListener(v->choose.accept(position));
            bindings.add(()->chip.setSelected(selected.getAsInt()==position));
            if(previous!=null){previous.setNextFocusRightId(chip.getId());chip.setNextFocusLeftId(previous.getId());}
            previous=chip;
        }
        if(previous!=null)previous.setNextFocusRightId(previous.getId());
    }
    private TextView button(String title) {
        TextView text=text(title,14,true);text.setId(View.generateViewId());
        text.setTextColor(getColorStateList(R.color.milo_quick_text));text.setBackgroundResource(R.drawable.milo_settings_choice);
        text.setGravity(Gravity.CENTER_VERTICAL);text.setPadding(dp(12),0,dp(12),0);
        text.setFocusableInTouchMode(false);text.setFocusable(true);text.setClickable(true);return text;
    }
    private void register(View view) {
        if(first==null)first=view;
        view.setNextFocusLeftId(navigation[page].getId());
    }
    private TextView text(String value,int sp,boolean bright) {
        TextView text=new TextView(this);text.setText(value);text.setTextSize(sp);
        text.setTextColor(bright?0xfff2f4f9:0xffabb8cc);
        text.setTypeface(UiFont.get(this,bright?R.string.font_medium:R.string.font_regular));return text;
    }
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private void save(String key,int value){choices.edit().putInt(key,value).apply();markChanged();refresh();}
    private void save(String key,boolean value){choices.edit().putBoolean(key,value).apply();markChanged();refresh();}
    private void markChanged(){choices.edit().putLong(Config.SETTINGS_REVISION_KEY,
        choices.getLong(Config.SETTINGS_REVISION_KEY,0)+1).apply();}
    private void refresh() {
        int savedY=scroll.getScrollY();
        View focused=getCurrentFocus();
        config=Config.load(this);
        for(Runnable binding:bindings)binding.run();
        preview.update(config,page);
        previewHint.setText(page==4?(UpdateSchedule.mode(this)==0?R.string.ms_mode_manual_hint:
            UpdateSchedule.mode(this)==1?R.string.ms_mode_open_hint:R.string.ms_mode_hint):R.string.ms_preview_hint);
        updateStatus();
        scroll.post(()->{if(isFinishing() || isDestroyed())return;scroll.scrollTo(0,savedY);if(focused!=null && focused.isAttachedToWindow())focused.requestFocus();});
    }
    private void open(Intent intent) {
        try {startActivity(intent);}
        catch(ActivityNotFoundException|SecurityException e){Toast.makeText(this,R.string.not_available,Toast.LENGTH_LONG).show();}
    }

    private void showWallpaperPicker() {
        File dir=Config.dir(this);
        List<File> images=new ArrayList<>(WallpaperRotation.images(dir==null?null:new File(dir,"wallpapers")));
        File single=dir==null?null:new File(dir,Config.WALLPAPER_NAME);
        if(single!=null && single.isFile())images.add(0,single);
        String[] labels=new String[images.size()+1];
        for(int i=0;i<images.size();i++)labels[i]=images.get(i).getName();
        labels[images.size()]=getString(R.string.wallpaper_none);
        String current=choices.getString(Config.WALLPAPER_CHOICE_KEY,null);
        int checked="none".equals(current)?images.size():-1;
        for(int i=0;i<images.size();i++)if(images.get(i).getName().equals(current))checked=i;
        new AlertDialog.Builder(this).setTitle(R.string.wallpaper_label)
            .setSingleChoiceItems(labels,checked,(dialog,which)->{
                dialog.dismiss();choices.edit().putString(Config.WALLPAPER_CHOICE_KEY,
                    which==images.size()?"none":images.get(which).getName()).apply();markChanged();refresh();
            }).setNeutralButton(R.string.wallpaper_theme_default,(dialog,which)->{
                choices.edit().remove(Config.WALLPAPER_CHOICE_KEY).apply();markChanged();refresh();
            }).setNegativeButton(android.R.string.cancel,null).show();
    }
    @Override public boolean dispatchKeyEvent(android.view.KeyEvent event){return RemoteInput.drop(event) || super.dispatchKeyEvent(event);}
}
