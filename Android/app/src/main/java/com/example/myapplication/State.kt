package com.example.myapplication

import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class State : AppCompatActivity()  {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.state)

        // WebView ---------------
        webView = findViewById<WebView>(R.id.stateView)
        webView.settings.javaScriptEnabled = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webViewClient = object : WebViewClient(){
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError? ) {
                handler?.proceed()
            }
        }
        webView.loadUrl("http://172.30.1.120:8000/state")
        // -----------------------
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}