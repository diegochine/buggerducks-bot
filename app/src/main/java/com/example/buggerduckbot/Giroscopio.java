package com.example.buggerduckbot;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.EventListener;

public class Giroscopio implements SensorEventListener {

    private SensorManager manager;
    private Sensor sensor;
    private HandlerThread thread;
    private Handler handler;

    private Float orientation;


    public Giroscopio(Context c){
        manager = (SensorManager) c.getSystemService(c.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        thread = new HandlerThread("Sensor thread", Thread.NORM_PRIORITY);
    }

    public void register(){
        thread.start();
        handler = new Handler(thread.getLooper());
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
    }

    public void unregister(){
        manager.unregisterListener(this);
        thread.quit();
    }

    public Float getOrientation(){
        return orientation;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

        // Remap coordinate system
        float[] remappedRotationMatrix = new float[16];
        SensorManager.remapCoordinateSystem(rotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                remappedRotationMatrix);

        // Convert to orientations
        float[] orientations = new float[3];
        SensorManager.getOrientation(remappedRotationMatrix, orientations);

        //Convert radiant to degree
        orientation = (float) (Math.toDegrees(orientations[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
