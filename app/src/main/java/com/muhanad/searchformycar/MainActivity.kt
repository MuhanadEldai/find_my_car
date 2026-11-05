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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.muhanad.searchformycar.data.CarRepository
import com.muhanad.searchformycar.ui.screens.*
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
            var currentScreen by remember { mutableStateOf("main") } // "main" or "settings"

            val activity = context as? ComponentActivity

            // Official Android 15/16 Edge-to-Edge SystemBarStyle updates
            DisposableEffect(isDarkMode) {
                activity?.enableEdgeToEdge(
                    statusBarStyle = if (isDarkMode) {
                        SystemBarStyle.dark("#1E293B".toColorInt())
                    } else {
                        SystemBarStyle.light(
                            "#FF94612F".toColorInt(),
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
                    currentScreen == "settings" -> currentScreen = "main"
                    currentTab != 0 -> currentTab = 0
                    else -> finish()
                }
            }

            SearchformycarTheme(darkTheme = isDarkMode) {
                if (currentScreen == "settings") {
                    SettingsScreen(
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
                        onBack = { currentScreen = "main" }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = when (currentTab) {
                                                0 -> "فتش لي عربيتي"
                                                1 -> "إضافة بلاغ سيارة جديدة"
                                                2 -> "حول التطبيق"
                                                else -> "فتش لي عربيتي"
                                            },
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            color = Color.White
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { currentScreen = "settings" },
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "الإعدادات",
                                            tint = Color.White // Settings icon set to white
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.scrim
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == 0,
                                    onClick = { currentTab = 0 },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                                    label = { Text("الرئيسية") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    icon = { Icon(Icons.Default.Add, contentDescription = "إضافة بلاغ") },
                                    label = { Text("إضافة بلاغ") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == 2,
                                    onClick = { currentTab = 2 },
                                    icon = { Icon(Icons.Default.Info, contentDescription = "حول التطبيق") },
                                    label = { Text("حول التطبيق") }
                                )
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
                                    onSelectTab = { currentTab = it }
                                )
                                1 -> AddReportScreen(
                                )
                                2 -> AboutScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
