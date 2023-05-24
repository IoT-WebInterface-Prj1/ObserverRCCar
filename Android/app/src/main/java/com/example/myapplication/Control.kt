package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.http.SslError
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
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.ImageView

class Control : AppCompatActivity() {
    private lateinit var mqttClient: MqttClient
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var toast: Toast
    private var isListening: Boolean = false
    private val RECORD_REQUEST_CODE = 101

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)

        // MQTT----------------
        val brokerUrl = "tcp://172.30.1.20:1883"
        val clientId = "android_control"
        val payload = "disconnected".toByteArray(Charsets.UTF_8)
        try{
            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
            val options = MqttConnectOptions()
            options.connectionTimeout = 5000
            options.setWill("rccar/drive/control",payload,2, false)
            mqttClient.connect(options)
        }catch(ex: MqttException){
            ex.printStackTrace()
        }
        // ---------------------

        // WebView ---------------
        webView = findViewById<WebView>(R.id.streaming)
        webView.settings.javaScriptEnabled = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webViewClient = object : WebViewClient(){
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError? ) {
                handler?.proceed()
            }
        }
        webView.loadUrl("http://172.30.1.120:8000/mjpeg/")
        // -----------------------
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onEndOfSpeech() {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onError(error: Int) {
                publish("AUDIO_ERROR")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0]
                    if (spokenText.contains("시동")) {
//                        publish("멈춰")
                        mqttClient.publish("rccar/drive/boot", MqttMessage("off".toByteArray()))

                    } else if (spokenText.contains("멈춰")) {
                        publish("stop")
                    }
                    else Log.d("SPEECH", "Somtheing ELSE")
                }

                isListening = false
            }

            override fun onPartialResults(p0: Bundle?) {}

            override fun onEvent(p0: Int, p1: Bundle?) {}

            override fun onRmsChanged(rmsdB: Float) {}
        }
        speechRecognizer.setRecognitionListener(recognitionListener)

        val recordButton = findViewById<ImageView>(R.id.record_button)
        val rightButton = findViewById<ImageView>(R.id.btnRight)
        val leftButton = findViewById<ImageView>(R.id.btnLeft)
        val forwardButton = findViewById<ImageView>(R.id.btnForward)
        val backwardButton = findViewById<ImageView>(R.id.btnBackward)
        val stopButton = findViewById<ImageView>(R.id.btnStop)

        // ---------Button click Listencer ---------
        recordButton.setOnClickListener {
            if (isListening) {
                stopListening()
            } else {
                startListening()
            }
        }

        rightButton.setOnClickListener {
            publish("right")
        }

        leftButton.setOnClickListener {
            publish("left")
        }

        forwardButton.setOnClickListener {
            publish("forward")
        }

        backwardButton.setOnClickListener {
            publish("backward")
        }

        stopButton.setOnClickListener {
            publish("stop")
        }
        // ---------End Button click Listencer ---------

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
        if (mqttClient.isConnected()) {
            mqttClient.publish("rccar/drive/control", MqttMessage(message.toByteArray()))
        } else {
            Log.e("MQTT", "Not connected")
        }
    }

    private fun startListening() {
        val permission = Manifest.permission.RECORD_AUDIO
        val granted = PackageManager.PERMISSION_GRANTED
        val hasPermission = ContextCompat.checkSelfPermission(this, permission) == granted
        val recordButton = findViewById<ImageView>(R.id.record_button)

        recordButton.setColorFilter(Color.parseColor("#55ff0000"))

        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), RECORD_REQUEST_CODE)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        speechRecognizer.startListening(intent)

        isListening = true
    }

    private fun stopListening() {
        if (isListening) {
            val recordButton = findViewById<ImageView>(R.id.record_button)

            recordButton.setColorFilter(Color.parseColor("#00000000"))
            speechRecognizer.stopListening()
            speechRecognizer.cancel()
            speechRecognizer.destroy()

            isListening = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            }
        }
    }

    override fun onDestroy() {
        webView.destroy()
        mqttClient.disconnect()
        super.onDestroy()
    }
}
