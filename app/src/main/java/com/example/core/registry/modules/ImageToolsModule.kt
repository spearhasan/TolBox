package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object ImageToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "image_compressor",
            name = "Image Compressor",
            category = ToolCategory.IMAGE,
            description = "Reduce image file size with custom quality slider and size savings preview.",
            icon = Icons.Default.Compress,
            keywords = listOf("compress", "shrink", "optimize", "reduce size", "photo", "jpg", "png"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_resizer",
            name = "Image Resizer",
            category = ToolCategory.IMAGE,
            description = "Resize image resolution to specific dimensions or scale percentages.",
            icon = Icons.Default.AspectRatio,
            keywords = listOf("resize", "scale", "dimensions", "width", "height", "pixels"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_cropper",
            name = "Image Cropper",
            category = ToolCategory.IMAGE,
            description = "Crop images into standard aspect ratios (1:1, 4:3, 16:9, Free).",
            icon = Icons.Default.Crop,
            keywords = listOf("crop", "cut", "aspect ratio", "square", "profile"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_rotator",
            name = "Image Rotator",
            category = ToolCategory.IMAGE,
            description = "Rotate photo clockwise 90°, 180°, or 270° instantly.",
            icon = Icons.Default.RotateRight,
            keywords = listOf("rotate", "turn", "orientation", "vertical"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_flipper",
            name = "Image Flipper",
            category = ToolCategory.IMAGE,
            description = "Flip image horizontally (mirror effect) or vertically.",
            icon = Icons.Default.Flip,
            keywords = listOf("flip", "mirror", "horizontal", "vertical"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_converter",
            name = "Image Converter",
            category = ToolCategory.IMAGE,
            description = "Convert images between JPEG, PNG, and modern WebP formats.",
            icon = Icons.Default.Transform,
            keywords = listOf("convert", "format", "jpg", "jpeg", "png", "webp"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_jpg_to_png",
            name = "JPG → PNG",
            category = ToolCategory.IMAGE,
            description = "Convert lossy JPEG to lossless PNG format with transparency support.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("jpg", "png", "lossless", "transparent"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_png_to_jpg",
            name = "PNG → JPG",
            category = ToolCategory.IMAGE,
            description = "Convert PNG images into lightweight JPEG photos.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("png", "jpg", "jpeg", "smaller"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_jpg_to_webp",
            name = "JPG → WebP",
            category = ToolCategory.IMAGE,
            description = "Convert JPG to next-gen WebP for 30%+ smaller file sizes.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("jpg", "webp", "next-gen", "compress"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_png_to_webp",
            name = "PNG → WebP",
            category = ToolCategory.IMAGE,
            description = "Convert PNG to WebP while preserving alpha channel transparency.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("png", "webp", "alpha", "transparency"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_webp_to_jpg",
            name = "WebP → JPG",
            category = ToolCategory.IMAGE,
            description = "Convert downloaded WebP graphics into universal JPG photos.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("webp", "jpg", "legacy", "universal"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_webp_to_png",
            name = "WebP → PNG",
            category = ToolCategory.IMAGE,
            description = "Convert WebP graphics into editable PNG images.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("webp", "png", "edit", "uncompressed"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_bmp_to_png",
            name = "BMP → PNG",
            category = ToolCategory.IMAGE,
            description = "Convert legacy Bitmap BMP images into clean compressed PNGs.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("bmp", "bitmap", "png", "convert"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_tiff_to_jpg",
            name = "TIFF → JPG",
            category = ToolCategory.IMAGE,
            description = "Convert massive TIFF scanner graphics into viewable JPG images.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("tiff", "tif", "jpg", "scanner"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_gif_to_png",
            name = "GIF → PNG",
            category = ToolCategory.IMAGE,
            description = "Extract crisp still frames from GIF animations as PNGs.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("gif", "png", "frame", "extract"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_to_base64",
            name = "Image → Base64",
            category = ToolCategory.IMAGE,
            description = "Encode any image file into data URI Base64 string for HTML/CSS.",
            icon = Icons.Default.Code,
            keywords = listOf("base64", "encode", "data uri", "html", "css"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "base64_to_image",
            name = "Base64 → Image",
            category = ToolCategory.IMAGE,
            description = "Decode Base64 string back into viewable and downloadable image.",
            icon = Icons.Default.Image,
            keywords = listOf("base64", "decode", "string to image"),
            inputType = ToolInputType.TEXT_INPUT
        ),
        ToolDefinition(
            id = "image_to_pdf",
            name = "Image → PDF",
            category = ToolCategory.IMAGE,
            description = "Convert a photograph or document scan into a clean PDF page.",
            icon = Icons.Default.PictureAsPdf,
            keywords = listOf("pdf", "convert", "document", "scan"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "multiple_images_to_pdf",
            name = "Multiple Images → PDF",
            category = ToolCategory.IMAGE,
            description = "Merge multiple photos or scans into a multi-page PDF document.",
            icon = Icons.Default.Collections,
            keywords = listOf("combine", "merge", "pdf", "photos", "multipage"),
            inputType = ToolInputType.IMAGE_MULTIPLE
        ),
        ToolDefinition(
            id = "image_to_zip",
            name = "Image → ZIP",
            category = ToolCategory.IMAGE,
            description = "Pack selected images into a single compressed ZIP archive.",
            icon = Icons.Default.FolderZip,
            keywords = listOf("zip", "bundle", "photos", "archive"),
            inputType = ToolInputType.IMAGE_MULTIPLE
        ),
        ToolDefinition(
            id = "image_metadata_viewer",
            name = "Image Metadata Viewer",
            category = ToolCategory.IMAGE,
            description = "View file resolution, color space, bit depth, and compression details.",
            icon = Icons.Default.Info,
            keywords = listOf("metadata", "info", "resolution", "details"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_metadata_cleaner",
            name = "Photo Metadata Remover",
            category = ToolCategory.IMAGE,
            description = "Strip all EXIF, GPS location, camera info, author tags, and AI metadata from photos.",
            icon = Icons.Default.CleaningServices,
            keywords = listOf("photo metadata remover", "clean", "privacy", "strip", "sanitize", "clear ai tags", "exif"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_exif_viewer",
            name = "EXIF Viewer",
            category = ToolCategory.IMAGE,
            description = "View camera model, ISO, aperture, shutter speed, and GPS location.",
            icon = Icons.Default.CameraAlt,
            keywords = listOf("exif", "camera", "iso", "gps", "aperture", "lens"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_exif_remover",
            name = "EXIF Remover",
            category = ToolCategory.IMAGE,
            description = "Delete GPS location coordinates and camera serial numbers completely.",
            icon = Icons.Default.GpsOff,
            keywords = listOf("exif", "gps", "remove", "privacy", "delete gps"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_dimensions_viewer",
            name = "Image Dimensions Viewer",
            category = ToolCategory.IMAGE,
            description = "Inspect exact pixel width, height, megapixels, and aspect ratio.",
            icon = Icons.Default.Straighten,
            keywords = listOf("dimensions", "pixels", "megapixels", "aspect"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_filesize_calc",
            name = "Image File Size Calculator",
            category = ToolCategory.IMAGE,
            description = "Estimate uncompressed vs compressed file sizes across formats.",
            icon = Icons.Default.Calculate,
            keywords = listOf("file size", "megabytes", "storage", "bytes"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_color_picker",
            name = "Image Color Picker",
            category = ToolCategory.IMAGE,
            description = "Inspect and extract exact HEX and RGB color values directly from an image.",
            icon = Icons.Default.Colorize,
            keywords = listOf("color", "picker", "eyedropper", "hex", "rgb", "palette"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_palette_generator",
            name = "Image Palette Generator",
            category = ToolCategory.IMAGE,
            description = "Generate a harmonious 6-color aesthetic palette from any photograph.",
            icon = Icons.Default.Palette,
            keywords = listOf("palette", "colors", "swatches", "theme"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_dominant_color",
            name = "Dominant Color Detector",
            category = ToolCategory.IMAGE,
            description = "Compute the primary dominant background and accent colors.",
            icon = Icons.Default.Lens,
            keywords = listOf("dominant", "primary color", "background color"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_hex_extractor",
            name = "HEX Color Extractor",
            category = ToolCategory.IMAGE,
            description = "Extract a list of top 10 unique HEX color codes found in image.",
            icon = Icons.Default.FormatColorFill,
            keywords = listOf("hex", "extract", "codes", "css", "web colors"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_blur",
            name = "Image Blur",
            category = ToolCategory.IMAGE,
            description = "Apply adjustable Gaussian blur to create soft artistic backgrounds.",
            icon = Icons.Default.BlurOn,
            keywords = listOf("blur", "gaussian", "soften", "filter"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_pixelate",
            name = "Image Pixelate",
            category = ToolCategory.IMAGE,
            description = "Censor faces, license plates, or create retro 8-bit pixel art.",
            icon = Icons.Default.Apps,
            keywords = listOf("pixelate", "mosaic", "censor", "hide", "retro"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_grayscale",
            name = "Image Grayscale",
            category = ToolCategory.IMAGE,
            description = "Convert color photos into high-contrast classic black & white.",
            icon = Icons.Default.FilterBAndW,
            keywords = listOf("grayscale", "black and white", "monochrome", "b&w"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_invert",
            name = "Image Invert",
            category = ToolCategory.IMAGE,
            description = "Invert color channels to produce photographic film negative.",
            icon = Icons.Default.InvertColors,
            keywords = listOf("invert", "negative", "reverse colors"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_sharpen",
            name = "Image Sharpen",
            category = ToolCategory.IMAGE,
            description = "Enhance edge contrast to sharpen slightly blurry photos.",
            icon = Icons.Default.Details,
            keywords = listOf("sharpen", "clarity", "edges", "enhance"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_brightness_adjuster",
            name = "Image Brightness Adjuster",
            category = ToolCategory.IMAGE,
            description = "Brighten dark underexposed photos or tone down overexposure.",
            icon = Icons.Default.Brightness6,
            keywords = listOf("brightness", "exposure", "lighten", "darken"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_contrast_adjuster",
            name = "Image Contrast Adjuster",
            category = ToolCategory.IMAGE,
            description = "Boost punchy contrast between bright highlights and deep shadows.",
            icon = Icons.Default.Contrast,
            keywords = listOf("contrast", "punch", "dynamic range"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_saturation_adjuster",
            name = "Image Saturation Adjuster",
            category = ToolCategory.IMAGE,
            description = "Vibrate dull colors or desaturate toward muted pastel tones.",
            icon = Icons.Default.ColorLens,
            keywords = listOf("saturation", "vibrance", "colors", "vivid"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_border_generator",
            name = "Image Border Generator",
            category = ToolCategory.IMAGE,
            description = "Add customizable colored borders and photo frame margins.",
            icon = Icons.Default.CropSquare,
            keywords = listOf("border", "frame", "margin", "polaroid"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_rounded_corners",
            name = "Image Rounded Corner Generator",
            category = ToolCategory.IMAGE,
            description = "Round image corners with customizable radius and transparent background.",
            icon = Icons.Default.RoundedCorner,
            keywords = listOf("rounded", "radius", "corners", "avatar"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_watermark",
            name = "Image Watermark",
            category = ToolCategory.IMAGE,
            description = "Protect your photos with logo or text watermark overlay with opacity.",
            icon = Icons.Default.BrandingWatermark,
            keywords = listOf("watermark", "protect", "copyright", "stamp"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_text_overlay",
            name = "Image Text Overlay",
            category = ToolCategory.IMAGE,
            description = "Overlay custom text, captions, or memes directly onto images.",
            icon = Icons.Default.Title,
            keywords = listOf("text overlay", "caption", "meme", "write"),
            inputType = ToolInputType.IMAGE_SINGLE
        ),
        ToolDefinition(
            id = "image_contact_sheet",
            name = "Contact Sheet Generator",
            category = ToolCategory.IMAGE,
            description = "Arrange a catalog collection of images into a printable overview grid.",
            icon = Icons.Default.GridOn,
            keywords = listOf("contact sheet", "grid", "catalog", "thumbnails"),
            inputType = ToolInputType.IMAGE_MULTIPLE
        ),
        ToolDefinition(
            id = "image_collage_maker",
            name = "Image Collage Maker",
            category = ToolCategory.IMAGE,
            description = "Combine 2 to 9 photos into stylish photo collage layouts.",
            icon = Icons.Default.Dashboard,
            keywords = listOf("collage", "combine", "photo grid", "montage"),
            inputType = ToolInputType.IMAGE_MULTIPLE
        ),
        ToolDefinition(
            id = "image_duplicate_detector",
            name = "Image Duplicate Detector",
            category = ToolCategory.IMAGE,
            description = "Compare perceptual hashes to find identical or similar duplicate photos.",
            icon = Icons.Default.Difference,
            keywords = listOf("duplicate", "similar", "phash", "compare", "clean photos"),
            inputType = ToolInputType.IMAGE_MULTIPLE
        )
    )
}
