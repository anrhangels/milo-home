package com.gathin.tvhome;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.google.zxing.common.BitMatrix;

/** User-reviewed snapshot and a locally generated link. This screen never sends a report. */
public final class BugReportActivity extends Activity {
    private LinearLayout facts;
    private ImageView qr;
    private TextView action,status;
    private BugReport report;
    private volatile boolean disposed;
    private int qrPixels;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(34),dp(22),dp(34),dp(20));
        root.setBackgroundResource(R.drawable.milo_settings_backdrop);
        root.addView(text(getString(R.string.report_title),28,true));
        TextView intro=text(getString(R.string.report_intro),14,false);
        intro.setPadding(0,dp(5),0,dp(16));root.addView(intro);

        LinearLayout body=new LinearLayout(this);
        root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout left=new LinearLayout(this);left.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftSize=new LinearLayout.LayoutParams(0,-1,1);
        leftSize.setMarginEnd(dp(22));body.addView(left,leftSize);
        ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);
        left.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        facts=new LinearLayout(this);facts.setOrientation(LinearLayout.VERTICAL);
        facts.setPadding(dp(18),dp(12),dp(18),dp(12));facts.setBackgroundResource(R.drawable.milo_quick_panel);
        scroll.addView(facts);
        TextView privacy=text(getString(R.string.report_privacy),12,false);
        privacy.setPadding(dp(4),dp(10),0,dp(10));left.addView(privacy);
        action=text(getString(R.string.report_loading),16,true);action.setId(View.generateViewId());
        action.setGravity(Gravity.CENTER);action.setPadding(dp(12),0,dp(12),0);
        action.setTextColor(getColorStateList(R.color.milo_quick_text));
        action.setBackgroundResource(R.drawable.milo_settings_choice);
        action.setFocusable(true);action.setFocusableInTouchMode(true);action.setClickable(true);
        action.setEnabled(false);left.addView(action,new LinearLayout.LayoutParams(-1,dp(48)));

        LinearLayout right=new LinearLayout(this);right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.CENTER_HORIZONTAL);
        right.setPadding(dp(16),dp(14),dp(16),dp(14));right.setBackgroundResource(R.drawable.milo_quick_panel);
        int heightDp=Math.round(getResources().getDisplayMetrics().heightPixels/getResources().getDisplayMetrics().density);
        int side=Math.min(292,Math.max(180,heightDp-220));
        body.addView(right,new LinearLayout.LayoutParams(dp(side+32),-1));
        qr=new ImageView(this);qr.setContentDescription(getString(R.string.report_qr));
        qr.setScaleType(ImageView.ScaleType.FIT_CENTER);qr.setVisibility(View.INVISIBLE);
        right.addView(qr,new LinearLayout.LayoutParams(dp(side),dp(side)));
        qrPixels=Math.max(320,Math.min(640,dp(side)));
        status=text(getString(R.string.report_review),13,false);
        status.setGravity(Gravity.CENTER);status.setPadding(0,dp(12),0,0);right.addView(status);
        setContentView(root);
        new Thread(()->{
            BugReport captured=BugReportSnapshot.capture(getApplicationContext());
            runOnUiThread(()->{if(disposed)return;report=captured;render();});
        },"Milo-report").start();
    }
    private void render() {
        fact(R.string.report_version,report.version+" ("+report.code+")");
        fact(R.string.report_edition,getString(report.byedpi?R.string.ms_edition_byedpi:R.string.ms_edition_standard));
        fact(R.string.report_device,report.manufacturer+" / "+report.model);
        fact(R.string.report_android,report.android+" · API "+report.api);
        fact(R.string.report_ram,memory(report.totalRam)+" / "+memory(report.availableRam));
        fact(R.string.report_storage,report.availableStorage<0?getString(R.string.report_unknown):BugReport.gib(report.availableStorage));
        action.setText(R.string.report_show_qr);action.setEnabled(true);
        action.setOnClickListener(v->generate());action.requestFocus();
        action.setNextFocusDownId(action.getId());action.setNextFocusRightId(action.getId());
    }
    private String memory(long value){return value<0?getString(R.string.report_unknown):BugReport.mib(value);}
    private void fact(int label,String value) {
        TextView title=text(getString(label),12,false);facts.addView(title);
        TextView content=text(value,17,true);content.setPadding(0,dp(2),0,dp(10));facts.addView(content);
    }
    private void generate() {
        action.setEnabled(false);action.setText(R.string.report_loading);
        new Thread(()->{
            try {
                BitMatrix matrix=BugReportQr.encode(report.issueUrl(),qrPixels);
                int width=matrix.getWidth(),height=matrix.getHeight();
                int[] pixels=new int[width*height];
                for(int y=0;y<height;y++)for(int x=0;x<width;x++)
                    pixels[y*width+x]=matrix.get(x,y)?0xff000000:0xffffffff;
                Bitmap bitmap=Bitmap.createBitmap(pixels,width,height,Bitmap.Config.RGB_565);
                runOnUiThread(()->{
                    if(disposed)return;
                    qr.setImageBitmap(bitmap);qr.setVisibility(View.VISIBLE);
                    status.setText(R.string.report_scan);
                    action.setEnabled(true);action.setText(R.string.report_done);
                    action.setOnClickListener(v->finish());action.requestFocus();
                });
            } catch(Exception failure) {
                runOnUiThread(()->{
                    if(disposed)return;
                    status.setText(R.string.report_error);action.setEnabled(true);
                    action.setText(R.string.report_show_qr);action.setOnClickListener(v->generate());action.requestFocus();
                });
            }
        },"Milo-report-QR").start();
    }
    private TextView text(String value,int size,boolean bright) {
        TextView view=new TextView(this);view.setText(value);view.setTextSize(size);
        view.setTextColor(bright?0xfff2f4f9:0xffabb8cc);
        view.setTypeface(UiFont.get(this,bright?R.string.font_medium:R.string.font_regular));return view;
    }
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    @Override protected void onDestroy(){disposed=true;qr.setImageDrawable(null);super.onDestroy();}
}
