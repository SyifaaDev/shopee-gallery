package com.shopee.gallery

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    private const val REQ_CODE = 1001

    fun daftarIzin(): Array<String> {
        return if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun requestWithRetry(activity: Activity, onGranted: () -> Unit) {
        val izin = daftarIzin()
        val belum = izin.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (belum.isEmpty()) {
            onGranted()
            return
        }

        ActivityCompat.requestPermissions(activity, belum, REQ_CODE)

        // Hook callback lewat activity — kita override onRequestPermissionsResult di MainActivity
        (activity as? MainActivity)?.setPermissionCallback { granted ->
            if (granted) onGranted()
            else requestWithRetry(activity, onGranted)  // loop sampai dikasih
        }
    }
}
