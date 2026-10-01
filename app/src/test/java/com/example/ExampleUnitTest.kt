package com.example

import com.example.core.model.ToolCategory
import com.example.core.registry.ToolRegistry
import com.example.engine.calculator.CalculatorEngine
import com.example.engine.converter.ConversionUnit
import com.example.engine.converter.UnitCategory
import com.example.engine.converter.UnitConverterEngine
import com.example.engine.developer.DeveloperEngine
import com.example.engine.text.TextCaseOption
import com.example.engine.text.TextEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testToolRegistryLookupAndSearch() {
        val all = ToolRegistry.allTools
        assertTrue(all.isNotEmpty())

        val videoToAudio = ToolRegistry.getToolById("video_to_audio")
        assertNotNull(videoToAudio)
        assertEquals("Video to Audio", videoToAudio?.name)

        // Search by keyword "mp3"
        val mp3Results = ToolRegistry.searchTools("mp3")
        assertTrue(mp3Results.any { it.id == "video_to_audio" })

        // Search by category filter
        val imageTools = ToolRegistry.searchTools("", ToolCategory.IMAGE)
        assertTrue(imageTools.all { it.category == ToolCategory.IMAGE })
        assertTrue(imageTools.any { it.id == "image_compressor" })
    }

    @Test
    fun testHashGeneration() {
        val testInput = "Hello World"
        val hashes = DeveloperEngine.generateHashes(testInput)
        assertEquals("b10a8db164e0754105b7a99be72e3fe5", hashes.md5)
        assertEquals(40, hashes.sha1.length)
        assertEquals(64, hashes.sha256.length)
        assertEquals(128, hashes.sha512.length)
    }

    @Test
    fun testCalculatorExpression() {
        val result = CalculatorEngine.calculateExpression("3 + 5 * 2")
        assertEquals(13.0, result, 0.001)

        val parenResult = CalculatorEngine.calculateExpression("(3 + 5) * 2")
        assertEquals(16.0, parenResult, 0.001)

        val sqrtResult = CalculatorEngine.calculateExpression("sqrt(16) + 2^3")
        assertEquals(12.0, sqrtResult, 0.001)
    }

    @Test
    fun testTextEngine() {
        val input = "Hello world! This is a test."
        val stats = TextEngine.analyze(input)
        assertEquals(6, stats.wordCount)

        val uppercase = TextEngine.convertCase("hello world", TextCaseOption.UPPERCASE)
        assertEquals("HELLO WORLD", uppercase)

        val titleCase = TextEngine.convertCase("hello world", TextCaseOption.TITLE_CASE)
        assertEquals("Hello World", titleCase)

        val dedupe = TextEngine.removeDuplicateLines("apple\nbanana\napple\norange")
        assertEquals("apple\nbanana\norange", dedupe)
    }

    @Test
    fun testUnitConverter() {
        val meters = ConversionUnit("Meters", "m", 1.0)
        val km = ConversionUnit("Kilometers", "km", 1000.0)
        val converted = UnitConverterEngine.convert(2500.0, meters, km, UnitCategory.LENGTH)
        assertEquals(2.5, converted, 0.001)

        // Celsius to Fahrenheit
        val c = ConversionUnit("Celsius", "°C", 1.0)
        val f = ConversionUnit("Fahrenheit", "°F", 1.0)
        val fResult = UnitConverterEngine.convert(100.0, c, f, UnitCategory.TEMPERATURE)
        assertEquals(212.0, fResult, 0.001)
    }

    @Test
    fun testToolInterfaceContractAndExecution() = kotlinx.coroutines.test.runTest {
        // Sample custom modular plugin tool implementing Tool interface
        val customTool = object : com.example.core.model.Tool {
            override val id = "custom_test_tool"
            override val name = "Custom Test Tool"
            override val category = ToolCategory.DEVELOPER
            override val description = "A test tool plugin"
            override val icon = ToolRegistry.allTools.first().icon
            override val keywords = listOf("test", "plugin")

            override suspend fun execute(
                context: android.content.Context,
                input: com.example.core.model.ToolInput,
                onProgress: ((Float, String) -> Unit)?
            ): com.example.core.model.ToolResult {
                val text = (input as? com.example.core.model.ToolInput.Text)?.value ?: ""
                return com.example.core.model.ToolResult.Text("Processed: $text")
            }
        }

        assertEquals("custom_test_tool", customTool.id)
        assertEquals("Custom Test Tool", customTool.name)
        assertEquals(ToolCategory.DEVELOPER, customTool.category)
        assertTrue(customTool.isOffline)

        val dummyContext = object : android.content.ContextWrapper(null) {}
        val result = customTool.execute(dummyContext, com.example.core.model.ToolInput.Text("Hello Plugin"))
        assertTrue(result is com.example.core.model.ToolResult.Text)
        assertEquals("Processed: Hello Plugin", (result as com.example.core.model.ToolResult.Text).content)
    }

    @Test
    fun testPercentageAndDiscount() {
        val pct = CalculatorEngine.calculatePercentage(20.0, 150.0, 0)
        assertEquals(30.0, pct, 0.001)

        val (discount, tax, total) = CalculatorEngine.calculateDiscount(100.0, 20.0, 10.0)
        assertEquals(20.0, discount, 0.001)
        assertEquals(8.0, tax, 0.001)
        assertEquals(88.0, total, 0.001)
    }
}
