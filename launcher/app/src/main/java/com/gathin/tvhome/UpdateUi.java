package com.gathin.tvhome;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.core.content.FileProvider;
import java.io.File;

/** Small popup UI; uses the existing panel and input row resources. */
final class UpdateUi implements UpdateManager.Listener {
    private final Activity activity;
    private final UpdateButton button;
    private final UpdateManager manager;
    private PopupWindow popup;
    private AlertDialog confirmation;
    private boolean manual;
    UpdateUi(Activity activity,UpdateButton button) {
        this.activity=activity; this.button=button; manager=UpdateManager.get(activity);
        button.setOnClickListener(v -> { if(manager.state==UpdateManager.READY && manager.readyFile!=null) ready(manager.readyFile); else menu(); });
    }
    void start() { manager.attach(this); }
    void stop() { if(popup!=null) popup.dismiss(); if(confirmation!=null) confirmation.dismiss(); manager.detach(this); }
    void manualCheck() { manual=true; manager.check(true); Toast.makeText(activity,R.string.update_checking,Toast.LENGTH_SHORT).show(); }
    private String shownError="";
    public void changed() {
        if(manager.state==UpdateManager.ERROR && !manager.error.isEmpty() && !manager.error.equals(shownError)){
            shownError=manager.error;Toast.makeText(activity,manager.error,Toast.LENGTH_LONG).show();
        }else if(manager.state!=UpdateManager.ERROR)shownError="";
        boolean active=manager.state==UpdateManager.DOWNLOADING || manager.state==UpdateManager.VERIFYING;
        button.setVisibility(!manager.releases.isEmpty() || active || manager.state==UpdateManager.READY?View.VISIBLE:View.GONE);
        button.progress(manager.state==UpdateManager.DOWNLOADING?manager.percent:-1);
        int label=manager.state==UpdateManager.VERIFYING?R.string.update_verifying:
            manager.state==UpdateManager.READY?R.string.update_install:R.string.update_available;
        button.setContentDescription(activity.getString(label)+(active&&manager.percent>=0?" "+manager.percent+"%":""));
        if(manual && !manager.isChecking()) {
            manual=false;
            Toast.makeText(activity,manager.state==UpdateManager.ERROR?R.string.update_failure:
                manager.releases.isEmpty()?R.string.update_none:R.string.update_available,Toast.LENGTH_LONG).show();
        }
    }
    private void menu() {
        if(popup!=null && popup.isShowing() || confirmation!=null && confirmation.isShowing())return;
        if(manager.isChecking()) { Toast.makeText(activity,R.string.update_checking,Toast.LENGTH_SHORT).show(); return; }
        if(manager.state==UpdateManager.DOWNLOADING || manager.state==UpdateManager.VERIFYING) {
            Toast.makeText(activity,R.string.update_in_progress,Toast.LENGTH_SHORT).show(); return;
        }
        View panel=activity.getLayoutInflater().inflate(R.layout.popup_inputs,null,false);
        LinearLayout rows=panel.findViewById(R.id.rows);
        TextView heading=new TextView(activity); heading.setText(activity.getString(R.string.update_available)+(manager.hasPartial()?"\n"+activity.getString(R.string.opt_interrupted):""));
        heading.setTextColor(Color.WHITE); heading.setPadding(dp(12),dp(10),dp(12),dp(10)); rows.addView(heading);
        popup=new PopupWindow(panel,dp(360),ViewGroup.LayoutParams.WRAP_CONTENT,true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); popup.setOutsideTouchable(true); popup.setAnimationStyle(0);
        View first=null;
        for(String channel:new String[]{"stable","rc"}) {
            UpdateRelease selected=null;
            for(UpdateRelease release:manager.releases) if(release.channel.equals(channel)) selected=release;
            View row=activity.getLayoutInflater().inflate(R.layout.item_update,rows,false);
            TextView title=row.findViewById(R.id.updateTitle),detail=row.findViewById(R.id.updateDetail);
            title.setText(activity.getString(channel.equals("stable")?R.string.update_stable:R.string.update_rc)+(selected==null?"":" — "+selected.name));
            detail.setText(selected==null?activity.getString(R.string.update_unavailable):
                selected.date.substring(0,Math.min(10,selected.date.length()))+"  "+selected.notes);
            if(selected==null) { row.setAlpha(.45f); row.setFocusable(false); row.setEnabled(false); }
            else {
                final UpdateRelease release=selected;
                row.setOnClickListener(v -> { popup.dismiss(); confirm(release); });
                if(first==null) first=row;
            }
            rows.addView(row);
        }
        popup.setOnDismissListener(() -> { popup=null; button.requestFocus(); });
        panel.setAlpha(0); panel.setTranslationY(-dp(8));
        popup.showAsDropDown(button,0,dp(10),Gravity.END);
        panel.animate().alpha(1).translationY(0).setDuration(180).start();
        if(first!=null) first.requestFocus();
    }
    private void confirm(UpdateRelease release) {
        String message=(release.channel.equals("stable")?activity.getString(R.string.update_stable):activity.getString(R.string.update_rc))
            +" — "+release.name+"\n"+android.text.format.Formatter.formatShortFileSize(activity,release.bytes)+"\n\n"+release.notes;
        confirmation=new AlertDialog.Builder(activity).setTitle(R.string.update_confirm).setMessage(message)
            .setNegativeButton(R.string.update_no,(d,w) -> menu())
            .setPositiveButton(R.string.update_yes,(d,w) -> manager.download(release)).create();
        confirmation.setOnDismissListener(d -> confirmation=null);
        confirmation.show();
    }
    public void ready(File file) {
        if(activity.isFinishing() || activity.isDestroyed() || !activity.hasWindowFocus()) return;
        if(!activity.getPackageManager().canRequestPackageInstalls()) {
            new AlertDialog.Builder(activity).setTitle(R.string.update_install_permission).setMessage(R.string.update_install_permission_help)
                .setNegativeButton(android.R.string.cancel,null).setPositiveButton(R.string.settings,(d,w) -> {
                    Intent request=new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+activity.getPackageName()));
                    if(request.resolveActivity(activity.getPackageManager())!=null) activity.startActivity(request);
                }).show();
            return;
        }
        Uri uri=FileProvider.getUriForFile(activity,activity.getPackageName()+".updates",file);
        Intent install=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        install.setClipData(ClipData.newRawUri("Milo Home update",uri));
        for(ResolveInfo candidate:activity.getPackageManager().queryIntentActivities(install,0)) {
            if((candidate.activityInfo.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))!=0) {
                install.setComponent(new ComponentName(candidate.activityInfo.packageName,candidate.activityInfo.name));
                try { activity.startActivity(install); return; } catch(ActivityNotFoundException ignored) { }
            }
        }
        Toast.makeText(activity,R.string.update_installer_missing,Toast.LENGTH_LONG).show();
    }
    private int dp(int value) { return Math.round(value*activity.getResources().getDisplayMetrics().density); }
}
