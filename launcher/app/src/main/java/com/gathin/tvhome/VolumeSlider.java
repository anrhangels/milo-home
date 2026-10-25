package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.widget.SeekBar;

/** A single filled capsule; retains SeekBar's D-pad, touch and accessibility range actions. */
public final class VolumeSlider extends SeekBar {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF();
    private final Drawable speaker;
    private final float density;

    public VolumeSlider(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setPadding(0, 0, 0, 0);
        setBackground(null);
        setDefaultFocusHighlightEnabled(false);
        setThumb(null);
        setSplitTrack(false);
        setKeyProgressIncrement(1);
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        speaker = context.getDrawable(R.drawable.ic_milo_volume).mutate();
    }

    @Override protected synchronized void onDraw(Canvas canvas) {
        float inset = 2 * density;
        float width = getWidth() - 2 * inset;
        float height = getHeight() - 2 * inset;
        float radius = height / 2f;
        box.set(inset, inset, getWidth() - inset, getHeight() - inset);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x28FFFFFF);
        canvas.drawRoundRect(box, radius, radius, paint);
        float cap = Math.min(width, height * 1.45f);
        float filled = Math.max(cap, width * getProgress() / Math.max(1f, getMax()));
        box.right = inset + filled;
        paint.setColor(isEnabled() ? 0xE9EDF5F8 : 0x669EA8AF);
        canvas.drawRoundRect(box, radius, radius, paint);
        int size = Math.round(21 * density);
        int left = Math.round(inset + (cap - size) / 2);
        int top = (getHeight() - size) / 2;
        speaker.setBounds(left, top, left + size, top + size);
        speaker.setTint(0xFF35404B);
        speaker.draw(canvas);
        String percent = Math.round(100f * getProgress() / Math.max(1, getMax())) + "%";
        paint.setTextSize(14 * getResources().getDisplayMetrics().scaledDensity);
        paint.setTextAlign(Paint.Align.RIGHT);
        float textX = getWidth() - 18 * density;
        paint.setColor(0xFFF3F6F8);
        float baseline = getHeight()/2f - (paint.ascent()+paint.descent())/2f;
        canvas.drawText(percent, textX, baseline, paint);
        // Keep text readable as the fill passes behind it.
        canvas.save();
        canvas.clipRect(inset, inset, inset + filled, getHeight() - inset);
        paint.setColor(0xFF35404B);
        canvas.drawText(percent, textX, baseline, paint);
        canvas.restore();
        box.set(inset,inset,getWidth()-inset,getHeight()-inset);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((isFocused()?1.5f:0.7f)*density);
        paint.setColor(isFocused()?0xE6FFFFFF:0x35FFFFFF);
        canvas.drawRoundRect(box,radius,radius,paint);
        paint.setStyle(Paint.Style.FILL);
    }
}
