package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.webkit.WebView
import android.widget.Button
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*

class Control : AppCompatActivity() {

    private lateinit var mqttClient: MqttClient

    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

//        setContentView(binding.root)

        // MQTT----------------
        val brokerUrl = "tcp://172.30.1.38:1883"
        val clientId = "android"
        mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
        mqttClient.connect()
        // ---------------------

        // WebView ---------------
        val webView = findViewById<WebView>(R.id.streaming)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("http://172.30.1.29:8000/mjpeg/?mode=stream")
        // -----------------------

        // Button ------------------
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

//        val recordButton = findViewById<Button>(R.id.record_button)
//        recordButton.setOnClickListener {
//            if (isListening){
//                stopListening()
//            } else
//                startListening()
//
//        }
        // -------------------button

        mqttClient.subscribe("rccar/response/control")

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

                if (topic == "rccar/response/control") {
                    val message = mqttMessage?.toString()
                    Log.d("MESSAGE", "${message}")
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })
    }

    private fun publish(message: String) {

        mqttClient.publish("rccar/drive/control", MqttMessage(message.toByteArray()))
    }

//    private fun startListening() {
//        val permission = RECORD_AUDIO
//        val granted = PackageManager.PERMISSION_GRANTED
//        val hasPermission = ContextCompat.checkSelfPermission(this, permission) == granted
//
//        if (!hasPermission) {
//
//            return
//        }
//
//        speechRecognizer.setRecognitionListener(object : RecognitionListener {
//            override fun onReadyForSpeech(params: Bundle?) {
//                // 시작 준비가 완료되면 호출
//            }
//
//            override fun onBeginningOfSpeech() {
//                // 음성 인식이 시작될 때 호출
//            }
//
//            override fun onEndOfSpeech() {
//                // 음성 인식이 종료시 호출
//            }
//
//            override fun onError(error: Int) {
//                // 음성 인식 중에 오류가 발생하면 호출
//                publish("AUDIO_ERROR")
//            }
//
//            override fun onResults(results: Bundle?) {
//                // 음성 인식 결과가 준비되면 호출
//                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                if (matches != null && matches.isNotEmpty()) {
//                    val spokenText = matches[0]
//                    publish(spokenText)
//                }
//
//                isListening = false
//            }
//
//            override fun onPartialResults(partialResults: Bundle?) {
//                // 음성 인식 중에 부분 결과가 사용 가능하면 호출
//            }
//
//            override fun onEvent(eventType: Int, params: Bundle?) {
//                // 인식 이벤트가 발생할 때 호출
//            }
//        })
//
//        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
//        speechRecognizer.startListening(intent)
//
//        isListening = true
//    }
//
//    private fun stopListening() {
//        if (isListening) {
//            speechRecognizer.stopListening()
//            speechRecognizer.cancel()
//            speechRecognizer.destroy()
//
//            isListening = false
//        }
//    }
}