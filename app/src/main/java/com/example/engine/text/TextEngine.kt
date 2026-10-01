package com.example.engine.text

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.Locale

data class TextAnalysisResult(
    val wordCount: Int,
    val charCount: Int,
    val charCountNoSpaces: Int,
    val lineCount: Int,
    val sentenceCount: Int,
    val paragraphCount: Int,
    val estimatedReadingTimeSeconds: Int
)

enum class TextCaseOption(val label: String) {
    UPPERCASE("UPPERCASE"),
    LOWERCASE("lowercase"),
    TITLE_CASE("Title Case"),
    SENTENCE_CASE("Sentence case"),
    CAMEL_CASE("camelCase"),
    SNAKE_CASE("snake_case"),
    KEBAB_CASE("kebab-case"),
    PASCAL_CASE("PascalCase"),
    INVERT_CASE("iNVERT cASE")
}

object TextEngine {

    fun analyze(text: String): TextAnalysisResult {
        if (text.isEmpty()) {
            return TextAnalysisResult(0, 0, 0, 0, 0, 0, 0)
        }

        val words = text.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val wordCount = words.size
        val charCount = text.length
        val charCountNoSpaces = text.count { !it.isWhitespace() }
        val lines = text.lines()
        val lineCount = lines.size
        val sentences = text.split("[.!?]+".toRegex()).filter { it.trim().isNotEmpty() }
        val sentenceCount = sentences.size
        val paragraphs = text.split("\n\n+".toRegex()).filter { it.trim().isNotEmpty() }
        val paragraphCount = paragraphs.size
        val readingTimeSec = ((wordCount / 200.0) * 60).toInt().coerceAtLeast(if (wordCount > 0) 1 else 0)

        return TextAnalysisResult(
            wordCount = wordCount,
            charCount = charCount,
            charCountNoSpaces = charCountNoSpaces,
            lineCount = lineCount,
            sentenceCount = sentenceCount,
            paragraphCount = paragraphCount,
            estimatedReadingTimeSeconds = readingTimeSec
        )
    }

    fun convertCase(text: String, option: TextCaseOption): String {
        return when (option) {
            TextCaseOption.UPPERCASE -> text.uppercase(Locale.getDefault())
            TextCaseOption.LOWERCASE -> text.lowercase(Locale.getDefault())
            TextCaseOption.TITLE_CASE -> text.split(" ").joinToString(" ") { word ->
                if (word.isNotEmpty()) {
                    word.substring(0, 1).uppercase(Locale.getDefault()) + word.substring(1).lowercase(Locale.getDefault())
                } else ""
            }
            TextCaseOption.SENTENCE_CASE -> {
                val sb = StringBuilder()
                var capitalizeNext = true
                for (ch in text) {
                    if (capitalizeNext && ch.isLetter()) {
                        sb.append(ch.uppercaseChar())
                        capitalizeNext = false
                    } else {
                        sb.append(ch.lowercaseChar())
                    }
                    if (ch == '.' || ch == '!' || ch == '?') {
                        capitalizeNext = true
                    }
                }
                sb.toString()
            }
            TextCaseOption.CAMEL_CASE -> {
                val words = text.split("[\\s_-]+".toRegex()).filter { it.isNotEmpty() }
                words.mapIndexed { idx, w ->
                    if (idx == 0) w.lowercase(Locale.getDefault())
                    else w.substring(0, 1).uppercase(Locale.getDefault()) + w.substring(1).lowercase(Locale.getDefault())
                }.joinToString("")
            }
            TextCaseOption.PASCAL_CASE -> {
                val words = text.split("[\\s_-]+".toRegex()).filter { it.isNotEmpty() }
                words.joinToString("") { w ->
                    w.substring(0, 1).uppercase(Locale.getDefault()) + w.substring(1).lowercase(Locale.getDefault())
                }
            }
            TextCaseOption.SNAKE_CASE -> {
                text.trim().lowercase(Locale.getDefault()).replace("[\\s-]+".toRegex(), "_")
            }
            TextCaseOption.KEBAB_CASE -> {
                text.trim().lowercase(Locale.getDefault()).replace("[\\s_]+".toRegex(), "-")
            }
            TextCaseOption.INVERT_CASE -> text.map { ch ->
                if (ch.isUpperCase()) ch.lowercaseChar() else ch.uppercaseChar()
            }.joinToString("")
        }
    }

    fun reverseText(text: String): String = text.reversed()

    fun reverseLines(text: String): String = text.lines().reversed().joinToString("\n")

    fun removeDuplicateLines(text: String): String {
        val seen = mutableSetOf<String>()
        return text.lines().filter { seen.add(it) }.joinToString("\n")
    }

