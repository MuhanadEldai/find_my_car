package com.muhanad.searchformycar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.muhanad.searchformycar.data.CarRepository
import com.muhanad.searchformycar.ui.screens.*
import com.muhanad.searchformycar.ui.theme.AppStrings
import com.muhanad.searchformycar.ui.theme.SearchformycarTheme

class MainActivity : ComponentActivity() {
    private val repository = CarRepository()

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("app_prefs", MODE_PRIVATE) }
            var isDarkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
            var language by remember { mutableStateOf(prefs.getString("language", "ar") ?: "ar") }
            var currentTab by remember { mutableIntStateOf(0) }
            var isAboutOpen by remember { mutableStateOf(false) }

            val isAr = language == "ar"
            val activity = context as? ComponentActivity

            LaunchedEffect(context) {
                repository.setContext(context)
            }

            // Official Android 15/16 Edge-to-Edge SystemBarStyle updates
            DisposableEffect(isDarkMode) {
                activity?.enableEdgeToEdge(
                    statusBarStyle = if (isDarkMode) {
                        SystemBarStyle.dark("#1E293B".toColorInt())
                    } else {
                        SystemBarStyle.light(
                            "#FFA97733".toColorInt(),
                            "#FF94612F".toColorInt()
                        )
                    },
                    navigationBarStyle = if (isDarkMode) {
                        SystemBarStyle.dark("#1E293B".toColorInt())
                    } else {
                        SystemBarStyle.light(
                            "#FFFFFF".toColorInt(),
                            "#FFFFFF".toColorInt()
                        )
                    }
                )
                onDispose {}
            }

            // Handle back button press gracefully
            BackHandler(enabled = true) {
                when {
                    isAboutOpen -> isAboutOpen = false
                    currentTab != 0 -> currentTab = 0
                    else -> finish()
                }
            }

            CompositionLocalProvider(
                LocalLayoutDirection provides if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                SearchformycarTheme(darkTheme = isDarkMode) {
                    if (isAboutOpen) {
                        AboutScreen(
                            language = language,
                            onBack = { isAboutOpen = false }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = MaterialTheme.colorScheme.background,
                            topBar = {
                                Surface(
                                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                                    color = MaterialTheme.colorScheme.scrim,
                                    shadowElevation = 8.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TopAppBar(
                                        title = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Start,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            ) {
                                                Text(
                                                    text = when (currentTab) {
                                                        0 -> AppStrings.get("app_name", language)
                                                        1 -> AppStrings.get("add_report_title", language)
                                                        2 -> AppStrings.get("settings", language)
                                                        else -> AppStrings.get("app_name", language)
                                                    },
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 22.sp,
                                                    color = Color.White
                                                )
                                            }
                                        },

                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = Color.Transparent
                                        )
                                    )
                                }
                            },
                            bottomBar = {
                                Surface(
                                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 8.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val navItemColors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    )
                                    NavigationBar(
                                        containerColor = Color.Transparent
                                    ) {
                                        NavigationBarItem(
                                            selected = currentTab == 0,
                                            onClick = { currentTab = 0 },
                                            icon = { Icon(Icons.Default.Home, contentDescription = AppStrings.get("home", language)) },
                                            label = { Text(AppStrings.get("home", language)) },
                                            colors = navItemColors
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == 1,
                                            onClick = { currentTab = 1 },
                                            icon = { Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_report", language)) },
                                            label = { Text(AppStrings.get("add_report", language)) },
                                            colors = navItemColors
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == 2,
                                            onClick = { currentTab = 2 },
                                            icon = { Icon(Icons.Default.Settings, contentDescription = AppStrings.get("settings", language)) },
                                            label = { Text(AppStrings.get("settings", language)) },
                                            colors = navItemColors
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.94f, animationSpec = tween(350)))
                                        .togetherWith(fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.98f, animationSpec = tween(250)))
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                label = "tab_transition"
                            ) { tab ->
                                when (tab) {
                                    0 -> HomeScreen(
                                        repository = repository,
                                        isDarkMode = isDarkMode,
                                        language = language,
                                        onSelectTab = { currentTab = it }
                                    )
                                    1 -> AddReportScreen(
                                        language = language
                                    )
                                    2 -> SettingsScreen(
                                        isDarkMode = isDarkMode,
                                        onDarkModeChanged = {
                                            isDarkMode = it
                                            prefs.edit { putBoolean("dark_mode", it) }
                                        },
                                        language = language,
                                        onLanguageChanged = {
                                            language = it
                                            prefs.edit { putString("language", it) }
                                        },
                                        onOpenAbout = { isAboutOpen = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
