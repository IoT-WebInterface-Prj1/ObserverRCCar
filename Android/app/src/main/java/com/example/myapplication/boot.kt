package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


class boot : AppCompatActivity() {

    private lateinit var mqttClient: MqttClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val brokerUrl = "tcp://localhost:1883"
        val clientId = "android"
        mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
        mqttClient.connect()

        val startButton = findViewById<Button>(R.id.boot)
        startButton.setOnClickListener {
            val topic = "start"
            val message = "boot"
            val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient.publish(topic, mqttMessage)
        }

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
                if (topic == "start") {
                    // 시동을 걸고, 성공 여부를 발행
                    val success = startEngine()
                    val resultTopic = "start/result_boot"
                    val resultMessage = if (success) "success" else "fail"
                    val resultMqttMessage = MqttMessage(resultMessage.toByteArray())
                    mqttClient.publish(resultTopic, resultMqttMessage)

                    runOnUiThread {
                        val intent = Intent(this@boot, MainActivity::class.java)
                        intent.putExtra("success", success)
                        startActivity(intent)
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })

        mqttClient.subscribe("start")
    }

    private fun startEngine(): Boolean {
        // 스마트 RC카에서 시동을 걸고, 성공 여부를 반환
        return true
    }
}