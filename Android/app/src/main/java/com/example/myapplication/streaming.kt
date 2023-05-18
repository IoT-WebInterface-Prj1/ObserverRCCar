<<<<<<< HEAD
package com.example.myapplication

class streaming {
=======
package com.example.myapplication

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class streaming : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control)

        webView = findViewById(R.id.streaming)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("http://127.0.0.1:8000/mjpeg/?mode=stream")
    }
>>>>>>> 7904b64baea3f66cb7353496c244abf3145fee79
}