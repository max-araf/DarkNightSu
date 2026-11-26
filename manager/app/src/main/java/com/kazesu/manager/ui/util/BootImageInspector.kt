package com.kazesu.manager.ui.util

import android.content.Context
import android.net.Uri

/**
 * DarkNightSu - reads the Android boot image header so we can tell GKI 2.0 images (header v3 / v4)
 * from older non-GKI images (header v0-v2) BEFORE patching. A kernel string like "5.10.x" does NOT prove
 * the phone is GKI: header v2 means LKM patching cannot work ("bootimage version 2 is not supported").
 */
object BootImageInspector {

    enum class Kind { BOOT, INIT_BOOT, VENDOR_BOOT, UNKNOWN }

    enum class Problem {
        NOT_A_BOOT_IMAGE,      // wrong file picked
        ARCHIVE_OR_COMPRESSED, // zip / tar / lz4 / gzip (e.g. Samsung AP, OTA zip) -> extract first
        TOO_SMALL,
        VENDOR_BOOT_SELECTED,  // vendor_boot.img is not what you patch
        NON_GKI_HEADER,        // header v0..v2: LKM patch impossible, use AnyKernel3
    }

    data class Info(
        val valid: Boolean,
        val kind: Kind,
        val headerVersion: Int,
        val kernelSize: Long,
        val ramdiskSize: Long,
        /** true when header v3+ (GKI 2.0 format). */
        val gkiFormat: Boolean,
        val problem: Problem?,
    ) {
        /** True when the LKM "Select a file and patch" flow can work with this image. */
        val lkmPatchable: Boolean get() = valid && gkiFormat && problem == null
    }

    private fun u32(b: ByteArray, off: Int): Long =
        (b[off].toLong() and 0xff) or ((b[off + 1].toLong() and 0xff) shl 8) or
            ((b[off + 2].toLong() and 0xff) shl 16) or ((b[off + 3].toLong() and 0xff) shl 24)

    private fun bad(p: Problem) = Info(false, Kind.UNKNOWN, -1, 0, 0, false, p)

    /** [head] = first >= 64 bytes of the file. [totalSize] = file size if known, else -1. */
    fun inspect(head: ByteArray, totalSize: Long = -1): Info {
        if (totalSize in 0..4095 || head.size < 64) return bad(Problem.TOO_SMALL)
        val magic = String(head, 0, 8, Charsets.ISO_8859_1)
        // archives people pick by mistake
        if (head[0] == 'P'.code.toByte() && head[1] == 'K'.code.toByte()) return bad(Problem.ARCHIVE_OR_COMPRESSED)
        if ((head[0].toInt() and 0xff) == 0x1f && (head[1].toInt() and 0xff) == 0x8b) return bad(Problem.ARCHIVE_OR_COMPRESSED)
        if ((head[0].toInt() and 0xff) == 0x04 && (head[1].toInt() and 0xff) == 0x22 &&
            (head[2].toInt() and 0xff) == 0x4d && (head[3].toInt() and 0xff) == 0x18) return bad(Problem.ARCHIVE_OR_COMPRESSED)
        if (magic == "VNDRBOOT") return Info(false, Kind.VENDOR_BOOT, u32(head, 8).toInt(), 0, 0, true, Problem.VENDOR_BOOT_SELECTED)
        if (magic != "ANDROID!") return bad(Problem.NOT_A_BOOT_IMAGE)

        val version = u32(head, 40).toInt()
        if (version < 0 || version > 4) return bad(Problem.NOT_A_BOOT_IMAGE)
        val kernelSize = u32(head, 8)
        val ramdiskSize = if (version >= 3) u32(head, 12) else u32(head, 16)
        val kind = if (version >= 3 && kernelSize == 0L) Kind.INIT_BOOT else Kind.BOOT
        val gki = version >= 3
        return Info(true, kind, version, kernelSize, ramdiskSize, gki, if (gki) null else Problem.NON_GKI_HEADER)
    }

    /** Reads the picked file (content:// Uri) and inspects it. Never throws. */
    fun inspect(context: Context, uri: Uri): Info = try {
        context.contentResolver.openInputStream(uri)?.use { ins ->
            val buf = ByteArray(4096)
            var n = 0
            while (n < buf.size) {
                val r = ins.read(buf, n, buf.size - n)
                if (r <= 0) break
                n += r
            }
            val size = try {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            } catch (_: Throwable) {
                -1L
            }
            inspect(buf.copyOf(n), size)
        } ?: bad(Problem.NOT_A_BOOT_IMAGE)
    } catch (_: Throwable) {
        bad(Problem.NOT_A_BOOT_IMAGE)
    }

    /** Root only: header version of a partition (e.g. /dev/block/by-name/boot_a). null = unreadable. */
    fun headerVersionViaRoot(devicePath: String): Int? {
        val hex = RootExec.lines("dd if='$devicePath' bs=64 count=1 2>/dev/null | od -An -tx1 -v")
            .joinToString(" ").trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (hex.size < 44) return null
        val magic = String(ByteArray(8) { hex[it].toInt(16).toByte() }, Charsets.ISO_8859_1)
        if (magic != "ANDROID!") return null
        val v = (0 until 4).sumOf { i -> (hex[40 + i].toInt(16).toLong() and 0xff) shl (8 * i) }.toInt()
        return if (v in 0..4) v else null
    }
}
