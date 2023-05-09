package Mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttHelper private constructor(context: Context) {
    private val TAG = MqttHelper::class.simpleName
    private val BROKER_URL = "tcp://localhost:1883"
    private val mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(context, BROKER_URL, MqttClient.generateClientId())

    companion object {
        private var instance: MqttHelper? = null
        private val TOPIC = "car-control"


        @Synchronized
        fun getInstance(context: Context): MqttHelper {
            if (instance == null) {
                instance = MqttHelper(context)
            }
            return instance!!
        }
    }

    fun connect() {
        try {
            val token = mqttAndroidClient.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(TAG, "onSuccess")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(TAG, "onFailure")
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            mqttAndroidClient.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(payload: String) {
        try {
            val message = MqttMessage(payload.toByteArray())
            mqttAndroidClient.publish(TOPIC, message)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String) {
        try {
            mqttAndroidClient.subscribe(topic, 0, object : IMqttMessageListener {
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = String(message!!.payload)
                    Log.d(TAG, "messageArrived: $topic : $payload")

                    when (topic) {
                        "right" -> doRightTurn()
                        "left" -> doLeftTurn()
                        "forward" -> doForward()
                        "backward" -> doBackward()
                    }
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun doRightTurn() {
        // 우회전
        Log.d(TAG, "doRightTurn")
    }

    fun doLeftTurn() {
        // 좌회전
        Log.d(TAG, "doLeftTurn")
    }

    fun doForward() {
        // 전진
        Log.d(TAG, "doForward")
    }

    fun doBackward() {
        // 후진
        Log.d(TAG, "doBackward")
    }
}
