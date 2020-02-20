package udl.eps.testaccelerometre;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class TestAccelerometreActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorAccelerometre, sensorLight;
    private boolean color = false;
    private TextView view, viewLight;
    private float maxRange;
    private float lValue;
    private long lastUpdateAccel, lastUpdateLight, maxTimeAccel = 200, maxTimeLight = 8000;


    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView viewAccel;
        ScrollView scrollView;
        view = (TextView) findViewById(R.id.textViewAccelerometro);     //Busquem la referencia amb els textviews de la layout
        viewAccel = (TextView) findViewById(R.id.textView);
        viewLight = (TextView) findViewById(R.id.textViewLight);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        view.setBackgroundColor(Color.GREEN);                   //Pintem el background dels textviews corresponents amb el color que els toca
        scrollView.setBackgroundColor(Color.YELLOW);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);                       //Obtenim la referencia al sensorManager
        sensorAccelerometre = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);        //Una vegada tenim la referencia amb sensorManager, busquem la referencia...
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);                        //...amb els serveis dels sensors

        if (sensorAccelerometre != null) {
            String textAccel = viewAccel.getText().toString();
            float resolution = sensorAccelerometre.getResolution();         //Si la referencia al sensor es diferent de null
            maxRange = sensorAccelerometre.getMaximumRange();               //Agafem les dades del sensor: resolució, maxRange, power
            float power = sensorAccelerometre.getPower();

            textAccel = textAccel + getString(R.string.msgResolution) + resolution +            //Actualitzem el textView amb les noves dades
                    getString(R.string.msgMaxRange) + maxRange +
                    getString(R.string.msgPower) + power;
            viewAccel.setText(textAccel);

            sensorManager.registerListener(this, sensorAccelerometre,           //Registrem el Listener del sensor amb un delay NORMAL
                    SensorManager.SENSOR_DELAY_NORMAL);
            // register this class as a listener for the accelerometer sensor
        }else{
            viewAccel.setText(R.string.noAccel);                //En cas que la referencia sigui null printem el següent missatge
        }

        if(sensorLight != null){
            String textLight;
            maxRange = sensorLight.getMaximumRange();                                       //Si la referencia al sensor es diferent de null
            textLight = viewLight.getText().toString();
            textLight = textLight + getString(R.string.msgMaxRange)+ maxRange;              //Agafem el valor del maxRange i actualitzem el textview
            viewLight.setText(textLight);
            lValue = 0;
            sensorManager.registerListener(this, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);  //Registrem el Listener del sensor amb un delay NORMAL
        }
        else{
            viewLight.setText(R.string.noLightSensor);                      //En cas que la referencia sigui null printem el següent missatge
        }

        lastUpdateAccel = System.currentTimeMillis();                   //Retornem el temps actual en milisegons
        lastUpdateLight = System.currentTimeMillis();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:                             //Establim els diferents casos segons els events del sensors que volem controlar
                getAccelerometer(event);                                //En cas que salti un event de l'accelerometre
                break;
            case Sensor.TYPE_LIGHT:
                getLight(event);                                        //En cas que salti un event del sensor de llum
                break;
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = System.currentTimeMillis();
        if (accelationSquareRoot >= 2)
        {
            if (actualTime - lastUpdateAccel < maxTimeAccel) {
                return;
            }
            lastUpdateAccel = actualTime;

            Toast.makeText(this, R.string.shuffed, Toast.LENGTH_SHORT).show();
            if (color) {
                view.setBackgroundColor(Color.GREEN);

            } else {
                view.setBackgroundColor(Color.RED);
            }
            color = !color;
        }
    }

    private void getLight(SensorEvent event) {
        float[] values = event.values;
        float light = values[0];                                    //Ens quedem amb el valor de la variable x
        long actualTime = System.currentTimeMillis();
        if (diffValue(lValue, light)){                              //Comprovem si hi ha hagut un canvi de intensitat
            lValue = light;                                         //Guardem el nou valor
            if ((actualTime - lastUpdateLight) < maxTimeLight){
                return;
            }
            lastUpdateLight = actualTime;                           //En cas d'haver superat el temps per una nova actualització
            String textLight = viewLight.getText().toString();              //Actualitzem el textView amb un nou missatge
            textLight = textLight + getString(R.string.txtValue) + lValue +
                    getString(R.string.hopLine) +
                    getIntensity(lValue, maxRange)+ getString(R.string.txtIntensity);
            viewLight.setText(textLight);
        }
    }

    private String getIntensity(float lValue, float maxRange) {
        float minim = maxRange/3;
        float maxim = minim * 2;

        if (lValue < minim){                                   //Si l'intensitat de la llum es menor que minim mostra LOW
            return getString(R.string.low);
        }
        else if (lValue >= minim && lValue < maxim){     //Si l'intensitat de la llum esta entre minim i maxim mostra MEDIUM
            return getString(R.string.medium);
        }
        else return getString(R.string.high);                                           //Si l'intensitat de la llum es major que maxim mostra HIGH
    }

    public boolean diffValue(float x, float y){
        return Math.abs(x - y) >= 2000;                 //Retorna true en cas que la diferencia entre les dues variables sigui >= 2000
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this, sensorAccelerometre);            //Cancelem el Listener de l'Accelerometre
        sensorManager.unregisterListener(this, sensorLight);                    //Cancelem el Listener del sensor de llum
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);           //Registrem el Listener de l'Accelerometre
        sensorManager.registerListener(this, sensorAccelerometre, SensorManager.SENSOR_DELAY_NORMAL);   //Registrem el Listener del sensor de llum
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this,sensorAccelerometre);             //Cancelem el Listener de l'Accelerometre
        sensorManager.unregisterListener(this,sensorLight);                     //Cancelem el Listener del sensor de llum
    }
}