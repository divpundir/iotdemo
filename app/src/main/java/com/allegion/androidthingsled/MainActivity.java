package com.allegion.androidthingsled;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;

public class MainActivity extends AppCompatActivity  implements MqttCallback{

    public static final String LED_PIN = "BCM22"; //physical pin #15
    private Gpio ledPin;
    private String TAG = MainActivity.class.getSimpleName();
    private TextView ledStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ledStatus = (TextView) findViewById(R.id.led_status);
        try {
            MqttClient client = new MqttClient("tcp://10.200.131.119:1883", "AndroidThingSub", new MemoryPersistence());
            client.setCallback(this);
            client.connect();

            String topic = "topic/led";
            client.subscribe(topic);

        } catch (MqttException e) {
            e.printStackTrace();
        }

        PeripheralManagerService service = new PeripheralManagerService();
        try {
            // Create GPIO connection for LED.
            ledPin = service.openGpio(LED_PIN);
            // Configure as an output.
            ledPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost....");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        Log.d(TAG, payload);
        switch (payload) {
            case "on":
                Log.d(TAG, "LED ON");
                ledPin.setValue(true);
//                ledStatus.setText("LED turned ON");
                break;

            case "off":
                Log.d(TAG, "LED OFF");
                ledPin.setValue(false);
//                ledStatus.setText("LED turned OFF");
                break;

            case "diwali":
                for(int i= 0;i<=10;i++){
                    ledPin.setValue(true);
                    Thread.sleep(250);
                    ledPin.setValue(false);
                    Thread.sleep(250);
//                    ledStatus.setText("LED set to blinking mode");
                }
                break;

            default:
                Log.d(TAG, "Message not supported!");
                ledStatus.setText("LED command is not recognized!");
                break;
        }


    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete....");
    }
}
