# Contributing to ToolBox

Thank you for your interest in contributing to ToolBox!

## Architecture Principle: Core App ≠ Individual Tools
The central architectural tenet of ToolBox is the **Modular Tool Plugin Architecture**:
1. Every tool implements the `Tool` interface or uses `ToolDefinition`:
   - Unique ID (`id: String`)
   - Name (`name: String`) and description (`description: String`)
   - Category (`category: ToolCategory` — Video, Audio, Image, PDF, Text, Developer, Calculator, Converter, File)
   - UI Vector Icon (`icon: ImageVector`)
   - Search Keywords and Synonyms (`keywords: List<String>`)
   - Input Type (`inputType: ToolInputType`)
   - Execution Method (`suspend fun execute(context: Context, input: ToolInput, onProgress: ((Float, String) -> Unit)?): ToolResult`)
2. Adding a new tool does **NOT** require modifying Home screens, Search indexes, or Favorites logic. Simply:
   - Implement the `Tool` interface or engine logic
   - Register the tool in `ToolRegistry.kt`
   - Bind the UI component in `NavGraph.kt` or `CommonToolScreen.kt`

## Guidelines
- Write modern Kotlin with Jetpack Compose (Material 3).
- Adhere to the offline-first, local-processing rule.
- Do not add heavy closed-source SDKs or ad trackers.
- Ensure all tests pass (`gradle :app:testDebugUnitTest`).
