package com.example.core.registry.modules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.model.ToolInputType

object AudioToolsModule {

    val tools: List<ToolDefinition> = listOf(
        ToolDefinition(
            id = "audio_cutter",
            name = "Audio Cutter",
            category = ToolCategory.AUDIO,
            description = "Cut, crop, and save custom ringtones or clips from songs.",
            icon = Icons.Default.ContentCut,
            keywords = listOf("cut", "crop", "ringtone", "slice", "music"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_trimmer",
            name = "Audio Trimmer",
            category = ToolCategory.AUDIO,
            description = "Trim start and ending silences or unwanted segments from audio tracks.",
            icon = Icons.Default.ContentCut,
            keywords = listOf("trim", "shorten", "audio", "voice", "recording"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_splitter",
            name = "Audio Splitter",
            category = ToolCategory.AUDIO,
            description = "Split long podcasts or audiobooks into discrete tracks or chapters.",
            icon = Icons.Default.CallSplit,
            keywords = listOf("split", "divide", "podcast", "audiobook", "chapters"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_merger",
            name = "Audio Merger",
            category = ToolCategory.AUDIO,
            description = "Merge multiple audio recordings into one continuous track.",
            icon = Icons.Default.CallMerge,
            keywords = listOf("merge", "combine", "songs", "join", "stitch"),
            inputType = ToolInputType.FILE_MULTIPLE
        ),
        ToolDefinition(
            id = "audio_joiner",
            name = "Audio Joiner",
            category = ToolCategory.AUDIO,
            description = "Sequentially join multiple audio clips with seamless transitions.",
            icon = Icons.Default.MergeType,
            keywords = listOf("joiner", "concat", "playlist", "audio"),
            inputType = ToolInputType.FILE_MULTIPLE
        ),
        ToolDefinition(
            id = "audio_converter",
            name = "Audio Converter",
            category = ToolCategory.AUDIO,
            description = "Universal audio converter between MP3, AAC, WAV, M4A, and FLAC.",
            icon = Icons.Default.Transform,
            keywords = listOf("convert", "format", "mp3", "wav", "m4a", "flac"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_mp3_to_wav",
            name = "MP3 → WAV",
            category = ToolCategory.AUDIO,
            description = "Decode compressed MP3 audio into uncompressed PCM WAV format.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("mp3", "wav", "decode", "pcm", "uncompressed"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_wav_to_mp3",
            name = "WAV → MP3",
            category = ToolCategory.AUDIO,
            description = "Compress large WAV studio files into compact high-bitrate MP3s.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("wav", "mp3", "compress", "encode"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_mp3_to_m4a",
            name = "MP3 → M4A",
            category = ToolCategory.AUDIO,
            description = "Convert MP3 to modern MPEG-4 AAC / ALAC audio container.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("mp3", "m4a", "aac", "apple"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_m4a_to_mp3",
            name = "M4A → MP3",
            category = ToolCategory.AUDIO,
            description = "Convert voice memos and Apple M4A recordings into universal MP3.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("m4a", "mp3", "voice memo", "convert"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_flac_to_mp3",
            name = "FLAC → MP3",
            category = ToolCategory.AUDIO,
            description = "Convert high-resolution FLAC albums into portable 320kbps MP3s.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("flac", "mp3", "album", "portable"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_wav_to_flac",
            name = "WAV → FLAC",
            category = ToolCategory.AUDIO,
            description = "Losslessly compress studio WAV masters into space-saving FLAC.",
            icon = Icons.Default.SwapHoriz,
            keywords = listOf("wav", "flac", "lossless", "archive"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_compressor",
            name = "Audio Compressor",
            category = ToolCategory.AUDIO,
            description = "Shrink audio file size by optimizing bitrate, channels, and sample rate.",
            icon = Icons.Default.Compress,
            keywords = listOf("compress", "reduce size", "shrink", "optimize"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_bitrate_changer",
            name = "Audio Bitrate Changer",
            category = ToolCategory.AUDIO,
            description = "Change bitrate between 64kbps, 128kbps, 192kbps, 256kbps, and 320kbps.",
            icon = Icons.Default.Tune,
            keywords = listOf("bitrate", "quality", "kbps", "320kbps", "128kbps"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_sample_rate_changer",
            name = "Audio Sample Rate Changer",
            category = ToolCategory.AUDIO,
            description = "Resample audio frequency (44.1kHz CD audio, 48kHz Video, 96kHz Hi-Res).",
            icon = Icons.Default.GraphicEq,
            keywords = listOf("sample rate", "hz", "44100", "48000", "frequency"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_channel_converter",
            name = "Audio Channel Converter",
            category = ToolCategory.AUDIO,
            description = "Switch audio channel layout between Stereo (2.0), Mono (1.0), and Surround.",
            icon = Icons.Default.Headphones,
            keywords = listOf("channel", "stereo", "mono", "layout"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_stereo_to_mono",
            name = "Stereo → Mono",
            category = ToolCategory.AUDIO,
            description = "Downmix two-channel stereo audio into a single balanced mono channel.",
            icon = Icons.Default.VolumeDown,
            keywords = listOf("stereo", "mono", "downmix", "combine channels"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_mono_to_stereo",
            name = "Mono → Stereo",
            category = ToolCategory.AUDIO,
            description = "Duplicate mono audio track into left and right stereo channels.",
            icon = Icons.Default.VolumeUp,
            keywords = listOf("mono", "stereo", "dual channel"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_volume_booster",
            name = "Volume Booster",
            category = ToolCategory.AUDIO,
            description = "Amplify quiet audio and recordings up to 200% with gain limiter.",
            icon = Icons.Default.VolumeUp,
            keywords = listOf("boost", "loudness", "amplify", "volume", "gain"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_volume_normalizer",
            name = "Volume Normalizer",
            category = ToolCategory.AUDIO,
            description = "Normalize peak loudness (EBU R128 standard) across songs.",
            icon = Icons.Default.GraphicEq,
            keywords = listOf("normalize", "lufs", "ebu", "level", "even volume"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_fade_in",
            name = "Audio Fade In",
            category = ToolCategory.AUDIO,
            description = "Add smooth progressive fade-in volume ramp at track beginning.",
            icon = Icons.Default.TrendingUp,
            keywords = listOf("fade in", "intro", "smooth", "volume ramp"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_fade_out",
            name = "Audio Fade Out",
            category = ToolCategory.AUDIO,
            description = "Add smooth fading out volume decay at the ending of an audio clip.",
            icon = Icons.Default.TrendingDown,
            keywords = listOf("fade out", "outro", "decay", "ending"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_silence_detector",
            name = "Audio Silence Detector",
            category = ToolCategory.AUDIO,
            description = "Scan recordings for silent pauses, gaps, and dead air timestamps.",
            icon = Icons.Default.GraphicEq,
            keywords = listOf("silence", "pause", "dead air", "gaps", "scan"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_silence_remover",
            name = "Silence Remover",
            category = ToolCategory.AUDIO,
            description = "Automatically detect and cut out silent pauses from lectures & interviews.",
            icon = Icons.Default.CleaningServices,
            keywords = listOf("remove silence", "cut pauses", "truncate", "trim silent"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_speed_changer",
            name = "Audio Speed Changer",
            category = ToolCategory.AUDIO,
            description = "Change playback tempo (0.5x to 2.5x) with pitch correction option.",
            icon = Icons.Default.Speed,
            keywords = listOf("speed", "tempo", "podcast", "faster", "slower"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_pitch_changer",
            name = "Audio Pitch Changer",
            category = ToolCategory.AUDIO,
            description = "Shift musical pitch in semitones up or down without affecting tempo.",
            icon = Icons.Default.Tune,
            keywords = listOf("pitch", "semitones", "key", "transpose", "vocal"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_reverse",
            name = "Reverse Audio",
            category = ToolCategory.AUDIO,
            description = "Play audio backwards for sound design, backwards speech, or effects.",
            icon = Icons.Default.FastRewind,
            keywords = listOf("reverse", "backwards", "rewind", "sfx"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_loop_creator",
            name = "Audio Loop Creator",
            category = ToolCategory.AUDIO,
            description = "Create repeating audio loops for soundscapes, white noise, and beats.",
            icon = Icons.Default.Loop,
            keywords = listOf("loop", "repeat", "ambient", "white noise", "beat"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_waveform_generator",
            name = "Audio Waveform Generator",
            category = ToolCategory.AUDIO,
            description = "Generate visual amplitude waveform graphics from audio tracks.",
            icon = Icons.Default.GraphicEq,
            keywords = listOf("waveform", "amplitude", "visualizer", "peaks"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_metadata_viewer",
            name = "Audio Metadata Viewer",
            category = ToolCategory.AUDIO,
            description = "Inspect ID3 tags: artist, title, album, year, genre, and bitrate.",
            icon = Icons.Default.Info,
            keywords = listOf("id3", "tags", "artist", "album", "metadata"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_metadata_cleaner",
            name = "Audio Metadata Cleaner",
            category = ToolCategory.AUDIO,
            description = "Strip hidden comments, encoder tags, and private metadata from songs.",
            icon = Icons.Default.CleaningServices,
            keywords = listOf("clean tags", "strip id3", "sanitize", "privacy"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_album_art_extractor",
            name = "Album Art Extractor",
            category = ToolCategory.AUDIO,
            description = "Extract high-resolution front cover artwork embedded inside audio files.",
            icon = Icons.Default.Image,
            keywords = listOf("album art", "cover", "artwork", "extract image"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_duration_calc",
            name = "Audio Duration Calculator",
            category = ToolCategory.AUDIO,
            description = "Calculate exact runtime in minutes, seconds, and total sample count.",
            icon = Icons.Default.Timer,
            keywords = listOf("duration", "runtime", "length", "samples"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_filesize_calc",
            name = "Audio File Size Calculator",
            category = ToolCategory.AUDIO,
            description = "Calculate expected file size for given bitrate, channels, and length.",
            icon = Icons.Default.Calculate,
            keywords = listOf("file size", "bitrate", "mb", "calculator"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_frequency_analyzer",
            name = "Audio Frequency Analyzer",
            category = ToolCategory.AUDIO,
            description = "Analyze frequency distribution and cutoff thresholds.",
            icon = Icons.Default.Analytics,
            keywords = listOf("frequency", "hz", "cutoff", "analyzer"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_spectrum_analyzer",
            name = "Audio Spectrum Analyzer",
            category = ToolCategory.AUDIO,
            description = "Visualize real-time or offline FFT sound frequency spectrum.",
            icon = Icons.Default.GraphicEq,
            keywords = listOf("spectrum", "fft", "frequencies", "equalizer"),
            inputType = ToolInputType.AUDIO_SINGLE
        ),
        ToolDefinition(
            id = "audio_channel_analyzer",
            name = "Audio Channel Analyzer",
            category = ToolCategory.AUDIO,
            description = "Inspect phase alignment, channel balance, and L/R loudness differences.",
            icon = Icons.Default.Balance,
            keywords = listOf("channels", "phase", "balance", "left right"),
            inputType = ToolInputType.AUDIO_SINGLE
        )
    )
}
