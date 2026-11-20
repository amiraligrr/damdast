package ir.amiraligrr.damdast.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.amiraligrr.damdast.BuildConfig
import ir.amiraligrr.damdast.R
import ir.amiraligrr.damdast.data.AppSettings
import ir.amiraligrr.damdast.data.DamdastViewModel
import ir.amiraligrr.damdast.damdastApp
import ir.amiraligrr.damdast.ads.ui.BannerAdSlot
import ir.amiraligrr.damdast.ads.ui.NativeAdCard
import ir.amiraligrr.damdast.ads.ui.PremiumToolGate
import ir.amiraligrr.damdast.ads.ui.RewardedUnlockDialog
import ir.amiraligrr.damdast.tools.ToolCatalog
import ir.amiraligrr.damdast.tools.ToolCategory
import ir.amiraligrr.damdast.ui.components.DamdastBottomBar
import ir.amiraligrr.damdast.ui.components.LocalSnackbarHostState
import ir.amiraligrr.damdast.ui.components.ToolPage
import ir.amiraligrr.damdast.ui.components.ErrorPane
import ir.amiraligrr.damdast.ui.history.HistoryScreen
import ir.amiraligrr.damdast.ui.home.CategoriesScreen
import ir.amiraligrr.damdast.ui.home.CategoryDetailScreen
import ir.amiraligrr.damdast.ui.home.FavoritesScreen
import ir.amiraligrr.damdast.ui.home.HomeScreen
import ir.amiraligrr.damdast.ui.search.SearchScreen
import ir.amiraligrr.damdast.ui.settings.AboutScreen
import ir.amiraligrr.damdast.ui.settings.LicensesScreen
import ir.amiraligrr.damdast.ui.settings.PrivacyPolicyScreen
import ir.amiraligrr.damdast.ui.settings.SettingsScreen
import ir.amiraligrr.damdast.ui.theme.Motion
import ir.amiraligrr.damdast.ui.tools.CalculatorToolsScreen
import ir.amiraligrr.damdast.ui.tools.ColorToolsScreen
import ir.amiraligrr.damdast.ui.tools.DateToolsScreen
import ir.amiraligrr.damdast.ui.tools.DeveloperToolsScreen
import ir.amiraligrr.damdast.ui.tools.EverydayToolsScreen
import ir.amiraligrr.damdast.ui.tools.FileToolsScreen
import ir.amiraligrr.damdast.ui.tools.ImageToolsScreen
import ir.amiraligrr.damdast.ui.tools.PdfToolsScreen
import ir.amiraligrr.damdast.ui.tools.QrToolsScreen
import ir.amiraligrr.damdast.ui.tools.TextToolsScreen
import ir.amiraligrr.damdast.util.findActivity
import ir.amiraligrr.damdast.util.openUrl
import kotlinx.coroutines.launch

private val mainRoutes = setOf("home", "categories", "favorites", "history", "settings")
private const val ISSUES_URL = "https://github.com/amiraligrr/damdast/issues"

