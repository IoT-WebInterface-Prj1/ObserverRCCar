package Mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

public class MqttHelper {
    private static final String TAG = MqttHelper.class.getSimpleName();
    private static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
    private static final String TOPIC = "RC_CAR";
    private static MqttHelper instance;
    private MqttAndroidClient mqttAndroidClient;

    private MqttHelper(Context context) {
        mqttAndroidClient = new MqttAndroidClient(context, BROKER_URL, MqttClient.generateClientId());
    }

    public static synchronized MqttHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MqttHelper(context);
        }
        return instance;
    }

    public void connect() throws MqttException {
        IMqttToken token = mqttAndroidClient.connect();
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "onSuccess");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d(TAG, "onFailure");
            }
        });
    }

    public void disconnect() throws MqttException {
        mqttAndroidClient.disconnect();
    }

    public void publish(String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        mqttAndroidClient.publish(TOPIC, message);
    }

    public void subscribe(String topic) throws MqttException {
        mqttAndroidClient.subscribe(topic, 0, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                Log.d(TAG, "messageArrived: " + topic + " : " + payload);
            }
        });
    }
}
