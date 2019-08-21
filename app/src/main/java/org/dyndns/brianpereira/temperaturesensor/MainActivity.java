/* Program to display the temperature and humidity
   Written by Brian Pereira on 23 August 2017
*/

package org.dyndns.brianpereira.temperaturesensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView txtViewTemperature;
    SensorManager sensorManager;
    Sensor temperatureSensor, humiditySensor;
    String TEMPERATURE_NOT_SUPPORTED = "Temperature sensor not found on this device.";
    String HUMIDITY_NOT_SUPPORTED = "Humidity sensor not found on this device.";

    float ambientTemperature = 0;
    float lastKnownRelativeHumidity = 0;
    float absoluteHumidity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Code starts here
        txtViewTemperature = (TextView) findViewById(R.id.txtViewTemp);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        if (temperatureSensor == null)
            txtViewTemperature.setText(TEMPERATURE_NOT_SUPPORTED);
        if (humiditySensor == null)
            txtViewTemperature.setText(HUMIDITY_NOT_SUPPORTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            lastKnownRelativeHumidity = event.values[0];
        }

        if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            ambientTemperature = event.values[0];

          /*  Toast.makeText(this.getApplicationContext(),"Sensed Temperature change",
                    Toast.LENGTH_SHORT).show(); */
        }
        if(lastKnownRelativeHumidity !=0) {
            absoluteHumidity = calculateAbsoluteHumidity(ambientTemperature, lastKnownRelativeHumidity);
        }

        txtViewTemperature.setText("Ambient Temperature:\n " + String.valueOf(ambientTemperature)
                + getResources().getString(R.string.celsius) +"\nAbsolute Humidity:\n "
                + String.valueOf(absoluteHumidity)+ "%");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String accuracyMsg = "";

        switch(accuracy){
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyMsg="Sensor has high accuracy";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyMsg="Sensor has medium accuracy";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyMsg="Sensor has low accuracy";
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                accuracyMsg="Sensor has unreliable accuracy";
                break;
            default:
                break;
        }
        Toast accuracyToast = Toast.makeText(this.getApplicationContext(), accuracyMsg, Toast.LENGTH_SHORT);
        accuracyToast.show();
    }

    /* Meaning of the constants
     Dv: Absolute humidity in grams/meter3
     m: Mass constant
     Tn: Temperature constant
     Ta: Temperature constant
     Rh: Actual relative humidity in percent (%) from phone’s sensor
     Tc: Current temperature in degrees C from phone’ sensor
     A: Pressure constant in hP
     K: Temperature constant for converting to kelvin
     */
    public float calculateAbsoluteHumidity(float temperature, float relativeHumidity)
    {
        float Dv = 0;
        float m = 17.62f;
        float Tn = 243.12f;
        float Ta = 216.7f;
        float Rh = relativeHumidity;
        float Tc = temperature;
        float A = 6.112f;
        float K = 273.15f;

        Dv =   (float) (Ta * (Rh/100) * A * Math.exp(m*Tc/(Tn+Tc)) / (K + Tc));

        return Dv;
    }
}
