package com.example.lenovo.AugGraffiti;

/**
 * Created by kalla on 10/10/2016.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
    public Bitmap   myBitmap;
    public Canvas   myCanvas;
    private Path    tag_Path;
    private Paint   BitmapPaint;
    private Paint   tag_Paint;
    private float   mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    // this method is to set the styles and color of the paint that is to be drawn on the canvas
    public CanvasView(Context context, AttributeSet attributes) {
        super(context, attributes);

        tag_Path = new Path();
        BitmapPaint = new Paint(Paint.DITHER_FLAG);
        tag_Paint = new Paint();
        tag_Paint.setAntiAlias(true);
        tag_Paint.setDither(true);
        tag_Paint.setColor(Color.RED);
        tag_Paint.setStyle(Paint.Style.STROKE);
        tag_Paint.setStrokeJoin(Paint.Join.ROUND);
        tag_Paint.setStrokeCap(Paint.Cap.ROUND);
        tag_Paint.setStrokeWidth(9);

    }

    /* this method is called when the size of the view is changed because of the drawing of the bitmap
     and reflects those changes in the view*/
    @Override
    protected void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        super.onSizeChanged(width, height, oldwidth, oldheight);
        myBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas(myBitmap);
    }

    /* this method enables you to draw on the canvas view*/
    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(myBitmap, 0, 0, BitmapPaint);
        canvas.drawPath(tag_Path, tag_Paint);
    }

    /* this method captures the initial touch position*/
    private void touch_start(float x, float y) {
        tag_Path.reset();
        tag_Path.moveTo(x, y);
        mX = x;
        mY = y;
    }

    /* this method captures the most recent position and also the intermediate positions in the motion event*/
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            tag_Path.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }

    /* this method captures the final release position and also the intermediate positions from the last touch event*/
    private void touch_up() {
        tag_Path.lineTo(mX, mY);
        myCanvas.drawPath(tag_Path, tag_Paint);
        tag_Path.reset();
    }


    /* this method captures the motion of the tag on the screen while the tag is placed*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    // this method is used to enable a drawing cache and place a bitmap in the drawing cache
    public Bitmap getBitmap()
    {
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap tag_bmp = Bitmap.createBitmap(this.getDrawingCache());
        this.setDrawingCacheEnabled(false);
        return tag_bmp;
    }

}