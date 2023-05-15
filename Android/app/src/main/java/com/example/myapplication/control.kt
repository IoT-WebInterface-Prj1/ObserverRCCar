package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class control : AppCompatActivity() {

    private lateinit var mqttClient: MqttClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        val brokerUrl = "tcp://localhost:1883"
        val clientId = "android"
        mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
        mqttClient.connect()

        val rightButton = findViewById<Button>(R.id.right_button)
        rightButton.setOnClickListener {
            publish("right")
        }

        val leftButton = findViewById<Button>(R.id.left_button)
        leftButton.setOnClickListener {
            publish("left")
        }

        val forwardButton = findViewById<Button>(R.id.forward_button)
        forwardButton.setOnClickListener {
            publish("forward")
        }

        val backwardButton = findViewById<Button>(R.id.backward_button)
        backwardButton.setOnClickListener {
            publish("backward")
        }

        val recordButton = findViewById<Button>(R.id.record_button)
        recordButton.setOnClickListener {

        }

        mqttClient.subscribe("control")

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(throwable: Throwable?) {
                if (throwable != null){
                    throwable.printStackTrace()
                }
                try {
                    mqttClient.reconnect()
                } catch(ex: MqttException){
                    ex.printStackTrace()
                }
            }

            override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
                if (topic == "control") {
                    val message = mqttMessage?.toString()

                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })
    }

    private fun publish(message: String) {
        mqttClient.publish("control", MqttMessage(message.toByteArray()))
    }

}

