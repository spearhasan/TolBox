package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object GifToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "gif_video_to_gif",
            name = "Video → GIF",
            category = ToolCategory.GIF,
            description = "Convert any video clip into an animated GIF with custom FPS and size.",
            icon = Icons.Default.Animation,
            keywords = listOf("video to gif", "create gif", "meme", "animation"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "gif_to_video",
            name = "GIF → Video",
            category = ToolCategory.GIF,
            description = "Convert animated GIF into MP4 video file for WhatsApp/Instagram status.",
            icon = Icons.Default.Movie,
            keywords = listOf("gif to video", "gif to mp4", "instagram", "status"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_to_png",
            name = "GIF → PNG",
            category = ToolCategory.GIF,
            description = "Deconstruct GIF into crisp individual PNG frame images.",
            icon = Icons.Default.Image,
            keywords = listOf("gif to png", "frames", "extract", "images"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_to_jpg",
            name = "GIF → JPG",
            category = ToolCategory.GIF,
            description = "Extract still cover frame from GIF animation as JPG photo.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("gif to jpg", "cover", "still photo"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_compressor",
            name = "GIF Compressor",
            category = ToolCategory.GIF,
            description = "Reduce large GIF file size by color quantization and frame dropping.",
            icon = Icons.Default.Compress,
            keywords = listOf("compress gif", "shrink", "optimize", "reduce mb"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_resizer",
            name = "GIF Resizer",
            category = ToolCategory.GIF,
            description = "Scale GIF dimensions up or down while preserving animation timing.",
            icon = Icons.Default.AspectRatio,
            keywords = listOf("resize gif", "scale", "width", "height"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_cropper",
            name = "GIF Cropper",
            category = ToolCategory.GIF,
            description = "Crop rectangular region from animated GIF frames.",
            icon = Icons.Default.Crop,
            keywords = listOf("crop gif", "cut area", "square gif"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_speed_changer",
            name = "GIF Speed Changer",
            category = ToolCategory.GIF,
            description = "Speed up or slow down GIF animation playback frame rate.",
            icon = Icons.Default.Speed,
            keywords = listOf("speed gif", "faster", "slower", "frame delay"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_frame_extractor",
            name = "GIF Frame Extractor",
            category = ToolCategory.GIF,
            description = "Extract all constituent frames as a ZIP archive of images.",
            icon = Icons.Default.BurstMode,
            keywords = listOf("frame extractor", "burst", "all frames", "zip"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_frame_remover",
            name = "GIF Frame Remover",
            category = ToolCategory.GIF,
            description = "Delete specific unwanted or stuttering frames from GIF.",
            icon = Icons.Default.Delete,
            keywords = listOf("remove frame", "delete frame", "cut frame"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_frame_reorder",
            name = "GIF Frame Reorder",
            category = ToolCategory.GIF,
            description = "Rearrange sequence order of animation frames.",
            icon = Icons.Default.Reorder,
            keywords = listOf("reorder", "sequence", "frames", "arrange"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_reverse",
            name = "GIF Reverse",
            category = ToolCategory.GIF,
            description = "Play GIF animation backwards (boomerang rewind effect).",
            icon = Icons.Default.FastRewind,
            keywords = listOf("reverse gif", "rewind", "backwards", "boomerang"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_loop_controller",
            name = "GIF Loop Controller",
            category = ToolCategory.GIF,
            description = "Set infinite looping or specific loop repetition count (1x, 2x, 5x).",
            icon = Icons.Default.Loop,
            keywords = listOf("loop", "infinite", "repeat count", "iterations"),
            inputType = ToolInputType.FILE_SINGLE
        ),
        ToolDefinition(
            id = "gif_metadata_viewer",
            name = "GIF Metadata Viewer",
            category = ToolCategory.GIF,
            description = "View frame count, total animation duration, palette size, and delays.",
            icon = Icons.Default.Info,
            keywords = listOf("gif metadata", "frame count", "duration", "palette"),
            inputType = ToolInputType.FILE_SINGLE
        )
    )
}
