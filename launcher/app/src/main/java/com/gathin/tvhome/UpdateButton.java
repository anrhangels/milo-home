package com.gathin.tvhome;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.TextView;

/** Actual byte progress only: no repeating animator or indeterminate frame loop. */
public final class UpdateButton extends TextView {
    private final Paint ring=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval=new RectF();
    private int progress=-1;
    public UpdateButton(Context context,AttributeSet attrs) { super(context,attrs); ring.setStyle(Paint.Style.STROKE); }
    void progress(int value) { if(progress!=value) { progress=value; invalidate(); } }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(progress<0) return;
        float density=getResources().getDisplayMetrics().density;
        float cx=getWidth()/2f,cy=getHeight()/2f,r=16*density;
        oval.set(cx-r,cy-r,cx+r,cy+r); ring.setStrokeWidth(2*density);
        ring.setColor(isFocused()?0xff111111:0xff34c759);
        canvas.drawArc(oval,-90,Math.max(0,Math.min(100,progress))*3.6f,false,ring);
    }
}
