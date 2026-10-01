package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.core.model.ToolCategory
import com.example.core.registry.ToolRegistry
import com.example.ui.screens.*
import com.example.ui.screens.tools.*
import com.example.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object History : Screen("history", "History", Icons.Default.History)
}

@Composable
fun ToolBoxApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(Screen.Home, Screen.Favorites, Screen.History)
    val isBottomNavVisible = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (isBottomNavVisible) {
                NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = if (isBottomNavVisible) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                val tools by viewModel.filteredTools.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
                val favoriteIds by viewModel.favoriteToolIds.collectAsStateWithLifecycle()

                HomeScreen(
                    tools = tools,
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChanged,
                    selectedCategory = selectedCategory,
                    onCategorySelect = viewModel::onCategorySelected,
                    favoriteToolIds = favoriteIds,
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onToolClick = { tool ->
                        navController.navigate("tool/${tool.id}")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    }
                )
            }

            // Favorites Screen
            composable(Screen.Favorites.route) {
                val favTools by viewModel.favoriteTools.collectAsStateWithLifecycle()

                FavoritesScreen(
                    favoriteTools = favTools,
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onToolClick = { tool ->
                        navController.navigate("tool/${tool.id}")
                    },
                    onExploreClick = {
                        navController.navigate(Screen.Home.route)
                    }
                )
            }

            // History Screen
            composable(Screen.History.route) {
                val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()

                HistoryScreen(
                    historyItems = historyItems,
                    onDeleteItem = viewModel::deleteHistoryItem,
                    onClearAll = viewModel::clearAllHistory
                )
            }

            // Settings Screen
            composable("settings") {
                val cacheSize by viewModel.cacheSizeBytes.collectAsStateWithLifecycle()
                val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
                val autoSaveMedia by viewModel.autoSaveMedia.collectAsStateWithLifecycle()

                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = viewModel::setThemeMode,
                    autoSaveMedia = autoSaveMedia,
                    onAutoSaveChange = viewModel::setAutoSaveMedia,
                    cacheSizeBytes = cacheSize,
                    onClearCache = viewModel::clearTemporaryCache,
                    onBack = { navController.popBackStack() }
                )
            }

            // Generic Tool Route
            composable(
                route = "tool/{toolId}",
                arguments = listOf(navArgument("toolId") { type = NavType.StringType })
            ) { backStackEntry ->
                val toolId = backStackEntry.arguments?.getString("toolId") ?: ""
                val tool = ToolRegistry.getToolById(toolId)

                if (tool != null) {
                    val favoriteIds by viewModel.favoriteToolIds.collectAsStateWithLifecycle()
                    val autoSaveMedia by viewModel.autoSaveMedia.collectAsStateWithLifecycle()
                    val isFav = favoriteIds.contains(tool.id)

                    when {
                        tool.category == ToolCategory.QR || tool.id == "qr_generator" -> {
                            QrToolsScreen(
                                tool = tool,
                                onBack = { navController.popBackStack() },
                                isFavorite = isFav,
                                onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                                onRecordHistory = { name, summary -> viewModel.recordHistory(tool.id, name, summary) }
                            )
                        }
                        tool.category == ToolCategory.IMAGE -> {
                            ImageToolsScreen(
                                tool = tool,
                                onBack = { navController.popBackStack() },
                                isFavorite = isFav,
                                onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                                onRecordHistory = { name, summary -> viewModel.recordHistory(tool.id, name, summary) },
                                autoSaveEnabled = autoSaveMedia
                            )
                        }
                        tool.category in listOf(ToolCategory.VIDEO, ToolCategory.AUDIO, ToolCategory.GIF) -> {
                            MediaToolsScreen(
                                tool = tool,
                                onBack = { navController.popBackStack() },
                                isFavorite = isFav,
                                onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                                onRecordHistory = { name, summary -> viewModel.recordHistory(tool.id, name, summary) },
                                autoSaveEnabled = autoSaveMedia
                            )
                        }
                        tool.category == ToolCategory.PDF -> {
                            PdfToolsScreen(
                                tool = tool,
                                onBack = { navController.popBackStack() },
                                isFavorite = isFav,
                                onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                                onRecordHistory = { name, summary -> viewModel.recordHistory(tool.id, name, summary) },
                                autoSaveEnabled = autoSaveMedia
                            )
                        }
                        tool.category in listOf(ToolCategory.TEXT, ToolCategory.DOCUMENT, ToolCategory.OCR) -> {
                            TextToolsScreen(
                                tool = tool,
                                onBack = { navController.popBackStack() },
                                isFavorite = isFav,
                                onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                                onRecordHistory = { name, summary -> viewModel.recordHistory(tool.id, name, summary) }
                            )
                        }
                        tool.category == ToolCategory.DEVELOPER -> {
                            DeveloperToolsScreen(
                                tool = tool,
                                onBack = { navController.popBackStack() },
                                isFavorite = isFav,
                                onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                                onRecordHistory = { name, summary -> viewModel.recordHistory(tool.id, name, summary) }
                            )
                        }
                        tool.category == ToolCategory.CALCULATOR -> {
                            CalculatorToolsScreen(
                                tool = tool,
                                onBack = { navController.popBackStack() },
                                isFavorite = isFav,
                                onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                                onRecordHistory = { name, summary -> viewModel.recordHistory(tool.id, name, summary) }
                            )
                        }
                        tool.category == ToolCategory.CONVERTER -> {
                            ConverterToolsScreen(
                                tool = tool,
                                onBack = { navController.popBackStack() },
                                isFavorite = isFav,
                                onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                                onRecordHistory = { name, summary -> viewModel.recordHistory(tool.id, name, summary) }
                            )
                        }
                        tool.category == ToolCategory.FILE -> {
                            FileArchiveToolsScreen(
                                tool = tool,
                                onBack = { navController.popBackStack() },
                                isFavorite = isFav,
                                onFavoriteToggle = { viewModel.toggleFavorite(tool.id) },
                                onRecordHistory = { name, summary -> viewModel.recordHistory(tool.id, name, summary) }
                            )
                        }
                    }
                }
            }
        }
    }
}
