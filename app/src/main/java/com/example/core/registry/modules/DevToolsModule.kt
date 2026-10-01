package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object DevToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "dev_json_formatter",
            name = "JSON Formatter",
            category = ToolCategory.DEVELOPER,
            description = "Beautify and indent ugly unformatted JSON strings.",
            icon = Icons.Default.DataObject,
            keywords = listOf("json formatter", "prettify json", "indent json"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_json_validator",
            name = "JSON Validator",
            category = ToolCategory.DEVELOPER,
            description = "Validate JSON syntax with detailed syntax error and line indicators.",
            icon = Icons.Default.FactCheck,
            keywords = listOf("json validator", "check json", "lint json"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_json_minifier",
            name = "JSON Minifier",
            category = ToolCategory.DEVELOPER,
            description = "Compress JSON by stripping all indentation and whitespace.",
            icon = Icons.Default.Compress,
            keywords = listOf("json minifier", "compress json", "strip whitespace"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_xml_formatter",
            name = "XML Formatter",
            category = ToolCategory.DEVELOPER,
            description = "Format and neatly indent raw XML and SVG markup.",
            icon = Icons.Default.Code,
            keywords = listOf("xml formatter", "beautify xml", "indent xml"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_xml_validator",
            name = "XML Validator",
            category = ToolCategory.DEVELOPER,
            description = "Verify well-formed XML syntax and tag closure matching.",
            icon = Icons.Default.CheckCircle,
            keywords = listOf("xml validator", "check xml", "well formed"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_yaml_formatter",
            name = "YAML Formatter",
            category = ToolCategory.DEVELOPER,
            description = "Clean up YAML hierarchy, indentation, and key mappings.",
            icon = Icons.Default.ListAlt,
            keywords = listOf("yaml formatter", "beautify yaml", "clean yaml"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_html_formatter",
            name = "HTML Formatter",
            category = ToolCategory.DEVELOPER,
            description = "Prettify unorganized HTML code blocks with standard indentation.",
            icon = Icons.Default.Html,
            keywords = listOf("html formatter", "beautify html", "clean html"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_css_formatter",
            name = "CSS Formatter",
            category = ToolCategory.DEVELOPER,
            description = "Format CSS stylesheets and rule declarations cleanly.",
            icon = Icons.Default.Style,
            keywords = listOf("css formatter", "beautify css", "clean css"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_sql_formatter",
            name = "SQL Formatter",
            category = ToolCategory.DEVELOPER,
            description = "Format complex SQL queries with capitalized keywords and line breaks.",
            icon = Icons.Default.Storage,
            keywords = listOf("sql formatter", "beautify sql", "database query"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "base64_tool",
            name = "Base64 Encoder/Decoder",
            category = ToolCategory.DEVELOPER,
            description = "Encode plain text to Base64 or decode Base64 back to text.",
            icon = Icons.Default.Code,
            keywords = listOf("base64", "encode", "decode", "binary"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "url_tool",
            name = "URL Encoder/Decoder",
            category = ToolCategory.DEVELOPER,
            description = "Encode or decode query parameters and URI components.",
            icon = Icons.Default.Link,
            keywords = listOf("url encode", "url decode", "percent encoding"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_hex_tool",
            name = "Hex Encoder/Decoder",
            category = ToolCategory.DEVELOPER,
            description = "Convert UTF-8 text to Hexadecimal byte representation and back.",
            icon = Icons.Default.Memory,
            keywords = listOf("hex", "hexadecimal", "bytes", "encode hex"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "hash_generator",
            name = "Hash Generator",
            category = ToolCategory.DEVELOPER,
            description = "Generate MD5, SHA-1, SHA-256, and SHA-512 cryptographic hashes.",
            icon = Icons.Default.Security,
            keywords = listOf("hash", "md5", "sha256", "sha1", "sha512", "checksum"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "uuid_generator",
            name = "UUID Generator",
            category = ToolCategory.DEVELOPER,
            description = "Generate RFC 4122 v4 UUID identifiers individually or in batches.",
            icon = Icons.Default.Fingerprint,
            keywords = listOf("uuid", "guid", "v4", "unique identifier"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "jwt_decoder",
            name = "JWT Token Decoder",
            category = ToolCategory.DEVELOPER,
            description = "Decode JWT header, payload claims, and expiration date offline.",
            icon = Icons.Default.Token,
            keywords = listOf("jwt", "token", "json web token", "claims", "auth"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "regex_tester",
            name = "Regex Tester",
            category = ToolCategory.DEVELOPER,
            description = "Test regular expression patterns live against test strings.",
            icon = Icons.Default.Search,
            keywords = listOf("regex", "regular expression", "pattern matcher", "match"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_markdown_preview",
            name = "Markdown Previewer",
            category = ToolCategory.DEVELOPER,
            description = "Live dual-pane editor and renderer for Markdown documentation.",
            icon = Icons.Default.Preview,
            keywords = listOf("markdown preview", "render md", "docs"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "timestamp_converter",
            name = "Timestamp Converter",
            category = ToolCategory.DEVELOPER,
            description = "Convert Unix epoch seconds and milliseconds to human datetime & ISO 8601.",
            icon = Icons.Default.Schedule,
            keywords = listOf("timestamp", "epoch", "unix time", "iso8601"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "color_converter",
            name = "Color Code Converter",
            category = ToolCategory.DEVELOPER,
            description = "Convert and preview color codes between HEX, RGB, HSL, and Android Color Int.",
            icon = Icons.Default.Palette,
            keywords = listOf("color converter", "hex", "rgb", "hsl", "css colors"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_ip_lookup",
            name = "IP Address Info",
            category = ToolCategory.DEVELOPER,
            description = "Inspect IP version (IPv4/IPv6), subnet mask, CIDR, and network classes.",
            icon = Icons.Default.Router,
            keywords = listOf("ip lookup", "ipv4", "ipv6", "cidr", "subnet"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_user_agent",
            name = "User-Agent Parser",
            category = ToolCategory.DEVELOPER,
            description = "Parse browser User-Agent strings into browser, engine, OS, and device.",
            icon = Icons.Default.Devices,
            keywords = listOf("user agent", "ua parser", "browser info", "os detect"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_cron_parser",
            name = "Cron Parser",
            category = ToolCategory.DEVELOPER,
            description = "Translate standard 5-part cron expressions into human readable schedule.",
            icon = Icons.Default.Update,
            keywords = listOf("cron", "cron expression", "schedule", "cron syntax"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "dev_http_status",
            name = "HTTP Status Code Reference",
            category = ToolCategory.DEVELOPER,
            description = "Complete reference guide to 1xx, 2xx, 3xx, 4xx, and 5xx HTTP response codes.",
            icon = Icons.Default.Http,
            keywords = listOf("http codes", "404", "500", "status code", "rest api"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "dev_chmod_calc",
            name = "chmod Calculator",
            category = ToolCategory.DEVELOPER,
            description = "Calculate Linux file permissions (e.g. 755, 644, rwxr-xr-x) with toggles.",
            icon = Icons.Default.VpnKey,
            keywords = listOf("chmod", "permissions", "linux", "755", "644"),
            inputType = ToolInputType.NONE
        )
    )
}
