package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object VideoToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "video_to_audio",
            name = "Video to Audio",
            category = ToolCategory.VIDEO,
            description = "Extract audio (M4A / AAC) directly from video files without loss of quality.",
            icon = Icons.Default.Audiotrack,
            keywords = listOf("video", "audio", "extract", "sound", "mp3", "m4a", "aac", "music", "rip"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_to_mp3",
            name = "Video → MP3",
            category = ToolCategory.VIDEO,
            description = "Extract and convert video audio track into standard MP3 format.",
            icon = Icons.Default.Audiotrack,
            keywords = listOf("video", "mp3", "audio", "sound", "extract", "music"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_to_wav",
            name = "Video → WAV",
            category = ToolCategory.VIDEO,
            description = "Rip uncompressed WAV audio from video files with pristine quality.",
            icon = Icons.Default.GraphicEq,
            keywords = listOf("video", "wav", "lossless", "pcm", "audio"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_to_aac",
            name = "Video → AAC",
            category = ToolCategory.VIDEO,
            description = "Extract high efficiency AAC audio stream directly from video container.",
            icon = Icons.Default.MusicNote,
            keywords = listOf("video", "aac", "audio", "apple", "m4a"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_to_m4a",
            name = "Video → M4A",
            category = ToolCategory.VIDEO,
            description = "Fast lossless demuxing of MPEG-4 audio track from video files.",
            icon = Icons.Default.Audiotrack,
            keywords = listOf("video", "m4a", "extract", "audio", "fast"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_to_flac",
            name = "Video → FLAC",
            category = ToolCategory.VIDEO,
            description = "Extract lossless studio-grade FLAC audio from concert and music videos.",
            icon = Icons.Default.GraphicEq,
            keywords = listOf("video", "flac", "lossless", "hi-fi", "audio"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_to_gif",
            name = "Video → GIF",
            category = ToolCategory.VIDEO,
            description = "Convert short video clips or segments into animated GIF files.",
            icon = Icons.Default.Animation,
            keywords = listOf("video", "gif", "animated", "meme", "short"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_to_image",
            name = "Video → Image",
            category = ToolCategory.VIDEO,
            description = "Capture high-resolution still photographs directly from video scenes.",
            icon = Icons.Default.Image,
            keywords = listOf("video", "photo", "still", "capture", "frame"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_to_frames",
            name = "Video → Frames",
            category = ToolCategory.VIDEO,
            description = "Export continuous sequential image frames from any video clip.",
            icon = Icons.Default.BurstMode,
            keywords = listOf("frames", "sequence", "burst", "video", "export"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_thumbnail_extractor",
            name = "Extract Video Thumbnail",
            category = ToolCategory.VIDEO,
            description = "Extract poster art or high-quality cover thumbnail from video files.",
            icon = Icons.Default.PhotoLibrary,
            keywords = listOf("thumbnail", "cover", "poster", "preview"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_extract_audio",
            name = "Extract Video Audio",
            category = ToolCategory.VIDEO,
            description = "Demux audio stream without re-encoding to preserve 100% quality.",
            icon = Icons.Default.Audiotrack,
            keywords = listOf("extract", "audio", "demux", "sound", "rip"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_remove_audio",
            name = "Remove Video Audio",
            category = ToolCategory.VIDEO,
            description = "Strip audio stream completely to produce a silent video file.",
            icon = Icons.Default.VolumeOff,
            keywords = listOf("remove audio", "strip sound", "silent", "mute"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_replace_audio",
            name = "Replace Video Audio",
            category = ToolCategory.VIDEO,
            description = "Swap existing video audio track with a new voiceover or background music.",
            icon = Icons.Default.Shuffle,
            keywords = listOf("replace", "swap", "dub", "voiceover", "music"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_mute",
            name = "Mute Video",
            category = ToolCategory.VIDEO,
            description = "Instantly mute audio track from videos for privacy or social sharing.",
            icon = Icons.Default.MicOff,
            keywords = listOf("mute", "silence", "quiet", "video"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_merger",
            name = "Video Merger",
            category = ToolCategory.VIDEO,
            description = "Merge multiple video clips sequentially into a single seamless video.",
            icon = Icons.Default.CallMerge,
            keywords = listOf("merge", "join", "combine", "concat", "stitch"),
            inputType = ToolInputType.FILE_MULTIPLE
        ),
        ToolDefinition(
            id = "video_splitter",
            name = "Video Splitter",
            category = ToolCategory.VIDEO,
            description = "Split lengthy video into parts by duration, size, or chapters.",
            icon = Icons.Default.CallSplit,
            keywords = listOf("split", "divide", "parts", "chapters"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_trimmer",
            name = "Video Trimmer",
            category = ToolCategory.VIDEO,
            description = "Trim start and end points of video clips with precision seeking.",
            icon = Icons.Default.ContentCut,
            keywords = listOf("trim", "cut", "clip", "shorten", "crop time"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_cutter",
            name = "Video Cutter",
            category = ToolCategory.VIDEO,
            description = "Cut out unwanted segments, ads, or pauses from videos.",
            icon = Icons.Default.ContentCut,
            keywords = listOf("cut", "remove part", "slice", "snip"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_cropper",
            name = "Video Cropper",
            category = ToolCategory.VIDEO,
            description = "Crop video frame dimensions (16:9, 9:16, 1:1, 4:5) for social media.",
            icon = Icons.Default.Crop,
            keywords = listOf("crop", "dimension", "aspect", "instagram", "tiktok"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_rotator",
            name = "Video Rotator",
            category = ToolCategory.VIDEO,
            description = "Rotate video 90°, 180°, or 270° to fix orientation mistakes.",
            icon = Icons.Default.RotateRight,
            keywords = listOf("rotate", "turn", "orientation", "vertical", "horizontal"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_flipper",
            name = "Video Flipper",
            category = ToolCategory.VIDEO,
            description = "Flip video horizontally (mirror) or vertically.",
            icon = Icons.Default.Flip,
            keywords = listOf("flip", "mirror", "invert", "horizontal"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_resizer",
            name = "Video Resizer",
            category = ToolCategory.VIDEO,
            description = "Change video dimensions to 1080p, 720p, 480p, or custom resolution.",
            icon = Icons.Default.AspectRatio,
            keywords = listOf("resize", "resolution", "scale", "1080p", "720p"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_compressor",
            name = "Video Compressor",
            category = ToolCategory.VIDEO,
            description = "Shrink large video file sizes with intelligent bitrate optimization.",
            icon = Icons.Default.Compress,
            keywords = listOf("compress", "shrink", "reduce size", "mb", "email"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_bitrate_changer",
            name = "Video Bitrate Changer",
            category = ToolCategory.VIDEO,
            description = "Adjust video bitrate (kbps) for custom bandwidth and quality.",
            icon = Icons.Default.Tune,
            keywords = listOf("bitrate", "quality", "kbps", "mbps"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_fps_changer",
            name = "Video FPS Changer",
            category = ToolCategory.VIDEO,
            description = "Convert frame rates between 24fps, 30fps, 60fps, or 120fps.",
            icon = Icons.Default.Speed,
            keywords = listOf("fps", "frame rate", "60fps", "30fps", "smooth"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_resolution_changer",
            name = "Video Resolution Changer",
            category = ToolCategory.VIDEO,
            description = "Scale resolution up or down while maintaining aspect ratio.",
            icon = Icons.Default.HighQuality,
            keywords = listOf("resolution", "4k", "1080p", "720p", "scale"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_aspect_ratio_converter",
            name = "Video Aspect Ratio Converter",
            category = ToolCategory.VIDEO,
            description = "Convert between 16:9 widescreen, 9:16 Shorts/Reels, and 1:1 Square.",
            icon = Icons.Default.FitScreen,
            keywords = listOf("aspect ratio", "16:9", "9:16", "1:1", "reels", "tiktok"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_speed_changer",
            name = "Video Speed Changer",
            category = ToolCategory.VIDEO,
            description = "Create slow-motion (0.25x, 0.5x) or timelapse fast-forward (2x, 4x) videos.",
            icon = Icons.Default.FastForward,
            keywords = listOf("speed", "slow motion", "timelapse", "fast forward", "2x"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_reverse",
            name = "Reverse Video",
            category = ToolCategory.VIDEO,
            description = "Play video backwards from end to start for cool rewind effects.",
            icon = Icons.Default.FastRewind,
            keywords = listOf("reverse", "rewind", "backward", "effect"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_freeze_frame",
            name = "Freeze Frame Creator",
            category = ToolCategory.VIDEO,
            description = "Pause and hold a specific dramatic video frame for a set duration.",
            icon = Icons.Default.PauseCircle,
            keywords = listOf("freeze", "pause", "still frame", "hold"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_snapshot",
            name = "Video Snapshot",
            category = ToolCategory.VIDEO,
            description = "Capture an instantaneous lossless screenshot from video playback.",
            icon = Icons.Default.Camera,
            keywords = listOf("snapshot", "screenshot", "grab", "capture"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_color_info",
            name = "Video Color Information",
            category = ToolCategory.VIDEO,
            description = "Inspect color space, HDR/SDR profile, bit depth, and chroma subsampling.",
            icon = Icons.Default.ColorLens,
            keywords = listOf("color", "hdr", "bt709", "bt2020", "sdr", "depth"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_metadata_viewer",
            name = "Video Metadata Viewer",
            category = ToolCategory.VIDEO,
            description = "Inspect container tags, camera model, creation time, and encoder info.",
            icon = Icons.Default.Info,
            keywords = listOf("metadata", "exif", "tags", "codec", "camera", "details"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_metadata_cleaner",
            name = "Video Metadata Remover",
            category = ToolCategory.VIDEO,
            description = "Strip GPS coordinates, device tags, camera info, and AI-generated metadata from videos.",
            icon = Icons.Default.CleaningServices,
            keywords = listOf("video metadata remover", "clean", "privacy", "strip tags", "remove gps", "sanitize", "clear ai"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_duration_calc",
            name = "Video Duration Calculator",
            category = ToolCategory.VIDEO,
            description = "Compute total runtime in milliseconds, frames, and SMPTE timecodes.",
            icon = Icons.Default.Timer,
            keywords = listOf("duration", "runtime", "timecode", "frames", "length"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_size_estimator",
            name = "Video File Size Estimator",
            category = ToolCategory.VIDEO,
            description = "Estimate output file size based on resolution, duration, and target bitrate.",
            icon = Icons.Default.Calculate,
            keywords = listOf("file size", "estimate", "mb", "storage", "bitrate"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_codec_info",
            name = "Video Codec Information",
            category = ToolCategory.VIDEO,
            description = "Analyze video encoding format: H.264 (AVC), H.265 (HEVC), VP9, or AV1.",
            icon = Icons.Default.VideoSettings,
            keywords = listOf("codec", "h264", "h265", "hevc", "av1", "vp9"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_framerate_analyzer",
            name = "Video Frame Rate Analyzer",
            category = ToolCategory.VIDEO,
            description = "Detect constant (CFR) vs variable (VFR) frame rates and dropped frames.",
            icon = Icons.Default.Analytics,
            keywords = listOf("framerate", "fps", "cfr", "vfr", "analyzer"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_audio_track_viewer",
            name = "Video Audio Track Viewer",
            category = ToolCategory.VIDEO,
            description = "List all embedded audio channels, languages, bitrates, and sample rates.",
            icon = Icons.Default.Audiotrack,
            keywords = listOf("audio tracks", "languages", "channels", "streams"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_subtitle_track_viewer",
            name = "Video Subtitle Track Viewer",
            category = ToolCategory.VIDEO,
            description = "View subtitle streams, codecs (SRT, ASS, VTT), and language tracks.",
            icon = Icons.Default.Subtitles,
            keywords = listOf("subtitles", "tracks", "srt", "languages", "cc"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_subtitle_extractor",
            name = "Video Subtitle Extractor",
            category = ToolCategory.VIDEO,
            description = "Extract embedded subtitle streams into editable .srt text files.",
            icon = Icons.Default.FileDownload,
            keywords = listOf("subtitle", "extract", "srt", "text", "rip"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_subtitle_remover",
            name = "Video Subtitle Remover",
            category = ToolCategory.VIDEO,
            description = "Remove soft subtitle tracks from MKV / MP4 video files.",
            icon = Icons.Default.Delete,
            keywords = listOf("subtitle", "remove", "strip", "clean"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_subtitle_muxer",
            name = "Video Subtitle Muxer",
            category = ToolCategory.VIDEO,
            description = "Mux an external SRT subtitle file into a video container.",
            icon = Icons.Default.Add,
            keywords = listOf("mux", "add subtitle", "srt", "embed"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_loop_creator",
            name = "Video Loop Creator",
            category = ToolCategory.VIDEO,
            description = "Generate seamless repeating video loops for backgrounds and kiosks.",
            icon = Icons.Default.Loop,
            keywords = listOf("loop", "repeat", "seamless", "infinite"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_concatenator",
            name = "Video Concatenator",
            category = ToolCategory.VIDEO,
            description = "Stitch matching video segments together with instant stream-copy.",
            icon = Icons.Default.MergeType,
            keywords = listOf("concat", "join", "stitch", "combine"),
            inputType = ToolInputType.FILE_MULTIPLE
        ),
        ToolDefinition(
            id = "video_segment_extractor",
            name = "Video Segment Extractor",
            category = ToolCategory.VIDEO,
            description = "Extract exact time-stamped video intervals without re-encoding.",
            icon = Icons.Default.SelectAll,
            keywords = listOf("segment", "interval", "extract", "range"),
            inputType = ToolInputType.VIDEO_SINGLE
        ),
        ToolDefinition(
            id = "video_contact_sheet",
            name = "Video Contact Sheet Generator",
            category = ToolCategory.VIDEO,
            description = "Generate a grid of preview storyboard screenshots summarizing a video.",
            icon = Icons.Default.GridOn,
            keywords = listOf("contact sheet", "storyboard", "thumbnails", "grid", "preview"),
            inputType = ToolInputType.VIDEO_SINGLE
        )
    )
}
