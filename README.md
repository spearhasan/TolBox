# 🧰 ToolBox — All-in-One Utility Toolbox for Android

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-M3-green.svg)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/Privacy-100%25_Offline-success.svg)](PRIVACY.md)

**ToolBox** is a lightweight, modern, open-source, privacy-first Android application designed as a universal utility platform. Instead of installing dozens of separate single-purpose apps or uploading private files to random converter websites, ToolBox provides dozens of high-performance tools running **100% locally on your device**.

---

## 🎯 Architecture Principle: `Core App ≠ Individual Tools`

The app is built around a pluggable **Tool Registry System**:
- The core application provides search indexing, favorite persistence, execution history, theme switching, storage management, and standardized UI scaffolding.
- Each tool is registered as an independent module. Adding a new tool is as simple as creating an engine function and registering a `ToolDefinition` with keywords and category metadata.

```
app/
├── core/
│   ├── model/         # ToolDefinition, Category, ExecutionState
│   ├── database/      # Room DB for Favorites and History
│   ├── registry/      # Centralized Tool Registry & Search Index
│   └── util/          # FileUtils, Safe Zip validation, QR Engine
├── engine/
│   ├── image/         # Compress, Resize, Format conversion, Filter, Blur
│   ├── media/         # Video-to-audio extraction, Trimmer, Audio cutter
│   ├── pdf/           # Images to PDF, Text to PDF, PDF to Bitmap
│   ├── text/          # Word count, Line deduplication, Encoders, Formatters
│   ├── developer/     # Hashes (MD5, SHA), UUID, Unix Timestamp, Regex, JWT
│   ├── calculator/    # Math, Percentage, Age, Date Diff, BMI
│   ├── converter/     # Unit converter (Length, Mass, Temp, Storage, etc.)
│   └── archive/       # Zip creation and extraction with Zip-Slip defense
├── ui/
│   ├── theme/         # Material Design 3 Theming
│   ├── components/    # CommonToolScreen, ToolCard, CategoryChip
│   ├── screens/       # Home, Favorites, History, Settings, Tool views
│   └── navigation/    # Jetpack Compose Navigation
```

---

## 🛠️ Included Tools

### 🎬 Video & Audio Tools
- **Video to Audio**: Extract audio tracks (.m4a/.aac) directly from video files without re-encoding.
- **Audio Cutter / Trimmer**: Trim audio files with millisecond precision.
- **Volume Booster**: Adjust and normalize audio levels.
- **Silence Detector**: Analyze waveform energy for silence segments.

### 🖼️ Image Tools
- **Image Compressor**: Compress JPEG/PNG/WebP with adjustable quality and preview size savings.
- **Image Resizer**: Scale dimensions with aspect ratio lock.
- **Format Converter**: Convert between PNG, JPG, and WebP (Lossy & Lossless).
- **Multiple Images → PDF**: Compile photo collections into a clean multipage PDF.
- **Image Color Picker**: Sample exact HEX and RGB color values with touch.
- **Image Grayscale & Blur**: Apply visual filters instantly.

### 📄 PDF & Document Tools
- **Images to PDF**: Create PDF documents from picked images with custom margins.
- **Text to PDF**: Render text documents directly into standard printable PDF files.
- **PDF to Images**: Render pages from PDF files into shareable bitmaps.
- **PDF Inspector**: Inspect page count, dimensions, and file size.

### 📝 Text Tools
- **Word & Character Counter**: Real-time stats on words, characters, sentences, paragraphs, and reading time.
- **Case Converter**: Uppercase, Lowercase, Title Case, Sentence Case, Invert Case.
- **Line Sorter & Deduplicator**: Clean messy data lists, remove duplicate lines, alphabetize.
- **Text Cleaner**: Strip empty lines, tabs, and duplicate whitespace.
- **Base64 & URL Encoder / Decoder**: Standard ASCII/UTF-8 compliant encoding and decoding.

### 🔐 Developer & Cryptography Tools
- **Hash Generator**: MD5, SHA-1, SHA-256, SHA-512 cryptographic digests.
- **UUID Generator**: RFC 4122 v4 UUID generator with batch count and case formatting.
- **Unix Timestamp Converter**: Epoch seconds/milliseconds to human-readable ISO format and vice versa.
- **Regex Tester**: Live regular expression matching with flag toggles (Multiline, Case-insensitive, DotAll).
- **JWT Decoder**: Decode JWT header and claims without verifying online.
- **Color Converter**: Seamless HEX, RGB, and HSL conversions with live preview.
- **JSON Formatter & Minifier**: Beautify or minify JSON strings with instant syntax validation.

### 🧮 Calculator & Everyday Tools
- **Expression Calculator**: Math parser supporting parentheses and scientific functions.
- **Percentage Calculator**: Quick computation of percentages, discounts, and percentage change.
- **Age & Milestone Calculator**: Exact age in years, months, days, plus upcoming birthday countdown.
- **Date Difference**: Calculate elapsed days, weeks, and months between two dates.
- **BMI Calculator**: Calculate Body Mass Index with standard WHO health categories.

### ⚖️ Universal Unit Converter
- **Length, Weight, Temperature, Area, Speed, Digital Storage, Energy**.

### 📦 File & Archive Tools
- **File Inspector**: MIME type, byte size, extension analysis.
- **ZIP Archive Creator**: Bundle multiple files into a compressed `.zip` archive.
- **ZIP Archive Extractor**: Safe extraction with canonical path validation (protecting against Zip-Slip).

### 📱 QR Code Generator
- Generate offline QR codes for text, URLs, Wi-Fi, and contacts with instant save and Android Share Sheet integration.

---

## 🔒 Privacy & Permissions
- **Zero broad storage permissions**: Operates entirely with modern Android Storage Access Framework (SAF) and native pickers.
- **No Internet Required**: 100% offline capability.
- **No telemetry, ads, or tracking**.

---

## 🚀 Building & Running

### Requirements
- Android SDK 36 (minSdk 24)
- JDK 17+ or JDK 21
- Gradle 9.x

### Build APK
```bash
gradle :app:assembleDebug
```

### Run Unit Tests
```bash
gradle :app:testDebugUnitTest
```

---

## 📄 License
Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
