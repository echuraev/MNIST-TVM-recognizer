package com.deelvin.mnist_tvm_recognizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Date;

public class PaintView extends View {
    private static final long DOUBLE_CLICK_TIME_DELTA = 200; // milliseconds
    private Path path = new Path();
    private Paint brush = new Paint();
    private Date lastTouch = new Date();
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Matrix scaleMatrix = new Matrix();
    private Path bitmapPath = new Path();
    private Paint canvasPaint = new Paint();

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);

        brush.setAntiAlias(true);
        brush.setColor(Color.BLUE);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(50.f);

        bitmap = Bitmap.createBitmap(TVM_MNIST_Helper.MNIST_INPUT_W, TVM_MNIST_Helper.MNIST_INPUT_H, Bitmap.Config.ALPHA_8);
        bitmapCanvas = new Canvas(bitmap);
        canvasPaint.setAntiAlias(true);
        canvasPaint.setStyle(Paint.Style.STROKE);
        canvasPaint.setStrokeJoin(Paint.Join.ROUND);
        canvasPaint.setStrokeWidth(2.f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Date currTime;
        long ms;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currTime = new Date();
                ms = (long) (currTime.getTime() - lastTouch.getTime());
                lastTouch = currTime;
                if (ms >= 5000 || ms <= DOUBLE_CLICK_TIME_DELTA) {
                    path.reset();
                    postInvalidate();
                    bitmap.eraseColor(Color.TRANSPARENT);
                }
                path.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                currTime = new Date();
                ms = (long) (currTime.getTime() - lastTouch.getTime());
                if (ms > 100) {
                    Log.w("myApp", "on break " + Integer.toString(this.getWidth()));
                    float scaleFactorX = TVM_MNIST_Helper.MNIST_INPUT_W / (float) this.getWidth();
                    float scaleFactorY = TVM_MNIST_Helper.MNIST_INPUT_H / (float) this.getHeight();
                    scaleMatrix.setScale(scaleFactorX, scaleFactorY);
                    path.transform(scaleMatrix, bitmapPath);
                    bitmapCanvas.drawPath(bitmapPath, canvasPaint);
                    MainActivity.setBitmap(bitmap);
                }
                break;
            default:
                return false;
        }

        postInvalidate();
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, brush);
    }
}