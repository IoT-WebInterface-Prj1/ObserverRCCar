package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions


class MainActivity : AppCompatActivity() {

    private lateinit var mqttClient: MqttClient

    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

//          172.30.1.57

            val brokerUrl = "tcp://172.30.1.18:1883" // 같은 와이파이 ip주소를 할당받아야
            val clientId = "android"
            try{
                mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
                val options = MqttConnectOptions()
                options.connectionTimeout = 5
                mqttClient.connect(options)
            }catch(ex: MqttException){
                ex.printStackTrace()
            }

            val startButton = findViewById<Button>(R.id.boot)
            startButton.text = getString(R.string.boot_button_text)
            startButton.setOnClickListener {
                val topic = "rccar/drive/boot"
                val message = "on"
                val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient.publish(topic, mqttMessage)
        }

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(throwable: Throwable?) {
                throwable?.printStackTrace()
                try {
                    mqttClient.reconnect()
                } catch(ex: MqttException){
                    ex.printStackTrace()
                }
            }

            override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
                if (topic != null && mqttMessage != null && topic == "rccar/response/boot") {
                    // 시동을 걸고, 성공 여부를 발행
                    val success = startEngine(mqttMessage)
                    val resultTopic = "rccar/response/boot"
                    val resultMessage = if (success) "on" else "off"
                    val resultMqttMessage = MqttMessage(resultMessage.toByteArray())
                    mqttClient.publish(resultTopic, resultMqttMessage)

                    runOnUiThread {
                        val intent = Intent(this@MainActivity, Control::class.java)
                        intent.putExtra("success", success)
                        startActivity(intent)
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })

        mqttClient.subscribe("rccar/response/boot")
    }

    private fun startEngine(success: MqttMessage?) : Boolean {
        if(success != null && success.toString() == "success"){
            return true
        }else{
            return false
        }

    }
}