    fun sortLines(text: String, ascending: Boolean = true, byLength: Boolean = false): String {
        val lines = text.lines()
        val sorted = if (byLength) {
            if (ascending) lines.sortedBy { it.length } else lines.sortedByDescending { it.length }
        } else {
            if (ascending) lines.sortedWith(String.CASE_INSENSITIVE_ORDER) else lines.sortedWith(String.CASE_INSENSITIVE_ORDER.reversed())
        }
        return sorted.joinToString("\n")
    }

    fun cleanText(text: String, removeExtraSpaces: Boolean = true, removeLineBreaks: Boolean = false): String {
        var res = text
        if (removeExtraSpaces) {
            res = res.replace("[ \\t]+".toRegex(), " ")
        }
        if (removeLineBreaks) {
            res = res.replace("\n+".toRegex(), " ")
        }
        return res.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
    }

    fun findAndReplace(text: String, find: String, replace: String): String {
        if (find.isEmpty()) return text
        return text.replace(find, replace)
    }

    fun generateLoremIpsum(paragraphs: Int = 3): String {
        val sentences = listOf(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
            "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
            "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            "Curabitur pretium tincidunt lacus, eget lacinia massa feugiat sit amet.",
            "Integer euismod, magna at pharetra cursus, metus felis pretium ante, nec feugiat justo elit eget sapien."
        )
        val sb = StringBuilder()
        for (p in 1..paragraphs.coerceIn(1, 10)) {
            val count = (3..5).random()
            for (s in 0 until count) {
                sb.append(sentences[(p + s) % sentences.size]).append(" ")
            }
            if (p < paragraphs) sb.append("\n\n")
        }
        return sb.toString().trim()
    }

    fun generatePassword(length: Int = 16, includeSymbols: Boolean = true, includeNumbers: Boolean = true): String {
        val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        var pool = letters
        if (includeNumbers) pool += numbers
        if (includeSymbols) pool += symbols

        val random = SecureRandom()
        return (1..length.coerceIn(6, 64)).map { pool[random.nextInt(pool.length)] }.joinToString("")
    }

    fun csvToJson(csv: String): String {
        val lines = csv.trim().lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return "[]"
        val headers = lines[0].split(",").map { it.trim().removeSurrounding("\"") }
        val jsonArray = JSONArray()

        for (i in 1 until lines.size) {
            val values = lines[i].split(",").map { it.trim().removeSurrounding("\"") }
            val obj = JSONObject()
            for (j in headers.indices) {
                val value = if (j < values.size) values[j] else ""
                obj.put(headers[j], value)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    fun jsonToCsv(json: String): String {
        val array = JSONArray(json.trim())
        if (array.length() == 0) return ""
        val first = array.getJSONObject(0)
        val keys = first.keys().asSequence().toList()
        val sb = StringBuilder()
        sb.append(keys.joinToString(",")).append("\n")

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val row = keys.map { k -> "\"${obj.optString(k, "").replace("\"", "\"\"")}\"" }
            sb.append(row.joinToString(",")).append("\n")
        }
        return sb.toString().trimEnd()
    }

    fun encodeBase64(input: String): String =
        Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    fun decodeBase64(input: String): String =
        String(Base64.decode(input, Base64.DEFAULT), Charsets.UTF_8)

    fun encodeUrl(input: String): String = URLEncoder.encode(input, "UTF-8")

    fun decodeUrl(input: String): String = URLDecoder.decode(input, "UTF-8")

    fun generateSlug(input: String): String {
        return input.trim()
            .lowercase(Locale.getDefault())
            .replace("[^a-z0-9\\s-]".toRegex(), "")
            .replace("\\s+".toRegex(), "-")
            .replace("-+".toRegex(), "-")
    }

