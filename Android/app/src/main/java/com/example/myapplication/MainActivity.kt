package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions


class MainActivity : AppCompatActivity() {
    val TAG = "[[MainActivity]]"
    // MQTT ---
    private lateinit var mqttClient: MqttClient

    // 자동로그인 ---
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 카카오 키 해시 Log -> 개발자 마다 등록해 주어야 함
        var keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)
        // 카카오 SDK 초기화
        KakaoSdk.init(this,"jeSpIpID0RZjHGncVuOXRRTCY4=")

        // SharedPreferences --- 로그인여부와 token 받아오기
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        editor = sharedPreferences.edit()

        var isLogin = sharedPreferences.getBoolean("is_login", true)
        var getToken = sharedPreferences.getString("get_token", "")
        Log.e(TAG, "${isLogin}")
        Log.e(TAG, "${getToken}")
        // ---------------------
//          172.30.1.57

        val brokerUrl = "tcp://172.30.1.75:1883" // 같은 와이파이 ip주소를 할당받아야
        val clientId = "android"
        try{
            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
            val options = MqttConnectOptions()
            options.connectionTimeout = 5
            mqttClient.connect(options)
        }catch(ex: MqttException){
            ex.printStackTrace()
        }
        // kakao Button 은 Visible, startButton 은 Gone 이 default
        val startButton = findViewById<Button>(R.id.boot)
        val kakaoButton = findViewById<ImageView>(R.id.btnKakaoLogin)
        val txtLogin = findViewById<TextView>(R.id.txtLoginWarn)

        if (isLogin == false){ // 로그인이 되어있지 않은경우 로그인 하도록 함
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    editor.putBoolean("is_login", false)
                    editor.putString("get_token", "")
                    editor.commit()
                    Log.e(TAG, "Kakao Account Login Fail", error)
                }
                else if (token != null) { // 제대로 로그인이 되었다면 login 여부와 token 을 저장
                    editor.putBoolean("is_login", true)
                    isLogin = true
                    editor.putString("get_token", token?.accessToken)
                    editor.commit()
                    Log.i(TAG, "Login Success with Account! ${token.accessToken}")
                }
            }

            kakaoButton.setOnClickListener { // 로그인 버튼 클릭시
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) { // 카카오톡 어플이 있다면 어플로 로그인
                    UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                        if (error != null) { // error 처리
                            Log.e(TAG, "Login FAIL", error)

                            // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                            // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
//                            if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
//                                return@kakaoButton
//                            }

                            // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                        }
                        else if (token != null) { // Login 성공 !
                            editor.putBoolean("is_login", true)
                            isLogin = true
                            editor.putString("get_token", token?.accessToken)
                            editor.commit()
                            Log.i(TAG, "Login Success with KakaoTalk ! ${token?.accessToken}")
                        }
                    }
                }
                else { // 어플이 없다면 계정으로 로그인
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                }
            }
        }
        // ----------------------- Kakao Login 확인

        Log.d("IS LOGIN", "${isLogin}")
        // 로그인 상태이면 카카오로그인 버튼, 텍스트를 숨기기
        // boot 버튼 불러오기
        if (isLogin) {
            kakaoButton.visibility = View.GONE
            txtLogin.visibility = View.GONE

            startButton.visibility = View.VISIBLE

            startButton.setOnClickListener {
                val topic = "rccar/drive/boot"
                val message = "on"
                val mqttMessage = MqttMessage(message.toByteArray())
                mqttClient.publish(topic, mqttMessage)
            }
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

                    if (success) {
                        runOnUiThread {
                            val intent = Intent(this@MainActivity, Control::class.java)
                            intent.putExtra("success", success)
                            startActivity(intent)
                        }
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