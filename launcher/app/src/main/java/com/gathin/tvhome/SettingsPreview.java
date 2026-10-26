package com.gathin.tvhome;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.io.File;

/** Small, static UI illustration, redrawn only when a setting changes; never starts a renderer loop. */
public final class SettingsPreview extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect=new RectF();
    private Config config;
    private boolean clock,updateExample;
    private boolean bold;
    private boolean blackClock;
    private int brightness=80;
    private int clockSize=120;
    private Bitmap wallpaper;
    private String wallpaperPath="";
    private final Typeface font;
    public SettingsPreview(Context c,AttributeSet attrs) {
        super(c,attrs); font=UiFont.get(c,R.string.font_regular);
    }
    void update(Config value,int page) {
        config=value; clock=page==2; updateExample=page==4;
        android.content.SharedPreferences p=getContext().getSharedPreferences(Config.SETTINGS_NAME,0);
        blackClock=ClockBackdrop.normalize(p.getInt(ClockBackdrop.KEY,0))==ClockBackdrop.BLACK;
        bold=p.getBoolean("milo_clock_bold",false);
        clockSize=Math.max(90,Math.min(150,p.getInt("milo_clock_size",120)));
        brightness=p.getInt("milo_clock_brightness",80);
        File file=value.wallpaper;
        String path=file==null?"":file.getAbsolutePath();
        if(!path.equals(wallpaperPath)) {
            wallpaperPath=path; wallpaper=null;
            if(file!=null) {
                BitmapFactory.Options o=new BitmapFactory.Options(); o.inJustDecodeBounds=true;
                BitmapFactory.decodeFile(path,o);
                if(o.outWidth>0 && o.outHeight>0) {
                    o.inSampleSize=1;
                    while(o.outWidth/o.inSampleSize>512 || o.outHeight/o.inSampleSize>288) o.inSampleSize*=2;
                    o.inJustDecodeBounds=false; o.inPreferredConfig=Bitmap.Config.RGB_565;
                    wallpaper=BitmapFactory.decodeFile(path,o);
                }
            }
        }
        invalidate();
    }
    void playMotion() {
        animate().cancel(); setAlpha(.35f);
        animate().alpha(1f).setDuration(400).start();
    }
    private void box(Canvas c,float l,float t,float r,float b,float radius,int color) {
        rect.set(l,t,r,b); paint.setColor(color); paint.setStyle(Paint.Style.FILL);
        c.drawRoundRect(rect,radius,radius,paint);
    }
    private void text(Canvas c,String value,float x,float y,float size,int color) {
        paint.setShader(null); paint.setTypeface(font); paint.setTextSize(size); paint.setColor(color);
        c.drawText(value,x,y,paint);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(config==null) return;
        canvas.save(); canvas.scale(getWidth()/240f,getWidth()/240f);
        box(canvas,0,12,240,170,14,0xff080b12);
        canvas.save();
        android.graphics.Path clip=new android.graphics.Path();
        rect.set(0,12,240,170); clip.addRoundRect(rect,14,14,android.graphics.Path.Direction.CW);
        canvas.clipPath(clip);
        if(!clock) {
            paint.setShader(new RadialGradient(70,95,185,0xff806132,0xff0a1424,Shader.TileMode.CLAMP));
            canvas.drawRect(0,12,240,170,paint); paint.setShader(null);
            if(wallpaper!=null) {
                float scale=Math.max(240f/wallpaper.getWidth(),158f/wallpaper.getHeight());
                float w=wallpaper.getWidth()*scale,h=wallpaper.getHeight()*scale;
                rect.set((240-w)/2,12+(158-h)/2,(240+w)/2,12+(158+h)/2);
                canvas.drawBitmap(wallpaper,null,rect,paint);
            }
            text(canvas,"20:43",13,44,20,0xfff3f4f8);
            int pills=1+(config.showNetworkPill?1:0)+(config.showInputsPill?1:0)+(config.libraryButton?1:0);
            int right=updateExample?217:237;
            for(int i=0;i<pills;i++) box(canvas,right-pills*20+i*20,28,right+16-pills*20+i*20,38,5,0x60ffffff);
            if(updateExample) {
                box(canvas,215,28,231,40,6,0xff74ce92);
                paint.setColor(0xff163424);paint.setStrokeWidth(1.2f);
                canvas.drawLine(223,37,223,31,paint);
                canvas.drawLine(223,31,220,34,paint);canvas.drawLine(223,31,226,34,paint);
            }
            if(config.dockTitle) text(canvas,"Milo",14,87,20,0xffeeeeef);
            int opacity=DockOpacity.alpha(config.dockOpacity,config.cinematicHome?140:255);
            if(config.dockGlass) {
                int top=DockOpacity.alpha(config.dockOpacity,36),bottom=DockOpacity.alpha(config.dockOpacity,13);
                paint.setShader(new LinearGradient(0,96,0,137,(top<<24)|0xffffff,(bottom<<24)|0xffffff,Shader.TileMode.CLAMP));
                rect.set(9,96,231,137);canvas.drawRoundRect(rect,10,10,paint);paint.setShader(null);
            } else box(canvas,9,96,231,137,10,(opacity<<24)|0x141416);
            int columns=config.columns;
            float gap=5, width=(212-(columns-1)*gap)/columns;
            int[] colours={0xffdce5ed,0xffb4c3db,0xffe5d5b2,0xffc7bddb,0xffb5d6d0,0xffc4ccdb,0xffd5bfc8};
            for(int i=0;i<columns;i++) {
                float x=14+i*(width+gap);
                box(canvas,x,102,x+width,130,6,colours[i]);
                if(config.alwaysShowNames) box(canvas,x+3,132,x+width-3,134,1,0xffc1c5ce);
            }
            for(int row=0;row<config.rowsBelowDock;row++) {
                for(int i=0;i<columns;i++) {
                    float x=14+i*(width+gap);
                    box(canvas,x,142+row*10,x+width,149+row*10,2,0x55bac5d6);
                }
            }
        } else {
            if (blackClock) paint.setColor(android.graphics.Color.BLACK);
            else paint.setShader(new RadialGradient(120,90,120,0xff182142,0xff050609,Shader.TileMode.CLAMP));
            canvas.drawRect(0,12,240,170,paint); paint.setShader(null);
            paint.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif-thin",Typeface.NORMAL));
            paint.setTextSize(47f*clockSize/120f); paint.setColor((Math.round(brightness*2.55f)<<24)|0xffffff);
            canvas.drawText("20:43",(240-paint.measureText("20:43"))/2,105,paint);
            text(canvas,"Milo",102,127,10,0xff8891a6);
        }
        canvas.restore();
        text(canvas,getContext().getString(clock?R.string.ms_clock_demo:updateExample?R.string.ms_update_example:R.string.ms_home_demo),2,194,12,0xffabb8cc);
        canvas.restore();
    }
}
