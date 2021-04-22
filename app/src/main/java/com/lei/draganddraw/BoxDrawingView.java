package com.lei.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoxDrawingView extends View {

    public static final String TAG = BoxDrawingView.class.getName();

    public static final String PARENT_STATE_KEY = "parent_state";
    public static final String BOXEN_KEY = "boxen";

    private Box mCurrentBox;
    private List<Box> mBoxen = new ArrayList<>();
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    public BoxDrawingView(Context context) {
        super(context);
    }

    public BoxDrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);
//        mBoxPaint.setStyle(Paint.Style.STROKE);
//        mBoxPaint.setStrokeWidth(10);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0XFFF8EFE0);
    }

    private int mWidth;
    private int mHeight;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        PointF touchPoint  = null;
        PointF touchPoint2 = null;

        for (int i=0;i<event.getPointerCount();i++) {
            if(event.getPointerId(i)==0)
                touchPoint = new PointF(event.getX(i), event.getY(i));
            if(event.getPointerId(i)==1)
                touchPoint2 = new PointF(event.getX(i), event.getY(i));
        }

        PointF current = new PointF(event.getX(), event.getY());
        String action = "";

        //getActionMasked多点触控，单点触控也能用这个
        // event.getActionMasked <==> event.getAction() & MotionEvent.ACTION_MASK
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                mCurrentBox = new Box(touchPoint);
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                action = "ACTION_POINTER_DOWN";
                mCurrentBox.setPointerOrigin(touchPoint2);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if(touchPoint  != null )
                    mCurrentBox.setCurrent(touchPoint);
                if(touchPoint2 != null ) {
                    PointF boxOrigin     = mCurrentBox.getOrigin();
                    PointF pointerOrigin = mCurrentBox.getPointerOrigin();
                    float angle2 = (float) Math.atan2(touchPoint2.y   - boxOrigin.y, touchPoint2.x   - boxOrigin.x);
                    float angle1 = (float) Math.atan2(pointerOrigin.y - boxOrigin.y, pointerOrigin.x - boxOrigin.x);
                    float calculatedAngle = (float) Math.toDegrees(angle2 - angle1);
                    if (calculatedAngle < 0) calculatedAngle += 360;
                    mCurrentBox.setAngle(calculatedAngle);
                    Log.d(TAG, "Set Box Angle " + calculatedAngle);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                Log.d(TAG, "Finger UP Box Set");
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                Log.d(TAG, "Action Cancel Box Set");
                mCurrentBox = null;
                break;
        }

        /*
        //getAction单点触控
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                mCurrentBox = new Box(current);
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(current);
                    //invalidate让BoxDrawingView失效，使它重新自我绘制，再次调用onDraw
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                break;
        }*/

        Log.i(TAG, action + " at x=" + current.x + ", y=" + current.y);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxen) {

            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            float angle = box.getAngle();
            float px = (box.getOrigin().x+box.getCurrent().x)/2;
            float py = (box.getOrigin().y+box.getCurrent().y)/2;
            canvas.save();
            canvas.rotate(angle, px, py);

            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.restore();
        }

    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PARENT_STATE_KEY, parcelable);
        bundle.putParcelableArrayList(BOXEN_KEY, (ArrayList<Box>) mBoxen);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable(PARENT_STATE_KEY));
        mBoxen = bundle.getParcelableArrayList(BOXEN_KEY);

    }
}
