package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.repository.ThemeMode
import com.example.core.util.FileUtils

enum class SettingsCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    APPEARANCE(
        title = "Appearance & Theme",
        subtitle = "Dark mode, light mode, or system default",
        icon = Icons.Default.DarkMode
    ),
    MEDIA_STORAGE(
        title = "Media & Gallery Auto-Save",
        subtitle = "Automatic mobile memory saving configurations",
        icon = Icons.Default.SaveAlt
    ),
    STORAGE_CACHE(
        title = "Storage & Cache Cleanup",
        subtitle = "Clear temporary scratch files & reclaim space",
        icon = Icons.Default.Storage
    ),
    DEVELOPER_INFO(
        title = "Developer Information",
        subtitle = "Creator profile, contact email, and architecture",
        icon = Icons.Default.Person
    ),
    ABOUT_APP(
        title = "About ToolBox",
        subtitle = "286+ Offline tools breakdown, version & license",
        icon = Icons.Default.Info
    ),
    PRIVACY_POLICY(
        title = "Privacy & Security",
        subtitle = "100% on-device processing and zero tracking guarantee",
        icon = Icons.Default.Security
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    autoSaveMedia: Boolean,
    onAutoSaveChange: (Boolean) -> Unit,
    cacheSizeBytes: Long,
    onClearCache: () -> Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var currentCacheSize by remember { mutableStateOf(cacheSizeBytes) }
    var activeCategory by remember { mutableStateOf<SettingsCategory?>(null) }

    // Intercept back button to return to Settings root if a sub-screen is open
    BackHandler(enabled = activeCategory != null) {
        activeCategory = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activeCategory?.title ?: "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeCategory != null) {
                                activeCategory = null
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        AnimatedContent(
            targetState = activeCategory,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width / 2 } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width / 2 } + fadeOut()
                }
            },
            label = "settings_navigation"
        ) { category ->
            if (category == null) {
                // Button-Based Root Menu
                SettingsRootMenu(
                    themeMode = themeMode,
                    autoSaveMedia = autoSaveMedia,
                    currentCacheSize = currentCacheSize,
                    onSelectCategory = { activeCategory = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            } else {
                // Sub-Screen View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (category) {
                        SettingsCategory.APPEARANCE -> AppearanceSubScreen(
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange
                        )
                        SettingsCategory.MEDIA_STORAGE -> MediaStorageSubScreen(
                            autoSaveMedia = autoSaveMedia,
                            onAutoSaveChange = onAutoSaveChange
                        )
                        SettingsCategory.STORAGE_CACHE -> StorageCacheSubScreen(
                            currentCacheSize = currentCacheSize,
                            onClearCache = {
                                val freed = onClearCache()
                                currentCacheSize = 0L
                                Toast.makeText(
                                    context,
                                    "Cleared ${FileUtils.formatFileSize(freed)} temporary cache",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        SettingsCategory.DEVELOPER_INFO -> DeveloperInfoSubScreen(
                            onCopyEmail = { email ->
                                clipboardManager.setText(AnnotatedString(email))
                                Toast.makeText(context, "Email copied: $email", Toast.LENGTH_SHORT).show()
                            },
                            onSendEmail = { email ->
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$email")
                                    putExtra(Intent.EXTRA_SUBJECT, "ToolBox App Inquiry & Feedback")
                                }
                                try {
                                    context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                                } catch (_: Exception) {
                                    clipboardManager.setText(AnnotatedString(email))
                                    Toast.makeText(context, "Email copied: $email", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        SettingsCategory.ABOUT_APP -> AboutSubScreen(
                            onShareApp = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "ToolBox App")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out ToolBox: Universal Offline Multi-Tool Platform with 286+ powerful on-device utilities!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share ToolBox"))
                            }
                        )
                        SettingsCategory.PRIVACY_POLICY -> PrivacySubScreen()
                    }
                }
            }
        }
    }
}

/**
 * Root Button-Based Menu
 */
@Composable
private fun SettingsRootMenu(
    themeMode: ThemeMode,
    autoSaveMedia: Boolean,
    currentCacheSize: Long,
    onSelectCategory: (SettingsCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ToolBox Control Center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "286+ On-Device Tools • 100% Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 1: Preferences
        Text(
            text = "App Preferences",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        SettingNavigationButton(
            title = "Appearance & Theme",
            subtitle = "Visual style & dark mode",
            statusBadge = themeMode.title,
            icon = Icons.Default.DarkMode,
            onClick = { onSelectCategory(SettingsCategory.APPEARANCE) }
        )

        SettingNavigationButton(
            title = "Media & Gallery Auto-Save",
            subtitle = "Automatic mobile storage saving",
            statusBadge = if (autoSaveMedia) "Auto-Save Active" else "Manual Save",
            icon = Icons.Default.SaveAlt,
            onClick = { onSelectCategory(SettingsCategory.MEDIA_STORAGE) }
        )

        SettingNavigationButton(
            title = "Storage & Cache Cleanup",
            subtitle = "Manage scratch buffers & cache",
            statusBadge = FileUtils.formatFileSize(currentCacheSize),
            icon = Icons.Default.Storage,
            onClick = { onSelectCategory(SettingsCategory.STORAGE_CACHE) }
        )

        // Section 2: Developer & About
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Developer & System",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        SettingNavigationButton(
            title = "Developer Information",
            subtitle = "Hasan • Lead Software Architect",
            statusBadge = "Verified",
            icon = Icons.Default.Person,
            highlight = true,
            onClick = { onSelectCategory(SettingsCategory.DEVELOPER_INFO) }
        )

        SettingNavigationButton(
            title = "About ToolBox",
            subtitle = "286 Tools breakdown & architecture",
            statusBadge = "v1.0.0",
            icon = Icons.Default.Info,
            onClick = { onSelectCategory(SettingsCategory.ABOUT_APP) }
        )

        SettingNavigationButton(
            title = "Privacy & Security",
            subtitle = "Zero tracking & client-side sandbox",
            statusBadge = "100% Offline",
            icon = Icons.Default.Security,
            onClick = { onSelectCategory(SettingsCategory.PRIVACY_POLICY) }
        )
    }
}

/**
 * Clickable Setting Navigation Button / Card
 */
@Composable
private fun SettingNavigationButton(
    title: String,
    subtitle: String,
    statusBadge: String,
    icon: ImageVector,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = statusBadge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 1. Appearance Sub-Screen
 */
@Composable
private fun AppearanceSubScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Select Theme Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose your preferred visual styling across all 286+ tools.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ThemeMode.values().forEach { mode ->
            val isSelected = themeMode == mode
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeModeChange(mode) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onThemeModeChange(mode) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                        Text(
                            text = when (mode) {
                                ThemeMode.SYSTEM -> "Syncs automatically with your Android system settings"
                                ThemeMode.LIGHT -> "Bright, crisp Daylight design system"
                                ThemeMode.DARK -> "OLED Deep Black design for battery efficiency"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Media & Storage Sub-Screen
 */
@Composable
private fun MediaStorageSubScreen(
    autoSaveMedia: Boolean,
    onAutoSaveChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Save to Mobile Storage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "মিডিয়া ফাইল তৈরি হওয়ার সাথে সাথে স্বয়ংক্রিয়ভাবে ডিভাইসের প্রধান গ্যালারি ও মেমরিতে সেভ হবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = autoSaveMedia,
                        onCheckedChange = onAutoSaveChange
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (autoSaveMedia) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (autoSaveMedia) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (autoSaveMedia) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (autoSaveMedia)
                                "Files are immediately registered in Android MediaStore"
                            else
                                "Files stay in scratch cache until you tap 'Save to Device'",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Text(
            text = "Storage Target Folders",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        listOf(
            Triple("Videos & Remuxes", "Movies/ToolBox", Icons.Default.Movie),
            Triple("Audio & Music Tracks", "Music/ToolBox", Icons.Default.Audiotrack),
            Triple("Photos & Images", "Pictures/ToolBox", Icons.Default.Image),
            Triple("PDF & Archives", "Download/ToolBox", Icons.Default.Description)
        ).forEach { (category, path, icon) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(category, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(path, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * 3. Storage & Cache Sub-Screen
 */
@Composable
private fun StorageCacheSubScreen(
    currentCacheSize: Long,
    onClearCache: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = FileUtils.formatFileSize(currentCacheSize),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Temporary Cache & Scratch Buffers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onClearCache,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Temporary Cache Now")
                }
            }
        }

        Text(
            text = "Cache Breakdown",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        listOf(
            "Video Render Scratch" to "Temporary muxer chunks generated during video trimming/remuxing",
            "Audio PCM Buffers" to "WAV buffer frames decoded during volume boosting & speed change",
            "Image Compression Temp" to "Downscaled bitmap caches prior to gallery export",
            "Extracted Archive Folders" to "Unzipped content waiting for user file picker interaction"
        ).forEach { (title, subtitle) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * 4. Dedicated Developer Information Sub-Screen (ডেভেলপারের তথ্য)
 */
@Composable
private fun DeveloperInfoSubScreen(
    onCopyEmail: (String) -> Unit,
    onSendEmail: (String) -> Unit
) {
    val developerEmail = "spearhasan@gmail.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Developer Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with Badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Hasan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Lead Developer & Systems Architect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "ToolBox Creator • Verified Architect",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Email Display & Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = developerEmail,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onSendEmail(developerEmail) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Email")
                    }

                    OutlinedButton(
                        onClick = { onCopyEmail(developerEmail) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Email")
                    }
                }
            }
        }

        // Section: Contact & Inquiries
        Text(
            text = "Direct Developer Channels",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        DeveloperInfoTile(
            icon = Icons.Default.ContactSupport,
            title = "Feature Requests & Custom Tools",
            subtitle = "Direct email feedback accepted for adding new tool modules"
        )

        DeveloperInfoTile(
            icon = Icons.Default.BugReport,
            title = "Bug Reports & Feedback",
            subtitle = "Fast responses for crashes, format issues, or edge cases"
        )

        // Section: Architecture & Technical Expertise
        Text(
            text = "Engine & Architecture Overview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        DeveloperInfoTile(
            icon = Icons.Default.Terminal,
            title = "High-Performance Kotlin Stack",
            subtitle = "Jetpack Compose M3, Coroutines, Flow, AndroidX Media3 & Exif"
        )

        DeveloperInfoTile(
            icon = Icons.Default.Build,
            title = "286+ Offline Utilities",
            subtitle = "Zero server dependencies — all compute runs 100% on device silicon"
        )

        DeveloperInfoTile(
            icon = Icons.Default.VerifiedUser,
            title = "Privacy-First Architecture",
            subtitle = "No analytics, no telemetry, no cloud uploads, zero user tracking"
        )
    }
}

@Composable
private fun DeveloperInfoTile(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * 5. About ToolBox Sub-Screen
 */
@Composable
private fun AboutSubScreen(
    onShareApp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ToolBox", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Universal Offline Multi-Tool Platform", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text("v1.0.0", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                HorizontalDivider()

                Text("• 286 Built-in Native Tools across 12 Modules")
                Text("• 100% On-Device Processing (No Cloud Required)")
                Text("• Built with Kotlin & Jetpack Compose Material 3")
                Text("• Open Source Apache 2.0 License")

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onShareApp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share ToolBox App")
                }
            }
        }

        Text(
            text = "Module Breakdown (286 Tools)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        listOf(
            "Video Tools" to "47 tools (Trim, Speed, Rotator, Remuxer, Lossless AI Cleaner)",
            "Audio Tools" to "37 tools (Trim, Speed, Volume Boost, Bass, Mono/Stereo, ID3 Clean)",
            "Image Tools" to "45 tools (Crop, Resize, Filters, Watermark, EXIF AI Cleaner)",
            "Text & Document Tools" to "53 tools (Case Convert, Diffs, Markdown, HTML, CSV)",
            "PDF Tools" to "26 tools (Merge, Extract, Delete, Watermark, Convert)",
            "GIF Tools" to "14 tools (Video to GIF, Frames, Speed, Reverse)",
            "OCR Tools" to "8 tools (Photo to Text, Screen to Clipboard)",
            "Developer Tools" to "24 tools (JSON, Base64, Hashes, RegEx, UUID)",
            "QR & Barcode" to "16 tools (Custom QR Generator & Multi-Format Scanner)",
            "File & Archive" to "6 tools (ZIP Packer, Extractor, File Sanitizer)",
            "Math & Unit Calculators" to "10 tools (Percentage, EMI, Unit Converter)"
        ).forEach { (name, details) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * 6. Privacy & Security Sub-Screen
 */
@Composable
private fun PrivacySubScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Privacy & Security Commitment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider()

                Text("• Zero Data Collection: We do not collect, read, or upload any personal or usage data.")
                Text("• 100% Client-Side: All transformations execute strictly inside your smartphone RAM and local storage sandbox.")
                Text("• Zero Trackers / Zero Ads: Absolutely no third-party tracking SDKs or advertising frameworks.")
                Text("• Metadata Stripping: Built-in tools allow you to erase GPS, EXIF, and AI prompts from your media files before sharing.")
            }
        }
    }
}
