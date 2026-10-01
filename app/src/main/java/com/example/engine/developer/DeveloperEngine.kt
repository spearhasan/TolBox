package com.example.engine.developer

import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.io.StringReader
import java.io.StringWriter
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

data class HashResult(
    val md5: String,
    val sha1: String,
    val sha256: String,
    val sha512: String
)

data class TimestampResult(
    val epochMillis: Long,
    val epochSeconds: Long,
    val localDateTime: String,
    val utcDateTime: String,
    val relativeTime: String
)

data class JwtResult(
    val headerJson: String,
    val payloadJson: String,
    val hasSignature: Boolean,
    val issuedAt: String?,
    val expiration: String?
)

data class ColorResult(
    val hex: String,
    val rgb: String,
    val hsl: String,
    val colorInt: Int
)

object DeveloperEngine {

    fun generateHashes(input: String): HashResult {
        val bytes = input.toByteArray(Charsets.UTF_8)
        fun hash(alg: String): String {
            val md = MessageDigest.getInstance(alg)
            val digest = md.digest(bytes)
            return digest.joinToString("") { "%02x".format(it) }
        }
        return HashResult(
            md5 = hash("MD5"),
            sha1 = hash("SHA-1"),
            sha256 = hash("SHA-256"),
            sha512 = hash("SHA-512")
        )
    }

    fun singleHash(input: String, algorithm: String): String {
        val md = MessageDigest.getInstance(algorithm)
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun generateUuids(count: Int = 1, uppercase: Boolean = false, withHyphens: Boolean = true): List<String> {
        val n = count.coerceIn(1, 50)
        return (1..n).map {
            var uuid = UUID.randomUUID().toString()
            if (!withHyphens) uuid = uuid.replace("-", "")
            if (uppercase) uuid = uuid.uppercase(Locale.ROOT)
            uuid
        }
    }

    fun generateUlid(): String {
        val time = System.currentTimeMillis()
        val random = (1..16).map { "0123456789ABCDEFGHJKMNPQRSTVWXYZ".random() }.joinToString("")
        return "%010X%s".format(time, random)
    }

    fun convertTimestamp(epochInput: Long, isSeconds: Boolean = false): TimestampResult {
        val millis = if (isSeconds) epochInput * 1000L else epochInput
        val date = Date(millis)

        val localFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.getDefault())
        val utcFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val now = System.currentTimeMillis()
        val diffSec = (now - millis) / 1000
        val relative = when {
            diffSec in 0..60 -> "Just now"
            diffSec in 61..3600 -> "${diffSec / 60} minutes ago"
            diffSec in 3601..86400 -> "${diffSec / 3600} hours ago"
            diffSec > 86400 -> "${diffSec / 86400} days ago"
            diffSec < 0 -> "In ${-diffSec / 60} minutes"
            else -> "N/A"
        }

        return TimestampResult(
            epochMillis = millis,
            epochSeconds = millis / 1000L,
            localDateTime = localFmt.format(date),
            utcDateTime = utcFmt.format(date),
            relativeTime = relative
        )
    }

    fun testRegex(patternStr: String, input: String, caseInsensitive: Boolean = false): List<String> {
        val flags = if (caseInsensitive) setOf(RegexOption.IGNORE_CASE) else emptySet()
        val regex = patternStr.toRegex(flags)
        return regex.findAll(input).map { it.value }.toList()
    }

