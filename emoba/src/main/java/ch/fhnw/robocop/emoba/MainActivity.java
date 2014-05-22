package ch.fhnw.robocop.emoba;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.fhnw.edu.mad.com.bluetooth.BluetoothChannel;
import ch.fhnw.edu.mad.mindstorm.LegoBrickSensorListener;
import ch.fhnw.edu.mad.mindstorm.nxt.NXT;
import ch.fhnw.edu.mad.mindstorm.robot.model.NXTShotBot;
import ch.fhnw.robocop.emoba.Drawing;

public class MainActivity extends ActionBarActivity implements LegoBrickSensorListener, SensorEventListener {

    private static final int REQUEST_CONNECT_DEVICE = 0;
    private static final int RESULT_CANCELED = -1;
    private static final int RESULT_OK = 1;
    private boolean onEmulator;
    private LinearLayout controlView;
    private Drawing controlPanel;

    private static SensorManager sensorService;

    public NXTShotBot robot;
    private BluetoothAdapter adapter;
    SensorManager manager;
    AlertDialog alert;
    Sensor sensor;
    NXT nxt;
    boolean bluetoothEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controlView = (LinearLayout) getLayoutInflater().inflate(
                R.layout.sample_control_view, null);

        controlPanel = (Drawing)controlView.findViewById(R.id.game);
        //controlPanel.setOnTouchListener(this);

        sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acc = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorService.registerListener(this, acc, SensorManager.SENSOR_DELAY_FASTEST);

        onEmulator = Build.PRODUCT.startsWith("sdk");
        setContentView(R.layout.sample_connect_view);

        // enable Bluetooth
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(this, "No bluetooth available", Toast.LENGTH_SHORT).show();
        } else if (!adapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
            bluetoothEnabled = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(robot!=null){
            robot.setVelocity(0, 0);
            robot.stop();
        }
        this.finish();
        if (adapter != null && bluetoothEnabled && adapter.isEnabled()) {
            adapter.disable();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        drawing.setControlPoint(new Point((int)event.getX(),(int)event.getY()));
//        Log.i("lol", "onTouchEvent: " + (int)event.getX() + " " + (int)event.getY());
//        return false;
//    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == RESULT_CANCELED) {
                    // do nothing
                } else if (resultCode == RESULT_OK) {
                    if (onEmulator) {
                        setContentView(controlView);
                    } else {
                        final BluetoothDevice[] pairedDevices = adapter.getBondedDevices().toArray(new BluetoothDevice[0]);

                        final List<String> pairedLegoDevicesMacs = new ArrayList<String>();
                        final List<String> pairedLegoDevicesNames = new ArrayList<String>();

                        for (BluetoothDevice b : pairedDevices) {
                            String tmp = b.getAddress();
                            if (tmp.subSequence(0, 8).equals("00:16:53")) {
                                pairedLegoDevicesMacs.add(b.getAddress());
                                pairedLegoDevicesNames.add(b.getName());
                            }
                        }

                        new AlertDialog.Builder(this)
                                .setTitle("Pick a device")
                                .setItems(pairedLegoDevicesNames.toArray(new String[0]),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int item) {
                                                onDeviceChosen(pairedLegoDevicesMacs.get(item));
                                            }
                                        }).create().show();
                    }
                }
                break;
        }
    }

    public void onDeviceChosen(String macAdress) {
        try {
            nxt = new NXT();
            nxt.addSensorListener(this);
            nxt.connectAndStart(macAdress);
            alert = ProgressDialog.show(this, "", "connecting...");
        } catch (IllegalStateException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onButtonClick(View view) {
        onActivityResult(REQUEST_CONNECT_DEVICE, RESULT_OK, null);
    }

    public void onActionClick(View view) {
        if(!onEmulator) {
            new Thread() {
                public void run() {
                    robot.action(true);
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    robot.action(false);
                }
            }.start();
        }
        Toast toast = Toast.makeText(this, "SHOOT!!!", Toast.LENGTH_SHORT);
        toast.show();
    }


    @Override
    public void handleLegoBrickMessage(Message message) {
        alert.dismiss();
        switch (message.getData().getInt("message")) {
            case BluetoothChannel.DISPLAY_TOAST:
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Error 001",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case BluetoothChannel.STATE_CONNECTERROR:
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Error 002",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case BluetoothChannel.STATE_CONNECTED:
                // Ready to instantiate and start robot.
                robot = new NXTShotBot(nxt);
                robot.start();
                runOnUiThread(new Thread() {

                    @Override
                    public void run() {
                        setContentView(controlView);
                    }

                });

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:

                float actX = event.values[0];
                float actY = event.values[1];

                float relX = controlPanel.getCenterPoint().x + (actX * (-50));
                float relY = controlPanel.getCenterPoint().y + (actY * (50));

                if ((controlPanel.getControlPoint().x - relX > 20 ||
                        controlPanel.getControlPoint().x - relX < -20) ||
                        controlPanel.getControlPoint().y - relY > 20 ||
                        controlPanel.getControlPoint().y - relY < -20) {
                    controlPanel.getControlPoint().set((int) relX, (int) relY);
                }

                float velX = (relX - controlPanel.getControlPoint().x) / controlPanel.getControlPoint().x;
                float velY = (relY - controlPanel.getControlPoint().y) / controlPanel.getControlPoint().y;

                if (-0.1 < velX && velX < 0.1) {
                    velX = 0;
                }
                if (-0.1 < velY && velY < 0.1) {
                    velY = 0;
                }

                if (!Build.PRODUCT.startsWith("sdk") && robot != null) {
                    robot.setVelocity(-velX, velY);
                }

                break;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
