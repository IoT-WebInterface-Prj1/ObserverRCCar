package com.example.myapplication

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class Streaming : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        webView = findViewById(R.id.streaming)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("http://www.naver.com")
    }
}
//172.30.1.29:8000/mjpeg/?mode=stream