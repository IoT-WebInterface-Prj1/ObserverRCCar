package com.example.myapplication
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions


class MainActivity : AppCompatActivity() {
    val TAG = "[[MainActivity]]"
    // MQTT ---
    private lateinit var mqttClient: MqttClient
    var isBoot = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // ---------------------
//          172.30.1.57

        val brokerUrl = "tcp://172.30.1.20:1883" // 같은 와이파이 ip주소를 할당받아야
        val clientId = "android_boot"
        try{
            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
            val options = MqttConnectOptions()
            options.connectionTimeout = 5
            mqttClient.connect(options)
        }catch(ex: MqttException){
            ex.printStackTrace()
        }

        val startButton = findViewById<ImageView>(R.id.boot)
        val bootTxt = findViewById<TextView>(R.id.txtBootState)
        val controlButton = findViewById<Button>(R.id.btnControl)
        val stateButton = findViewById<Button>(R.id.btnMediaView)

        startButton.setOnClickListener {
            val topic = "rccar/drive/boot"
            val bootState = bootTxt.text.toString()
            var message = "off"

            if (bootState == "BOOT ON") isBoot = false  // boot on인경우 클릭 -> boot off 되어야 한다
            else { // boot off 버튼을 클릭 -> boot on 되어야 한다
                isBoot = true
                message = "on"
            }

            val mqttMessage = MqttMessage(message.toByteArray())
            mqttClient.publish(topic, mqttMessage)
        }

        controlButton.setOnClickListener {
            if (isBoot) {
                val intent = Intent(this@MainActivity, Control::class.java)
                startActivity(intent)
            }
        }

        stateButton.setOnClickListener {
            val intent = Intent(this@MainActivity, MediaView::class.java)
            startActivity(intent)
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

                    runOnUiThread {
                        if (success) { // 정상적으로 mqtt 처리가 되었으면 시동 상태 사용자에게 표기
                            if (isBoot) {
                                bootTxt.text = "BOOT ON"
                                controlButton.setEnabled(true)
                            }
                            else {
                                bootTxt.text = "BOOT OFF"
                                controlButton.setEnabled(false)
                            }
                        }
                        else isBoot = !isBoot // 정상적으로 처리되지 않았으므로 시동 상태 되돌리기
                    }
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })
        mqttClient.subscribe("rccar/response/boot")
    }

    private fun startEngine(successMessage: MqttMessage) : Boolean {
        val success = successMessage.toString()

        return success == "success"
    }
}