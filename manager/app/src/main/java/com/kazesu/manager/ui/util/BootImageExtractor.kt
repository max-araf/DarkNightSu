package com.kazesu.manager.ui.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DarkNightSu - raw partition extractor (init_boot / boot / vendor_boot / recovery).
 * REQUIRES root from any manager. Output goes to Download/DarkNightSu/.
 */
object BootImageExtractor {

    val TARGETS = listOf("init_boot", "boot", "vendor_boot", "recovery")

    data class PartitionInfo(val name: String, val devicePath: String, val sizeBytes: Long)

    data class ExtractResult(
        val success: Boolean,
        val partition: String,
        val savedAs: String?,
        val uri: Uri?,
        val sizeBytes: Long,
        val sha256: String?,
        val magic: String?,
        val error: String?,
    )

    private fun sh(cmd: String): Shell.Result = Shell.cmd(cmd).exec()

    private fun byNameDir(): String? {
        listOf("/dev/block/by-name", "/dev/block/bootdevice/by-name").forEach {
            if (sh("[ -d '$it' ]").isSuccess) return it
        }
        return sh("ls -d /dev/block/platform/*/by-name /dev/block/platform/*/*/by-name 2>/dev/null | head -n 1")
            .out.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    }

    suspend fun listPartitions(): List<PartitionInfo> = withContext(Dispatchers.IO) {
        val dir = byNameDir() ?: return@withContext emptyList()
        val slot = sh("getprop ro.boot.slot_suffix").out.firstOrNull()?.trim().orEmpty()
        val result = ArrayList<PartitionInfo>()
        for (name in TARGETS) {
            val candidates = if (slot.isNotEmpty()) listOf(name + slot, name) else listOf(name)
            for (c in candidates) {
                val path = "$dir/$c"
                if (sh("[ -e '$path' ]").isSuccess) {
                    val size = sh("blockdev --getsize64 '$path' 2>/dev/null").out
                        .firstOrNull()?.trim()?.toLongOrNull() ?: 0L
                    result.add(PartitionInfo(c, path, size))
                    break
                }
            }
        }
        result
    }

    suspend fun extract(context: Context, p: PartitionInfo): ExtractResult = withContext(Dispatchers.IO) {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${p.name}_${Build.DEVICE}_$stamp.img"
        val tmp = File(context.cacheDir, "dn_extract_${p.name}.img")
        tmp.delete()
        try {
            sh("dd if='${p.devicePath}' of='${tmp.absolutePath}' bs=1048576 2>&1")
            sh("chmod 644 '${tmp.absolutePath}'")

            // Fallback: app cannot read the root-created file -> copy straight to Download via root.
            if (!tmp.exists() || !tmp.canRead() || tmp.length() == 0L) {
                val dest = "/sdcard/Download/DarkNightSu"
                sh("mkdir -p '$dest'")
                val r = sh("dd if='${p.devicePath}' of='$dest/$fileName' bs=1048576 2>&1")
                val ok = r.isSuccess && sh("[ -s '$dest/$fileName' ]").isSuccess
                return@withContext if (ok) {
                    ExtractResult(true, p.name, "Download/DarkNightSu/$fileName", null, p.sizeBytes, null, null, null)
                } else {
                    ExtractResult(false, p.name, null, null, 0, null, null, r.out.lastOrNull() ?: "dd failed")
                }
            }

            val size = tmp.length()
            if (p.sizeBytes > 0 && size != p.sizeBytes) {
                return@withContext ExtractResult(false, p.name, null, null, size, null, null,
                    "Size mismatch: got $size bytes, expected ${p.sizeBytes}.")
            }
            val magic = tmp.inputStream().use { ins ->
                val b = ByteArray(8)
                val n = ins.read(b)
                if (n == 8) String(b, Charsets.US_ASCII) else ""
            }
            val md = MessageDigest.getInstance("SHA-256")
            tmp.inputStream().use { ins ->
                val buf = ByteArray(1 shl 16)
                while (true) {
                    val n = ins.read(buf)
                    if (n <= 0) break
                    md.update(buf, 0, n)
                }
            }
            val sha = md.digest().joinToString("") { "%02x".format(it) }

            var uri: Uri? = null
            val shown: String
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/DarkNightSu")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext ExtractResult(false, p.name, null, null, size, sha, magic, "Could not create file in Downloads.")
                resolver.openOutputStream(uri)?.use { out -> tmp.inputStream().use { it.copyTo(out) } }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                shown = "Download/DarkNightSu/$fileName"
            } else {
                val dest = "/sdcard/Download/DarkNightSu"
                sh("mkdir -p '$dest'")
                sh("cp '${tmp.absolutePath}' '$dest/$fileName'")
                shown = "Download/DarkNightSu/$fileName"
            }
            ExtractResult(true, p.name, shown, uri, size, sha, magic, null)
        } catch (t: Throwable) {
            ExtractResult(false, p.name, null, null, 0, null, null, t.message ?: "extract failed")
        } finally {
            tmp.delete()
        }
    }

    /** "ANDROID!" = boot / init_boot, "VNDRBOOT" = vendor_boot. Anything else: warn, do not block. */
    fun magicLooksValid(magic: String?): Boolean = magic == "ANDROID!" || magic == "VNDRBOOT"
}
