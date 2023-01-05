package bkav.android.iotlightbulb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import bkav.android.iotlightbulb.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String serverUri = "tcp://demo.thingsboard.io:1883";
        String clientId = MqttClient.generateClientId();
        String accessToken = "wgUwf4sxGxHFNnTpqusu";
        MqttAndroidClient client = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("PhucDVb", "connectComplete: " + reconnect);
                // Bkav PhucDVb: v1/devices/me/attributes
                try {
                    client.publish("v1/devices/me/attributes", "{\"isTurningOn\": false}".getBytes(), 0, false);
                } catch (MqttException e) {
                    Log.d("PhucDVb", "connectComplete: ", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d("PhucDVb", "connectionLost: ", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("PhucDVb", "messageArrived: " + message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("PhucDVb", "deliveryComplete: ");
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(accessToken);

        try {
            client.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("PhucDVb", "onSuccess: ");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("PhucDVb", "onFailure: ");
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }

        binding.switch1.setOnCheckedChangeListener((compoundButton, b) -> {

        });
    }
}