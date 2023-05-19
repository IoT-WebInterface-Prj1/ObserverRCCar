package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class Control : AppCompatActivity() {
    private lateinit var mqttClient: MqttClient
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isListening: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)


        val brokerUrl = "tcp://172.30.1.38:1883"
        val clientId = "android"
        mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
        mqttClient.connect()


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onEndOfSpeech() {

            }

            override fun onBufferReceived(buffer: ByteArray?) {

            }

            override fun onError(error: Int) {

                publish("AUDIO_ERROR")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0]
                    if (spokenText.contains("시동")) {
                        publish("시동")
                    } else if (spokenText.contains("멈춰")) {
                        publish("멈춰")
                    }
                }

                isListening = false
            }

            override fun onRmsChanged(rmsdB: Float) {

            }

            override fun onPartialResults(partialResults: Bundle?) {

            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }
        })

        val recordButton = findViewById<Button>(R.id.record_button)
        recordButton.setOnClickListener {
            if (isListening) {
                stopListening()
            } else {
                startListening()
            }
        }

        mqttClient.subscribe("rccar/response/control")

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(throwable: Throwable?) {
                throwable?.printStackTrace()
                try {
                    mqttClient.reconnect()
                } catch (ex: MqttException) {
                    ex.printStackTrace()
                }
            }

            override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
                if (topic == "rccar/response/control") {
                    val message = mqttMessage?.toString()
                    Log.d("MESSAGE", "$message")
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

    private fun startListening() {
        val permission = Manifest.permission.RECORD_AUDIO
        val granted = PackageManager.PERMISSION_GRANTED
        val hasPermission = ContextCompat.checkSelfPermission(this, permission) == granted

        if (!hasPermission) {
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        speechRecognizer.startListening(intent)

        isListening = true
    }

    private fun stopListening() {
        if (isListening) {
            speechRecognizer.stopListening()
            speechRecognizer.cancel()
            speechRecognizer.destroy()

            isListening = false
        }
    }
}
