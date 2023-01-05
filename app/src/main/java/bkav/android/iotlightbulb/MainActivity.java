package bkav.android.iotlightbulb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import bkav.android.iotlightbulb.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    final String CLIENT_ATTR_TOPIC = "v1/devices/me/attributes";
    final String DEVICE_ACCESS_TOKEN = "wgUwf4sxGxHFNnTpqusu";

    ActivityMainBinding mBinding;
    String mClientId = MqttClient.generateClientId();
    MqttAndroidClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.switch1.setOnCheckedChangeListener((compoundButton, b) -> {
            if(mClient != null && mClient.isConnected()){
                publish(CLIENT_ATTR_TOPIC, ("{\"isTurningOn\":" + b + "}").getBytes());
            }
        });

        connectThingsboard("tcp://demo.thingsboard.io:1883", DEVICE_ACCESS_TOKEN);
    }

    public void publish(String topic, byte[] message){
        try {
            mClient.publish(topic, message, 0, false);
        } catch (MqttException e) {
            Log.d("PhucDVb", "publish: ", e);
            e.printStackTrace();
        }
    }

    public void subscribe(String topic){
        try {
            mClient.subscribe(topic, 0);
        } catch (MqttException e) {
            Log.d("PhucDVb", "subscribe: ", e);
            e.printStackTrace();
        }
    }

    public void connectThingsboard(String serverUrl, String accessToken){
        mClient = new MqttAndroidClient(getApplicationContext(), serverUrl, mClientId);
        mClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("PhucDVb", "connectComplete: " + reconnect);
                Toast.makeText(MainActivity.this, reconnect ? "Reconnected" : "Connected", Toast.LENGTH_SHORT).show();
                publish(CLIENT_ATTR_TOPIC,  ("{\"isTurningOn\":" + mBinding.switch1.isChecked() + "}").getBytes());

                subscribe("v1/devices/me/rpc/request/+");
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d("PhucDVb", "connectionLost: ", cause);
                Toast.makeText(MainActivity.this, "Lost connection. Trying to reconnect", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("PhucDVb", "messageArrived: " + message.toString());
                mBinding.switch1.setChecked(isTuningOn(new String(message.getPayload())));
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
            mClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("PhucDVb", "onSuccess: " + asyncActionToken);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("PhucDVb", "onFailure: ", exception);
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public boolean isTuningOn(String json){
       try {
           String method = new JSONObject(json).getString("method");
           return "turnOn".equals(method);
        } catch (JSONException e) {
            e.printStackTrace();
        }
       return false;
    }
}