package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object QrToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "qr_generator",
            name = "QR Generator",
            category = ToolCategory.QR,
            description = "Generate clean vector or bitmap QR codes with instant save and share.",
            icon = Icons.Default.QrCode,
            keywords = listOf("qr generator", "create qr", "barcode", "quick response"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_scanner",
            name = "QR Scanner",
            category = ToolCategory.QR,
            description = "Scan any QR code or barcode using the camera or device storage.",
            icon = Icons.Default.QrCodeScanner,
            keywords = listOf("qr scanner", "scan qr", "barcode scanner", "read qr"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "qr_text_to_qr",
            name = "Text → QR",
            category = ToolCategory.QR,
            description = "Encode plain text, secret notes, or keys into an offline QR code.",
            icon = Icons.Default.TextFields,
            keywords = listOf("text to qr", "secret note qr", "encode text"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_url_to_qr",
            name = "URL → QR",
            category = ToolCategory.QR,
            description = "Generate scannable website and social link QR codes with favicon option.",
            icon = Icons.Default.Link,
            keywords = listOf("url to qr", "website qr", "link to qr"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_wifi_to_qr",
            name = "Wi-Fi → QR",
            category = ToolCategory.QR,
            description = "Create Wi-Fi guest connect QR codes (WPA/WPA2/WPA3) for instant connection.",
            icon = Icons.Default.Wifi,
            keywords = listOf("wifi qr", "connect wifi", "wifi password"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "qr_contact_to_qr",
            name = "Contact → QR",
            category = ToolCategory.QR,
            description = "Generate vCard 3.0 business card QR codes with name, phone, and email.",
            icon = Icons.Default.ContactPage,
            keywords = listOf("contact qr", "vcard", "business card", "meCard"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "qr_email_to_qr",
            name = "Email → QR",
            category = ToolCategory.QR,
            description = "Generate pre-addressed mailto QR codes with subject and body.",
            icon = Icons.Default.Email,
            keywords = listOf("email qr", "mailto", "send email"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_phone_to_qr",
            name = "Phone → QR",
            category = ToolCategory.QR,
            description = "Generate direct telephone dialing tel: QR codes.",
            icon = Icons.Default.Phone,
            keywords = listOf("phone qr", "dialer", "tel link"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_sms_to_qr",
            name = "SMS → QR",
            category = ToolCategory.QR,
            description = "Generate text message SMSTO: QR codes with pre-filled message.",
            icon = Icons.Default.Sms,
            keywords = listOf("sms qr", "message qr", "text message"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_location_to_qr",
            name = "Location → QR",
            category = ToolCategory.QR,
            description = "Generate GPS geo: latitude/longitude coordinates map QR codes.",
            icon = Icons.Default.LocationOn,
            keywords = listOf("location qr", "gps qr", "map qr", "coordinates"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_crypto_to_qr",
            name = "Crypto → QR",
            category = ToolCategory.QR,
            description = "Generate Bitcoin, Ethereum, and USDT crypto payment QR codes.",
            icon = Icons.Default.CurrencyBitcoin,
            keywords = listOf("crypto qr", "bitcoin", "ethereum", "wallet qr"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_barcode_generator",
            name = "Barcode Generator",
            category = ToolCategory.QR,
            description = "Generate 1D barcodes: Code 128, EAN-13, UPC-A, and Code 39.",
            icon = Icons.Default.ViewWeek,
            keywords = listOf("barcode generator", "code128", "ean13", "upc"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_barcode_scanner",
            name = "Barcode Scanner",
            category = ToolCategory.QR,
            description = "Scan retail product barcodes and ISBN book numbers.",
            icon = Icons.Default.CenterFocusStrong,
            keywords = listOf("barcode scanner", "isbn", "product scan"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "qr_styling",
            name = "QR Styling",
            category = ToolCategory.QR,
            description = "Customize QR code colors, corner radius, dots, and center logo.",
            icon = Icons.Default.Palette,
            keywords = listOf("custom qr", "styled qr", "colors", "rounded qr"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_batch_generator",
            name = "Batch QR Generator",
            category = ToolCategory.QR,
            description = "Generate dozens of QR codes from a list or CSV and export as ZIP.",
            icon = Icons.Default.FolderZip,
            keywords = listOf("batch qr", "bulk qr", "csv to qr"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "qr_from_image",
            name = "QR Reader from Image",
            category = ToolCategory.QR,
            description = "Decode QR codes directly from gallery photos or saved screenshots.",
            icon = Icons.Default.PhotoLibrary,
            keywords = listOf("qr from image", "decode picture", "scan photo"),
            inputType = ToolInputType.IMAGE_SINGLE
        )
    )
}