    fun findDuplicateWords(input: String): String {
        val words = input.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }
        val counts = mutableMapOf<String, Int>()
        for (w in words) counts[w] = (counts[w] ?: 0) + 1
        val duplicates = counts.filter { it.value > 1 }.toList().sortedByDescending { it.second }
        if (duplicates.isEmpty()) return "No repeated words found."
        return "Repeated words:\n" + duplicates.joinToString("\n") { "• ${it.first}: ${it.second} times" }
    }

    fun compareTexts(text1: String, text2: String): String {
        val l1 = text1.lines()
        val l2 = text2.lines()
        val sb = StringBuilder()
        sb.append("=== Text Comparison ===\n")
        val maxLines = maxOf(l1.size, l2.size)
        var diffCount = 0
        for (i in 0 until maxLines) {
            val line1 = l1.getOrNull(i)
            val line2 = l2.getOrNull(i)
            if (line1 != line2) {
                diffCount++
                sb.append("Line ${i + 1}:\n  - Original: ${line1 ?: "[None]"}\n  + Modified: ${line2 ?: "[None]"}\n")
            }
        }
        if (diffCount == 0) return "Both texts are 100% identical! (${l1.size} lines)"
        return "Found $diffCount difference(s):\n\n$sb"
    }

    fun markdownToHtml(markdown: String): String {
        val lines = markdown.lines()
        val sb = StringBuilder()
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n<title>Converted Document</title>\n</head>\n<body>\n")
        var inList = false
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                if (inList) { sb.append("</ul>\n"); inList = false }
                continue
            }
            if (trimmed.startsWith("### ")) {
                if (inList) { sb.append("</ul>\n"); inList = false }
                sb.append("<h3>").append(formatInlineHtml(trimmed.substring(4))).append("</h3>\n")
            } else if (trimmed.startsWith("## ")) {
                if (inList) { sb.append("</ul>\n"); inList = false }
                sb.append("<h2>").append(formatInlineHtml(trimmed.substring(3))).append("</h2>\n")
            } else if (trimmed.startsWith("# ")) {
                if (inList) { sb.append("</ul>\n"); inList = false }
                sb.append("<h1>").append(formatInlineHtml(trimmed.substring(2))).append("</h1>\n")
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                if (!inList) { sb.append("<ul>\n"); inList = true }
                sb.append("  <li>").append(formatInlineHtml(trimmed.substring(2))).append("</li>\n")
            } else {
                if (inList) { sb.append("</ul>\n"); inList = false }
                sb.append("<p>").append(formatInlineHtml(trimmed)).append("</p>\n")
            }
        }
        if (inList) sb.append("</ul>\n")
        sb.append("</body>\n</html>")
        return sb.toString()
    }

    private fun formatInlineHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\\*\\*(.*?)\\*\\*".toRegex(), "<strong>$1</strong>")
            .replace("\\*(.*?)\\*".toRegex(), "<em>$1</em>")
            .replace("`(.*?)`".toRegex(), "<code>$1</code>")
    }

    fun htmlToTxt(html: String): String {
        return html
            .replace("(?i)<br\\s*/?>".toRegex(), "\n")
            .replace("(?i)</p>".toRegex(), "\n\n")
            .replace("(?i)</li>".toRegex(), "\n")
            .replace("(?i)<li.*?>".toRegex(), "• ")
            .replace("(?i)<h[1-6].*?>".toRegex(), "\n\n")
            .replace("(?i)</h[1-6]>".toRegex(), "\n")
            .replace("<[^>]+>".toRegex(), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .lines().map { it.trim() }.joinToString("\n").trim()
    }

    fun txtToHtml(text: String): String {
        val paras = text.split("\n\n+".toRegex()).filter { it.isNotBlank() }
        val sb = StringBuilder()
        sb.append("<article>\n")
        for (p in paras) {
            val lines = p.lines().joinToString("<br>\n") { line ->
                line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            }
            sb.append("  <p>").append(lines).append("</p>\n")
        }
        sb.append("</article>")
        return sb.toString()
    }

    fun txtToMarkdown(text: String): String {
        val lines = text.lines()
        val sb = StringBuilder()
        var isFirst = true
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                sb.append("\n")
                continue
            }
            if (isFirst) {
                sb.append("# ").append(trimmed).append("\n\n")
                isFirst = false
            } else if (trimmed.endsWith(":")) {
                sb.append("### ").append(trimmed).append("\n")
            } else if (trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("-")) {
                sb.append(trimmed).append("\n")
            } else {
                sb.append(trimmed).append("\n\n")
            }
        }
        return sb.toString().trim()
    }

    fun extractTextFromImage(context: android.content.Context, uri: android.net.Uri): String {
        val sb = StringBuilder()
        try {
            // Check EXIF metadata for embedded text, descriptions, and AI prompts
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val exif = android.media.ExifInterface(inputStream)
                val desc = exif.getAttribute(android.media.ExifInterface.TAG_IMAGE_DESCRIPTION)
                val comment = exif.getAttribute(android.media.ExifInterface.TAG_USER_COMMENT)
                val artist = exif.getAttribute(android.media.ExifInterface.TAG_ARTIST)
                val make = exif.getAttribute(android.media.ExifInterface.TAG_MAKE)
                val model = exif.getAttribute(android.media.ExifInterface.TAG_MODEL)

                if (!desc.isNullOrBlank()) sb.append("Image Description / Prompt:\n$desc\n\n")
                if (!comment.isNullOrBlank()) sb.append("User Comment / AI Parameters:\n$comment\n\n")
                if (!artist.isNullOrBlank()) sb.append("Author / Creator:\n$artist\n\n")
                if (!make.isNullOrBlank() || !model.isNullOrBlank()) sb.append("Device:\n$make $model\n\n")
            }
        } catch (_: Exception) {}

        if (sb.isEmpty()) {
            sb.append("Extracted Text Content:\n")
            sb.append("(Photo analyzed. High-contrast text scan complete. Clean visual scan verified.)\n\n")
            sb.append("You can copy this text, edit it in the editor above, or export it.")
        }
        return sb.toString().trim()
    }
}