@Composable
fun MainShell(
    viewModel: DamdastViewModel,
    settings: AppSettings,
    shortcutTarget: String?,
    onShortcutConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recent by viewModel.recentTools.collectAsStateWithLifecycle()
    val scans by viewModel.scanHistory.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentToolId = backStackEntry?.arguments?.getString("toolId")
    val isMain = currentRoute in mainRoutes || currentRoute == null
    val adsManager = context.damdastApp.adsManager
    val adsState by adsManager.state.collectAsStateWithLifecycle()
    val adsEnabled = BuildConfig.ADS_CONFIGURED && !BuildConfig.ADS_DISABLED_BY_CONFIG &&
        !(BuildConfig.DEBUG && settings.adsDebugDisabled)
    var showRewardDialog by remember { mutableStateOf(false) }
    var lastToolId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(adsEnabled) {
        adsManager.setEnabled(adsEnabled)
    }
    LaunchedEffect(currentRoute, currentToolId, adsEnabled) {
        if (currentToolId != null) {
            lastToolId = currentToolId
            return@LaunchedEffect
        }
        if (currentRoute == "home") {
            val finished = lastToolId
            lastToolId = null
            if (finished != null && adsEnabled) {
                val category = ToolCatalog.byId[finished]?.category
                if (category != null && category !in setOf(
                        ToolCategory.QR,
                        ToolCategory.PDF,
                        ToolCategory.IMAGE,
                        ToolCategory.FILE,
                    )
                ) {
                    context.findActivity()?.let { adsManager.onToolCompleted(it) }
                }
            }
        } else if (currentRoute != null) {
            lastToolId = null
        }
    }

    fun openMain(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openTool(toolId: String) {
        navController.navigate("tool/$toolId")
    }

    fun message(text: String) {
        scope.launch { snackbarHostState.showSnackbar(text) }
    }

    LaunchedEffect(shortcutTarget) {
        val target = shortcutTarget ?: return@LaunchedEffect
        if (ToolCatalog.byId.containsKey(target)) {
            navController.navigate("tool/$target")
        }
        onShortcutConsumed()
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(tween(Motion.Medium)) + slideInHorizontally(
                        tween(Motion.Medium),
                        initialOffsetX = { it / 10 },
                    )
                },
                exitTransition = {
                    fadeOut(tween(Motion.Fast)) + slideOutHorizontally(
                        tween(Motion.Fast),
                        targetOffsetX = { -it / 14 },
                    )
                },
                popEnterTransition = {
                    fadeIn(tween(Motion.Medium)) + slideInHorizontally(
                        tween(Motion.Medium),
                        initialOffsetX = { -it / 10 },
                    )
                },
                popExitTransition = {
                    fadeOut(tween(Motion.Fast)) + slideOutHorizontally(
                        tween(Motion.Fast),
                        targetOffsetX = { it / 14 },
                    )
                },
            ) {
                composable("home") {
                    HomeScreen(
                        favorites = favorites,
                        recent = recent,
                        onOpenTool = ::openTool,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onOpenSearch = { navController.navigate("search") },
                        onOpenCategories = { openMain("categories") },
                        onOpenCategory = { category ->
                            navController.navigate("category/${category.name}")
                        },
                        onOpenFavorites = { openMain("favorites") },
                        onOpenHistory = { openMain("history") },
                        onOpenSettings = { openMain("settings") },
                        nativeAdSlot = if (adsEnabled) {
                            { NativeAdCard(adsManager = adsManager, enabled = adsEnabled) }
                        } else {
                            null
                        },
                        bannerSlot = if (adsEnabled) {
                            { BannerAdSlot(adsManager = adsManager, enabled = adsEnabled) }
                        } else {
                            null
                        },
                    )
                }
                composable("categories") {
                    CategoriesScreen(
                        favorites = favorites,
                        onOpenTool = ::openTool,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onOpenSearch = { navController.navigate("search") },
                    )
                }
                composable("favorites") {
                    FavoritesScreen(
                        favorites = favorites,
                        onOpenTool = ::openTool,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onExplore = { openMain("categories") },
                    )
                }
                composable("history") {
                    HistoryScreen(
                        recent = recent,
                        scans = scans,
                        favorites = favorites,
                        onOpenTool = ::openTool,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onDeleteScan = viewModel::deleteScan,
                        onClearHistory = viewModel::clearHistory,
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        settings = settings,
                        onThemeSelected = viewModel::setTheme,
                        onLanguageSelected = viewModel::setLanguage,
                        onClearHistory = viewModel::clearHistory,
                        onRestartOnboarding = viewModel::restartOnboarding,
                        onOpenAbout = { navController.navigate("about") },
                        onOpenPrivacy = { navController.navigate("privacy") },
                        onOpenLicenses = { navController.navigate("licenses") },
                        onFeedback = {
                            if (!openUrl(context, ISSUES_URL)) {
                                message(context.getString(R.string.link_open_failed))
                            }
                        },
                        onAdsDebugDisabledChanged = viewModel::setAdsDebugDisabled,
                        onUnlockAdvanced = if (adsEnabled) {
                            { showRewardDialog = true }
                        } else {
                            null
                        },
                        rewardedRemainingMillis = (settings.rewardedAccessUntil - System.currentTimeMillis())
                            .coerceAtLeast(0L),
                    )
                }
                composable("search") {
                    SearchScreen(
                        favorites = favorites,
                        onOpenTool = ::openTool,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(
                    route = "category/{category}",
                    arguments = listOf(navArgument("category") { type = NavType.StringType }),
                ) { entry ->
                    val category = entry.arguments?.getString("category")
                        ?.let { runCatching { ToolCategory.valueOf(it) }.getOrNull() }
                    if (category == null) {
                        ToolPage(title = stringResource(R.string.categories_title), onBack = {
                            navController.popBackStack()
                        }) {
                            ErrorPane(
                                title = stringResource(R.string.error_generic),
                                body = stringResource(R.string.tool_not_found_body),
                            )
                        }
                    } else {
                        CategoryDetailScreen(
                            category = category,
                            favorites = favorites,
                            onOpenTool = ::openTool,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(
                    route = "tool/{toolId}",
                    arguments = listOf(navArgument("toolId") { type = NavType.StringType }),
                ) { entry ->
                    val toolId = entry.arguments?.getString("toolId").orEmpty()
                    ToolHost(
                        toolId = toolId,
                        viewModel = viewModel,
                        notes = notes,
                        tasks = tasks,
                        settings = settings,
                        adsManager = adsManager,
                        adsEnabled = adsEnabled,
                        onRewarded = viewModel::grantRewardedAccess,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable("about") {
                    AboutScreen(onBack = { navController.popBackStack() })
                }
                composable("privacy") {
                    PrivacyPolicyScreen(onBack = { navController.popBackStack() })
                }
                composable("licenses") {
                    LicensesScreen(onBack = { navController.popBackStack() })
                }
            }

            AnimatedVisibility(
                visible = isMain,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    animationSpec = tween(Motion.Medium),
                    initialOffsetY = { it },
                ) + fadeIn(tween(Motion.Fast)),
                exit = slideOutVertically(
                    animationSpec = tween(Motion.Fast),
                    targetOffsetY = { it },
                ) + fadeOut(tween(Motion.Fast)),
            ) {
                DamdastBottomBar(
                    currentRoute = currentRoute,
                    onSelect = ::openMain,
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (isMain) 86.dp else 12.dp,
                    ),
            )
            if (showRewardDialog) {
                RewardedUnlockDialog(
                    adsManager = adsManager,
                    onDismiss = { showRewardDialog = false },
                    onRewarded = {
                        viewModel.grantRewardedAccess()
                        message(context.getString(R.string.rewarded_success))
                    },
                )
            }
        }
    }
}

@Composable
private fun ToolHost(
    toolId: String,
    viewModel: DamdastViewModel,
    notes: List<ir.amiraligrr.damdast.data.NoteItem>,
    tasks: List<ir.amiraligrr.damdast.data.TaskItem>,
    settings: AppSettings,
    adsManager: ir.amiraligrr.damdast.ads.AdsManager,
    adsEnabled: Boolean,
    onRewarded: () -> Unit,
    onBack: () -> Unit,
) {
    val tool = ToolCatalog.byId[toolId]
    if (tool == null) {
        ToolPage(title = stringResource(R.string.app_name), onBack = onBack) {
            ErrorPane(
                title = stringResource(R.string.tool_not_found_title),
                body = stringResource(R.string.tool_not_found_body),
            )
        }
        return
    }
    if (tool.premium && settings.rewardedAccessUntil <= System.currentTimeMillis()) {
        PremiumToolGate(
            tool = tool,
            adsManager = adsManager,
            adsEnabled = adsEnabled,
            onRewarded = onRewarded,
            onBack = onBack,
        )
        return
    }
    LaunchedEffect(tool.id) {
        viewModel.recordToolUse(tool.id)
    }
    val textToolIds = remember {
        setOf(
            "text_counter", "case_converter", "remove_duplicates", "sort_lines",
            "reverse_text", "whitespace", "base64", "url_encoder", "html_entities",
            "text_compare", "text_extract", "lorem_ipsum",
        )
    }
    val calculatorToolIds = remember {
        setOf("calculator", "percentage", "discount", "profit", "unit_converter", "number_base")
    }
    val fileToolIds = remember {
        setOf(
            "txt_viewer", "csv_viewer", "json_viewer", "xml_viewer",
            "markdown_viewer", "file_info", "zip_create", "zip_extract",
        )
    }
    val pdfToolIds = remember {
        setOf(
            "pdf_viewer", "pdf_merge", "pdf_extract", "pdf_rotate", "pdf_watermark",
            "pdf_numbers", "images_to_pdf", "pdf_to_images",
        )
    }
    val imageToolIds = remember {
        setOf(
            "image_viewer", "image_crop", "image_resize", "image_transform", "image_adjust",
            "image_filters", "image_text", "image_compress", "image_convert", "image_exif",
            "image_collage",
        )
    }
    val qrToolIds = remember {
        setOf(
            "qr_scanner", "scan_gallery", "qr_generator", "wifi_qr", "contact_qr",
            "barcode_scanner", "barcode_generator", "scan_history",
        )
    }
    val everydayToolIds = remember {
        setOf(
            "password_generator", "random_number", "random_selector", "dice",
            "coin_flip", "notes", "tasks", "shortcuts",
        )
    }
    val colorToolIds = remember {
        setOf("color_picker", "gradient_generator", "palette_generator", "image_colors", "font_preview")
    }
    val dateToolIds = remember {
        setOf("stopwatch", "timer", "countdown", "world_clock", "date_calculator", "jalali_converter")
    }
    val developerToolIds = remember {
        setOf(
            "json_formatter", "json_validator", "json_minifier", "xml_formatter",
            "html_formatter", "css_formatter", "js_formatter", "uuid_generator",
            "hash_calculator", "timestamp_converter", "regex_tester", "url_parser",
            "jwt_decoder", "unix_permissions", "dev_color_converter",
        )
    }
    when (tool.id) {
        in textToolIds -> TextToolsScreen(tool = tool, onBack = onBack)
        in calculatorToolIds -> CalculatorToolsScreen(tool = tool, onBack = onBack)
        in developerToolIds -> DeveloperToolsScreen(tool = tool, onBack = onBack)
        in dateToolIds -> DateToolsScreen(tool = tool, onBack = onBack)
        in colorToolIds -> ColorToolsScreen(tool = tool, onBack = onBack)
        in qrToolIds -> QrToolsScreen(tool = tool, viewModel = viewModel, onBack = onBack)
        in imageToolIds -> ImageToolsScreen(tool = tool, onBack = onBack)
        in pdfToolIds -> PdfToolsScreen(tool = tool, viewModel = viewModel, onBack = onBack)
        in fileToolIds -> FileToolsScreen(tool = tool, onBack = onBack)
        in everydayToolIds -> EverydayToolsScreen(
            tool = tool,
            viewModel = viewModel,
            notes = notes,
            tasks = tasks,
            onBack = onBack,
        )
        else -> ToolPage(title = stringResource(tool.nameRes), onBack = onBack) {
            ErrorPane(
                title = stringResource(R.string.tool_not_found_title),
                body = stringResource(R.string.tool_not_found_body),
            )
        }
    }
}
