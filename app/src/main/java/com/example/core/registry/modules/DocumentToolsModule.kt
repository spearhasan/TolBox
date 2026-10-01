package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object DocumentToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "doc_txt_to_pdf",
            name = "TXT → PDF",
            category = ToolCategory.DOCUMENT,
            description = "Convert plain text files into clean paginated PDF documents.",
            icon = Icons.Default.PictureAsPdf,
            keywords = listOf("txt to pdf", "text to pdf", "convert", "printable"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "doc_txt_to_html",
            name = "TXT → HTML",
            category = ToolCategory.DOCUMENT,
            description = "Convert plain text notes into structured semantic HTML paragraphs.",
            icon = Icons.Default.Html,
            keywords = listOf("txt to html", "text to web", "html paragraph"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_txt_to_markdown",
            name = "TXT → Markdown",
            category = ToolCategory.DOCUMENT,
            description = "Format plain text lines into clean Markdown headings and lists.",
            icon = Icons.Default.Code,
            keywords = listOf("txt to md", "markdown", "format"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_markdown_to_html",
            name = "Markdown → HTML",
            category = ToolCategory.DOCUMENT,
            description = "Render Markdown syntax into clean formatted HTML code.",
            icon = Icons.Default.Html,
            keywords = listOf("markdown to html", "md to html", "render markdown"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_markdown_to_txt",
            name = "Markdown → TXT",
            category = ToolCategory.DOCUMENT,
            description = "Strip Markdown tokens and tags to produce pure plain text.",
            icon = Icons.Default.Description,
            keywords = listOf("md to txt", "strip markdown", "plain text"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_html_to_txt",
            name = "HTML → TXT",
            category = ToolCategory.DOCUMENT,
            description = "Extract readable body text from HTML markup with tags removed.",
            icon = Icons.Default.Notes,
            keywords = listOf("html to txt", "strip html", "text only"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_html_to_markdown",
            name = "HTML → Markdown",
            category = ToolCategory.DOCUMENT,
            description = "Convert webpage HTML markup back into lightweight Markdown.",
            icon = Icons.Default.Code,
            keywords = listOf("html to markdown", "convert html", "md"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_csv_to_json",
            name = "CSV → JSON",
            category = ToolCategory.DOCUMENT,
            description = "Parse tabular CSV spreadsheets into structured JSON arrays of objects.",
            icon = Icons.Default.DataObject,
            keywords = listOf("csv to json", "spreadsheet", "table", "json array"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_json_to_csv",
            name = "JSON → CSV",
            category = ToolCategory.DOCUMENT,
            description = "Flatten JSON arrays into spreadsheet-ready comma-separated CSV.",
            icon = Icons.Default.TableChart,
            keywords = listOf("json to csv", "export spreadsheet", "flatten json"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_csv_to_xml",
            name = "CSV → XML",
            category = ToolCategory.DOCUMENT,
            description = "Convert CSV rows into hierarchical XML entity nodes.",
            icon = Icons.Default.Code,
            keywords = listOf("csv to xml", "xml rows", "convert"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_xml_to_json",
            name = "XML → JSON",
            category = ToolCategory.DOCUMENT,
            description = "Convert XML documents and RSS feeds into modern JSON objects.",
            icon = Icons.Default.DataObject,
            keywords = listOf("xml to json", "convert xml", "parse xml"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_json_to_yaml",
            name = "JSON → YAML",
            category = ToolCategory.DOCUMENT,
            description = "Convert JSON config files into human-readable clean YAML format.",
            icon = Icons.Default.ListAlt,
            keywords = listOf("json to yaml", "yaml config", "docker", "k8s"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_yaml_to_json",
            name = "YAML → JSON",
            category = ToolCategory.DOCUMENT,
            description = "Parse YAML configuration files into valid JSON format.",
            icon = Icons.Default.DataObject,
            keywords = listOf("yaml to json", "parse yaml", "convert"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "doc_text_merger",
            name = "Text File Merger",
            category = ToolCategory.DOCUMENT,
            description = "Concatenate multiple text, log, or code files into one document.",
            icon = Icons.Default.CallMerge,
            keywords = listOf("merge text", "combine files", "concat logs"),
            inputType = ToolInputType.FILE_MULTIPLE
        ),
        ToolDefinition(
            id = "doc_text_splitter",
            name = "Text File Splitter",
            category = ToolCategory.DOCUMENT,
            description = "Split massive text or log files by line count or size chunk.",
            icon = Icons.Default.CallSplit,
            keywords = listOf("split text", "split log", "chunk file"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "doc_word_counter",
            name = "Document Word Counter",
            category = ToolCategory.DOCUMENT,
            description = "Count total words, unique words, and reading duration in files.",
            icon = Icons.Default.Numbers,
            keywords = listOf("document word count", "stats", "words"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "doc_char_counter",
            name = "Document Character Counter",
            category = ToolCategory.DOCUMENT,
            description = "Calculate characters with and without whitespace count in documents.",
            icon = Icons.Default.TextFields,
            keywords = listOf("character count", "letters", "symbols"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "doc_line_counter",
            name = "Document Line Counter",
            category = ToolCategory.DOCUMENT,
            description = "Count code lines, blank lines, and non-empty lines in text files.",
            icon = Icons.Default.FormatListNumbered,
            keywords = listOf("line count", "loc", "lines of code"),
            inputType = ToolInputType.FILE_SINGLE
        )
    )
}
