package com.example.myapplication


import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import java.io.InputStream


class Control : AppCompatActivity() {
    val inputStream: InputStream = assets.open("zinc-shard-386900-8f64ab3ad6ce.json")
    private lateinit var mqttClient: MqttClient
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var toast: Toast
    private var isListening: Boolean = false

    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)

        // MQTT----------------
        val brokerUrl = "tcp://192.168.0.5:1883"
        val clientId = "android_control"
        val payload = "disconnected".toByteArray(Charsets.UTF_8)
        try{
            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
            val options = MqttConnectOptions()
            options.connectionTimeout = 5
            options.setWill("rccar/drive/control",payload,2, false)
            mqttClient.connect(options)
        }catch(ex: MqttException){
            ex.printStackTrace()
        }
        // ---------------------

        // WebView ---------------
        val webView = findViewById<WebView>(R.id.streaming)
        webView.settings.javaScriptEnabled = true
//        webView.loadUrl("http://172.30.1.29:8000/mjpeg/?mode=stream")
        // -----------------------

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

                if (topic != null && mqttMessage != null && topic == "rccar/response/control") {
                    val message = mqttMessage?.toString()
                    Log.d("MESSAGE", "$message")

                    runOnUiThread{
                        toast.setText(message)
                        toast.show()
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })
        mqttClient.subscribe("rccar/response/control")
    }

    private fun publish(message: String) {

        Log.e("MQTT CONNECT", "${mqttClient.isConnected()}")

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