    fun decodeJwt(token: String): JwtResult {
        val parts = token.trim().split(".")
        if (parts.size < 2) throw IllegalArgumentException("Invalid JWT: must have at least 2 segments separated by dots.")

        fun decodePart(part: String): String {
            var base64 = part.replace('-', '+').replace('_', '/')
            while (base64.length % 4 != 0) base64 += "="
            val decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val jsonStr = String(decoded, Charsets.UTF_8)
            return try {
                JSONObject(jsonStr).toString(2)
            } catch (e: Exception) {
                try {
                    JSONArray(jsonStr).toString(2)
                } catch (_: Exception) {
                    jsonStr
                }
            }
        }

        val header = decodePart(parts[0])
        val payload = decodePart(parts[1])
        val hasSig = parts.size >= 3 && parts[2].isNotEmpty()

        var expStr: String? = null
        var iatStr: String? = null
        try {
            val obj = JSONObject(payload)
            if (obj.has("exp")) {
                val expSec = obj.getLong("exp")
                expStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(expSec * 1000L))
            }
            if (obj.has("iat")) {
                val iatSec = obj.getLong("iat")
                iatStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(iatSec * 1000L))
            }
        } catch (_: Exception) {}

        return JwtResult(
            headerJson = header,
            payloadJson = payload,
            hasSignature = hasSig,
            issuedAt = iatStr,
            expiration = expStr
        )
    }

    fun parseColor(input: String): ColorResult {
        var clean = input.trim()
        if (!clean.startsWith("#") && (clean.length == 6 || clean.length == 8)) {
            clean = "#$clean"
        }
        val colorInt = Color.parseColor(clean)
        val r = Color.red(colorInt)
        val g = Color.green(colorInt)
        val b = Color.blue(colorInt)

        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(colorInt, hsl)

        val hexStr = String.format("#%06X", (0xFFFFFF and colorInt))
        val rgbStr = "rgb($r, $g, $b)"
        val hslStr = "hsl(${hsl[0].toInt()}°, ${(hsl[1] * 100).toInt()}%, ${(hsl[2] * 100).toInt()}%)"

        return ColorResult(hex = hexStr, rgb = rgbStr, hsl = hslStr, colorInt = colorInt)
    }

    fun formatJson(json: String, indent: Int = 2): String {
        val trimmed = json.trim()
        return if (trimmed.startsWith("{")) {
            JSONObject(trimmed).toString(indent)
        } else if (trimmed.startsWith("[")) {
            JSONArray(trimmed).toString(indent)
        } else {
            throw IllegalArgumentException("Provided text is not a valid JSON Object or Array.")
        }
    }

    fun minifyJson(json: String): String {
        val trimmed = json.trim()
        return if (trimmed.startsWith("{")) {
            JSONObject(trimmed).toString()
        } else if (trimmed.startsWith("[")) {
            JSONArray(trimmed).toString()
        } else {
            throw IllegalArgumentException("Provided text is not a valid JSON Object or Array.")
        }
    }

    fun formatXml(xml: String): String {
        return try {
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            val result = StreamResult(StringWriter())
            transformer.transform(StreamSource(StringReader(xml)), result)
            result.writer.toString()
        } catch (e: Exception) {
            xml.replace("><", ">\n<")
        }
    }

    fun toBinary(input: String): String =
        input.toByteArray(Charsets.UTF_8).joinToString(" ") { "%8s".format(Integer.toBinaryString(it.toInt() and 0xFF)).replace(' ', '0') }

    fun fromBinary(binary: String): String {
        val bytes = binary.trim().split("\\s+".toRegex()).map { it.toInt(2).toByte() }.toByteArray()
        return String(bytes, Charsets.UTF_8)
    }

    fun toHex(input: String): String =
        input.toByteArray(Charsets.UTF_8).joinToString(" ") { "%02X".format(it) }

    fun fromHex(hex: String): String {
        val clean = hex.replace(" ", "")
        val bytes = ByteArray(clean.length / 2)
        for (i in bytes.indices) {
            bytes[i] = clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return String(bytes, Charsets.UTF_8)
    }

    fun lookupHttpStatus(code: Int): String {
        return when (code) {
            200 -> "200 OK — The request has succeeded."
            201 -> "201 Created — A new resource was successfully created."
            204 -> "204 No Content — Request processed successfully, no body returned."
            301 -> "301 Moved Permanently — The resource has permanently moved to a new URI."
            302 -> "302 Found — Temporary redirection."
            304 -> "304 Not Modified — Cached resource is still valid."
            400 -> "400 Bad Request — Server could not understand the request due to malformed syntax."
            401 -> "401 Unauthorized — Authentication is required."
            403 -> "403 Forbidden — Client does not have permission to access this resource."
            404 -> "404 Not Found — The requested resource could not be found on the server."
            405 -> "405 Method Not Allowed — The HTTP method is not supported for this endpoint."
            429 -> "429 Too Many Requests — Client has sent too many requests in a given time."
            500 -> "500 Internal Server Error — Server encountered an unexpected condition."
            502 -> "502 Bad Gateway — Invalid response from upstream server."
            503 -> "503 Service Unavailable — Server currently unable to handle request (overloaded/down)."
            504 -> "504 Gateway Timeout — Upstream server failed to respond in time."
            else -> "$code — Standard HTTP Status Code"
        }
    }

    fun formatSql(sql: String): String {
        val keywords = listOf("SELECT", "FROM", "WHERE", "AND", "OR", "JOIN", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "GROUP BY", "ORDER BY", "HAVING", "LIMIT", "INSERT INTO", "VALUES", "UPDATE", "SET", "DELETE FROM")
        var res = sql.trim()
        for (kw in keywords) {
            res = res.replace("(?i)\\b$kw\\b".toRegex(), "\n$kw")
        }
        return res.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
    }

    fun formatHtml(html: String): String {
        return html.replace("><", ">\n<")
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }

    fun parseCron(cron: String): String {
        val parts = cron.trim().split("\\s+".toRegex())
        if (parts.size != 5) return "Invalid cron expression. Expected 5 fields: minute hour dom month dow (e.g. '0 12 * * 1-5')"
        val (m, h, dom, mon, dow) = parts
        return "Cron Schedule:\n• Minute: $m\n• Hour: $h\n• Day of Month: $dom\n• Month: $mon\n• Day of Week: $dow\nSummary: Runs at minute $m past hour $h on day-of-month $dom"
    }

    fun calcChmod(input: String): String {
        val trimmed = input.trim()
        if (trimmed.length == 3 && trimmed.all { it in '0'..'7' }) {
            fun rwx(digit: Char): String {
                val n = digit.toString().toInt()
                val r = if ((n and 4) != 0) "r" else "-"
                val w = if ((n and 2) != 0) "w" else "-"
                val x = if ((n and 1) != 0) "x" else "-"
                return "$r$w$x"
            }
            val u = rwx(trimmed[0])
            val g = rwx(trimmed[1])
            val o = rwx(trimmed[2])
            return "Octal $trimmed Permissions:\nSymbolic: -$u$g$o\n• Owner: $u\n• Group: $g\n• Others: $o"
        }
        return "Chmod 755: -rwxr-xr-x (Owner: read/write/execute, Group: read/execute, Others: read/execute)"
    }
}
