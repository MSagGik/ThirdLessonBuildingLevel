package com.msaggik.thirdlessonbuildinglevel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // создание полей для сенсоров
    private SensorManager sensorManager; // менеджер сенсоров устройства (даёт доступ к нужному датчику)
    private Sensor senAccelerometer; // создание поля акселерометра
    private Sensor senGyroscope; // создание поля гироскопа
    private Sensor senRotationVector; // создание поля виртуального датчика на основе акселерометра и гироскопа
    // создание полей для экрана
    private TextView accelerometer; // поле вывода данных акселерометра
    private TextView gyroscope; // поле вывода данных гироскопа
    private TextView rotation; // поле вывода данных виртуального датчика наклона по оси
    private ImageView levelBuild; // поле строительного уровня

    // создание вспомогательных полей для сенсоров
    private float accelerometerX, accelerometerY, accelerometerZ; // поля ускорения по осям X, Y и Z акселерометра
    private float gyroscopeX, gyroscopeY, gyroscopeZ; // поля скорости вращения по осям X, Y и Z гироскопа

    // создание поля слушателя сенсоров
    private SensorEventListener sensorEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // присвоение идентификаторов представления activity_main соответствующим полям
        accelerometer = findViewById(R.id.accelerometer); // окно вывода данных акселерометра
        gyroscope = findViewById(R.id.gyroscope); // окно вывода данных гироскопа
        rotation = findViewById(R.id.rotation); // окно вывода данных виртуального датчика наклона по оси
        levelBuild = findViewById(R.id.levelBuild); // окно строительного уровня

        // получение доступа к сенсорам
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // инициализация сенсоров
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // инициализация акселерометра
        senGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE); // инициализация гироскопа
        senRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR); // инициализация виртуального датчика определения наклона по оси

        // инициализация слушателя
        sensorEventListener = new SensorEventListener() {
            // метод onSensorChanged() вызывается всякий раз, когда изменяется значение датчика
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                // получаем мультиссылку на сенсоры
                Sensor multiSensor = sensorEvent.sensor;

                switch (multiSensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        // инициализация определения положения устройства акселерометром
                        accelerometerX = sensorEvent.values[0]; // ось X (поперечная)
                        accelerometerY = sensorEvent.values[1]; // ось Y (продольная)
                        accelerometerZ = sensorEvent.values[2]; // ось Z (вертикальная)
                        // вывод на экран определённых значений и округление с помощью метода format()
                        accelerometer.setText("Данные акселерометра (м|c2):\nx = " + String.format("%.2f", accelerometerX)
                                + "\ny = " + String.format("%.2f", accelerometerY)
                                + "\nz = " + String.format("%.2f", accelerometerZ));
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        // инициализация определения положения устройства гироскопом
                        gyroscopeX = sensorEvent.values[0]; // ось X (поперечная)
                        gyroscopeY = sensorEvent.values[1]; // ось Y (продольная)
                        gyroscopeZ = sensorEvent.values[2]; // ось Z (вертикальная)
                        // вывод на экран определённых значений и округление с помощью метода format()
                        gyroscope.setText("Данные гироскопа (рад|c):\nx = " + String.format("%.2f", gyroscopeX)
                                + "\ny = " + String.format("%.2f", gyroscopeY)
                                + "\nz = " + String.format("%.2f", gyroscopeZ));
                        break;
                    case Sensor.TYPE_ROTATION_VECTOR:
                        // инициализация и вывод на экран угловых значений относительно одной оси и округление с помощью метода format()
                        rotation.setText("Данные угла наклона уровня:\n" + String.format("%.2f", vectorToDegree(sensorEvent)) + " градусов");
                        // синхронизация поворота изображения строительного уровня в зависимости от показаний датчика
                        levelBuild.setRotation(-vectorToDegree(sensorEvent));
                        break;
                }
            }

            // метод onAccuracyChanged() вызывается при изменении точности показаний датчика
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        // регистрация сенсоров (задание слушателя)
        sensorManager.registerListener(sensorEventListener, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL); // (слушатель, сенсор - аксерометр, время обновления - среднее)
        sensorManager.registerListener(sensorEventListener, senGyroscope, SensorManager.SENSOR_DELAY_NORMAL); // (слушатель, сенсор - гироскоп, время обновления - среднее)
        sensorManager.registerListener(sensorEventListener, senRotationVector, SensorManager.SENSOR_DELAY_NORMAL); // (слушатель, сенсор - вир.датчик, время обновления - среднее)
    }

    @Override
    protected void onPause() {
        super.onPause();
        // отзыв регистрации сенсоров (отключение слушателя)
        sensorManager.unregisterListener(sensorEventListener);
    }

    // метод математических операций перевода векторных значений в градусы (векторные значения -> матричные -> радиальные -> градусы)
    private float vectorToDegree(SensorEvent event) {
        // создание матрицы типа float с 16 ячейками для матричных значений
        float[] rotationMatrix = new float[16];
        // перевод векторных значений в матричные
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        // создание матрицы типа float с 16 ячейками для радиальных значений
        float[] remappedRotationMatrix = new float[16];
        // перевод матричных значений в радиальные
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix);
        // создание матрицы типа float с 3 ячейками для угловых значений относительно осей X, Y и Z
        float[] orientations = new float[3];
        // перевод радиальных значений в угловые (градусы)
        SensorManager.getOrientation(remappedRotationMatrix, orientations);
        for( int i = 0; i < 3; i++) {
            orientations[i] = (float) (Math.toDegrees(orientations[i]));
        }
        return orientations[2]; // вывод ориентации уровня относительно одной оси
    }
}