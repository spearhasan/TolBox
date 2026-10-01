package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object TextToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "text_word_counter",
            name = "Word Counter",
            category = ToolCategory.TEXT,
            description = "Count total words, vocabulary density, and syllable complexity.",
            icon = Icons.Default.Numbers,
            keywords = listOf("word count", "count words", "writing stats"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_char_counter",
            name = "Character Counter",
            category = ToolCategory.TEXT,
            description = "Count characters with and without spaces, bytes, and glyph count.",
            icon = Icons.Default.TextFields,
            keywords = listOf("char count", "letters", "characters", "twitter length"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_sentence_counter",
            name = "Sentence Counter",
            category = ToolCategory.TEXT,
            description = "Calculate sentence count and average words per sentence.",
            icon = Icons.Default.ShortText,
            keywords = listOf("sentences", "sentence count", "readability"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_paragraph_counter",
            name = "Paragraph Counter",
            category = ToolCategory.TEXT,
            description = "Count distinct paragraphs and text block structure.",
            icon = Icons.Default.FormatAlignLeft,
            keywords = listOf("paragraph", "blocks", "structure"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_line_counter",
            name = "Line Counter",
            category = ToolCategory.TEXT,
            description = "Count total lines, blank lines, and non-blank lines.",
            icon = Icons.Default.FormatListNumbered,
            keywords = listOf("lines", "line count", "row count"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_reading_time",
            name = "Reading Time Calculator",
            category = ToolCategory.TEXT,
            description = "Estimate reading time (200 wpm) and speaking speech time (130 wpm).",
            icon = Icons.Default.Timer,
            keywords = listOf("reading time", "speaking time", "wpm", "duration"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_remove_duplicate_lines",
            name = "Remove Duplicate Lines",
            category = ToolCategory.TEXT,
            description = "Instantly filter out duplicate repeating lines while preserving order.",
            icon = Icons.Default.FilterList,
            keywords = listOf("deduplicate", "duplicate lines", "unique lines", "clean"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_remove_empty_lines",
            name = "Remove Empty Lines",
            category = ToolCategory.TEXT,
            description = "Delete blank and whitespace-only lines from text.",
            icon = Icons.Default.CleaningServices,
            keywords = listOf("empty lines", "blank lines", "strip blank"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_remove_extra_spaces",
            name = "Remove Extra Spaces",
            category = ToolCategory.TEXT,
            description = "Collapse multiple consecutive spaces into a single clean space.",
            icon = Icons.Default.SpaceBar,
            keywords = listOf("extra spaces", "spaces", "trim spaces", "clean spaces"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_remove_line_breaks",
            name = "Remove Line Breaks",
            category = ToolCategory.TEXT,
            description = "Join broken lines into a continuous paragraph.",
            icon = Icons.Default.WrapText,
            keywords = listOf("remove line breaks", "join lines", "single line"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_sort_az",
            name = "Sort Lines A-Z",
            category = ToolCategory.TEXT,
            description = "Sort lines in alphabetical ascending order (A to Z).",
            icon = Icons.Default.SortByAlpha,
            keywords = listOf("sort", "alphabetical", "a to z", "ascending"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_sort_za",
            name = "Sort Lines Z-A",
            category = ToolCategory.TEXT,
            description = "Sort lines in reverse alphabetical order (Z to A).",
            icon = Icons.Default.SortByAlpha,
            keywords = listOf("sort reverse", "z to a", "descending"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_sort_length",
            name = "Sort Lines by Length",
            category = ToolCategory.TEXT,
            description = "Sort text lines from shortest to longest (or longest to shortest).",
            icon = Icons.Default.FormatLineSpacing,
            keywords = listOf("sort by length", "shortest", "longest"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_reverse_lines",
            name = "Reverse Lines",
            category = ToolCategory.TEXT,
            description = "Invert vertical line sequence from bottom to top.",
            icon = Icons.Default.SwapVert,
            keywords = listOf("reverse lines", "flip lines", "upside down"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_reverse_text",
            name = "Reverse Text",
            category = ToolCategory.TEXT,
            description = "Reverse entire text character-by-character for mirror writing.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("reverse text", "backwards text", "mirror text"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_uppercase",
            name = "Uppercase",
            category = ToolCategory.TEXT,
            description = "Convert all characters into CAPITAL LETTERS.",
            icon = Icons.Default.FormatSize,
            keywords = listOf("uppercase", "caps", "capital letters"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_lowercase",
            name = "Lowercase",
            category = ToolCategory.TEXT,
            description = "Convert all characters into small lowercase letters.",
            icon = Icons.Default.FormatSize,
            keywords = listOf("lowercase", "small letters"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_title_case",
            name = "Title Case",
            category = ToolCategory.TEXT,
            description = "Capitalize First Letter Of Every Major Word for Book Titles.",
            icon = Icons.Default.Title,
            keywords = listOf("title case", "capitalize words", "headline"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_sentence_case",
            name = "Sentence Case",
            category = ToolCategory.TEXT,
            description = "Capitalize the initial character of each sentence properly.",
            icon = Icons.Default.FormatColorText,
            keywords = listOf("sentence case", "grammar", "capitalize"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_camel_case",
            name = "camelCase Converter",
            category = ToolCategory.TEXT,
            description = "Convert words into camelCase programming identifier.",
            icon = Icons.Default.Code,
            keywords = listOf("camelcase", "code naming", "programming"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_snake_case",
            name = "snake_case Converter",
            category = ToolCategory.TEXT,
            description = "Convert phrases into snake_case with lowercases and underscores.",
            icon = Icons.Default.Code,
            keywords = listOf("snake_case", "python", "underscores"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_kebab_case",
            name = "kebab-case Converter",
            category = ToolCategory.TEXT,
            description = "Convert words into dash-separated kebab-case for CSS and URLs.",
            icon = Icons.Default.Code,
            keywords = listOf("kebab-case", "dash", "hyphenated"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_pascal_case",
            name = "PascalCase Converter",
            category = ToolCategory.TEXT,
            description = "Convert words into PascalCase class name casing.",
            icon = Icons.Default.Code,
            keywords = listOf("pascalcase", "class name", "capitalized camel"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_slug_generator",
            name = "Slug Generator",
            category = ToolCategory.TEXT,
            description = "Transform article headlines into clean, URL-friendly slugs.",
            icon = Icons.Default.Link,
            keywords = listOf("slug", "url slug", "clean url", "permalink"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_cleaner",
            name = "Text Cleaner",
            category = ToolCategory.TEXT,
            description = "Sanitize smart quotes, accents, zero-width spaces, and control chars.",
            icon = Icons.Default.CleaningServices,
            keywords = listOf("text cleaner", "sanitize", "smart quotes", "zero width"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_formatter",
            name = "Text Formatter",
            category = ToolCategory.TEXT,
            description = "Indent, tabulate, and standardize paragraphs and margins.",
            icon = Icons.Default.FormatAlignJustify,
            keywords = listOf("formatter", "indent", "beautify text"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_find_replace",
            name = "Find & Replace",
            category = ToolCategory.TEXT,
            description = "Search and substitute words with match case and whole-word toggles.",
            icon = Icons.Default.FindReplace,
            keywords = listOf("find replace", "substitute", "search and replace"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_duplicate_words",
            name = "Duplicate Word Finder",
            category = ToolCategory.TEXT,
            description = "Highlight accidental consecutive duplicate words (e.g. 'the the').",
            icon = Icons.Default.Spellcheck,
            keywords = listOf("duplicate word", "repeated words", "proofread"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_repeated_chars",
            name = "Repeated Character Finder",
            category = ToolCategory.TEXT,
            description = "Identify elongated characters and typos (e.g. 'sooooo').",
            icon = Icons.Default.Check,
            keywords = listOf("repeated char", "typo", "elongated"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_diff",
            name = "Text Diff",
            category = ToolCategory.TEXT,
            description = "Compare two versions of text with added/deleted line highlights.",
            icon = Icons.Default.Difference,
            keywords = listOf("diff", "compare text", "changes", "version diff"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_compare",
            name = "Text Compare",
            category = ToolCategory.TEXT,
            description = "Side-by-side textual similarity percentage and difference inspection.",
            icon = Icons.Default.Compare,
            keywords = listOf("compare", "similarity", "match score"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "text_lorem_ipsum",
            name = "Lorem Ipsum Generator",
            category = ToolCategory.TEXT,
            description = "Generate standard placeholder dummy text by words, sentences, or paragraphs.",
            icon = Icons.Default.Notes,
            keywords = listOf("lorem ipsum", "dummy text", "placeholder", "mock text"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "text_random_text",
            name = "Random Text Generator",
            category = ToolCategory.TEXT,
            description = "Generate random strings, alpha-numeric tokens, and test content.",
            icon = Icons.Default.Shuffle,
            keywords = listOf("random text", "random string", "mock data"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "text_random_number",
            name = "Random Number Generator",
            category = ToolCategory.TEXT,
            description = "Generate cryptographically secure random integers in any range.",
            icon = Icons.Default.Casino,
            keywords = listOf("random number", "dice", "integer", "rng"),
            inputType = ToolInputType.NONE
        ),
        ToolDefinition(
            id = "text_random_password",
            name = "Random Password Generator",
            category = ToolCategory.TEXT,
            description = "Generate uncrackable strong passwords with custom symbols and length.",
            icon = Icons.Default.Password,
            keywords = listOf("password generator", "strong password", "secure", "credentials"),
            inputType = ToolInputType.NONE
        )
    )
}
