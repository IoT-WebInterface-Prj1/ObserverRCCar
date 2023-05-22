package com.example.myapplication

import android.os.Bundle
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity

class Streaming : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        webView = findViewById(R.id.streaming)
        webView.settings.javaScriptEnabled = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webViewClient = object : WebViewClient(){
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError? ) {
                handler?.proceed()
            }
        }
        webView.loadUrl("http://172.30.1.120:8000/mjpeg/?mode=stream")
    }
}
/* 172.30.1.29:8000/mjpeg/?mode=stream */