package ch.fhnw.robocop.emoba;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import ch.fhnw.edu.mad.com.bluetooth.BluetoothChannel;
import ch.fhnw.edu.mad.mindstorm.LegoBrickSensorListener;
import ch.fhnw.edu.mad.mindstorm.robot.model.NXTShotBot;

/**
 * Created by olry on 4/28/14.
 */
public class Drawing extends SurfaceView implements SurfaceHolder.Callback {

    public NXTShotBot robot;
    private ControlThread thread;
    private Point centerPoint;
    private Point controlPoint;
    private int nullRadius = 50;
    private int ballRadius = 20;
    private Paint p = new Paint();

    public Drawing(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setWillNotDraw(false);
        centerPoint = new Point();
        controlPoint = new Point();
        Log.i("drawing", "in constructor");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setControlPoint(new Point((int) event.getX(), (int) event.getY()));
        Log.i("lol", "onTouchEvent: " + (int) event.getX() + " "	+ (int) event.getY());
        return false;
    }

    public void doDraw(Canvas canvas) {
        Log.i("draw", controlPoint + ": " + controlPoint.x + " " + controlPoint.y);
        canvas.drawColor(Color.BLACK);
        p.setColor(Color.GRAY);
        canvas.drawCircle(centerPoint.x, centerPoint.y, nullRadius, p);
        p.setColor(Color.BLACK);
        canvas.drawCircle(centerPoint.x, centerPoint.y, (ballRadius + 10), p);
        p.setColor(Color.RED);
        canvas.drawCircle(controlPoint.x, controlPoint.y, ballRadius, p);
    }

    public Point getControlPoint() {
        return new Point(controlPoint);
    }

    public Point getCenterPoint() {
        return new Point(centerPoint);
    }

    public void setControlPoint(Point p) {
        controlPoint.set(p.x, p.y);
        Log.i("controlPoint,p", p.x + " " + p.y);
        Log.i("controlPoint,cp", controlPoint + ": " + controlPoint.x + " "
                + controlPoint.y);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        centerPoint.set((int) ((this.getWidth() - 1) / 2.0),
                (int) ((this.getHeight() - 1) / 2.0));
        thread = new ControlThread(holder);
        setControlPoint(centerPoint);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (thread.isRunning) {
            thread.setRunning(false);
        }
    }


    public class ControlThread extends Thread {
        private SurfaceHolder holder;
        private Canvas canvas;
        private boolean isRunning;

        public ControlThread(SurfaceHolder holder) {
            this.holder = holder;
        }

        public void setRunning(boolean flag) {
            isRunning = flag;
        }

        public boolean isRunning() {
            return isRunning;
        }

        @Override
        public void run() {
            isRunning = true;
            while (isRunning) {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    doDraw(canvas);
                    holder.unlockCanvasAndPost(canvas);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
