package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ScreenDestination
import com.example.data.model.ThemeMode
import com.example.data.model.UserTier
import com.example.ui.components.AppNavigationDrawer
import com.example.ui.components.QuickPromptLibraryDialog
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.imageedit.ImageEditScreen
import com.example.ui.screens.imageedit.ImageEditViewModel
import com.example.ui.screens.imagegen.ImageGenScreen
import com.example.ui.screens.imagegen.ImageGenViewModel
import com.example.ui.screens.settings.ProfileDialog
import com.example.ui.screens.settings.SettingsDialog
import com.example.ui.screens.tools.AIToolsScreen
import com.example.ui.theme.LightGPTTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val chatViewModel: ChatViewModel = viewModel()
            val imageGenViewModel: ImageGenViewModel = viewModel()
            val imageEditViewModel: ImageEditViewModel = viewModel()

            val prefsRepo = chatViewModel.prefsRepo

            val themeMode by prefsRepo.themeMode.collectAsState(initial = ThemeMode.DARK)
            val selectedModel by prefsRepo.selectedModel.collectAsState(initial = com.example.data.model.AiModel.FLASH_3_5)
            val userTier by prefsRepo.userTier.collectAsState(initial = UserTier.FREE)
            val dailyUsageCount by prefsRepo.dailyUsageCount.collectAsState(initial = 0)
            val customApiKey by prefsRepo.customApiKey.collectAsState(initial = null)
            val userName by prefsRepo.userName.collectAsState(initial = "Explorer")
            val userEmail by prefsRepo.userEmail.collectAsState(initial = "faisaldummar009@gmail.com")

            val sessions by chatViewModel.sessions.collectAsState()
            val chatUiState by chatViewModel.uiState.collectAsState()

            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            LightGPTTheme(darkTheme = isDark) {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var currentScreen by remember { mutableStateOf(ScreenDestination.CHAT) }
                var showSettings by remember { mutableStateOf(false) }
                var showProfile by remember { mutableStateOf(false) }
                var showPromptLibraryGlobal by remember { mutableStateOf(false) }

                // Predictive Back Handling
                BackHandler(enabled = drawerState.isOpen || currentScreen != ScreenDestination.CHAT) {
                    if (drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    } else if (currentScreen != ScreenDestination.CHAT) {
                        currentScreen = ScreenDestination.CHAT
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppNavigationDrawer(
                            currentScreen = currentScreen,
                            currentSessionId = chatUiState.currentSessionId,
                            sessions = sessions,
                            userTier = userTier,
                            dailyUsageCount = dailyUsageCount,
                            onNavigate = { dest ->
                                currentScreen = dest
                                scope.launch { drawerState.close() }
                            },
                            onSelectSession = { id ->
                                chatViewModel.selectSession(id)
                                currentScreen = ScreenDestination.CHAT
                                scope.launch { drawerState.close() }
                            },
                            onNewChat = {
                                chatViewModel.createNewChat()
                                currentScreen = ScreenDestination.CHAT
                                scope.launch { drawerState.close() }
                            },
                            onRenameSession = { id, title ->
                                chatViewModel.renameSession(id, title)
                            },
                            onDeleteSession = { id ->
                                chatViewModel.deleteSession(id)
                            },
                            onOpenPromptLibrary = {
                                showPromptLibraryGlobal = true
                                scope.launch { drawerState.close() }
                            },
                            onOpenProfile = {
                                showProfile = true
                                scope.launch { drawerState.close() }
                            },
                            onOpenSettings = {
                                showSettings = true
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            ScreenDestination.CHAT -> {
                                ChatScreen(
                                    viewModel = chatViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                            ScreenDestination.IMAGE_GEN -> {
                                ImageGenScreen(
                                    viewModel = imageGenViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                            ScreenDestination.IMAGE_EDIT -> {
                                ImageEditScreen(
                                    viewModel = imageEditViewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                            ScreenDestination.AI_TOOLS -> {
                                AIToolsScreen(
                                    prefsRepo = prefsRepo,
                                    onOpenDrawer = { scope.launch { drawerState.open() } },
                                    onStartChatWithPrompt = { promptText ->
                                        currentScreen = ScreenDestination.CHAT
                                        chatViewModel.createNewChat(initialTitle = "AI Tool Session")
                                        chatViewModel.sendMessage(promptText)
                                    }
                                )
                            }
                        }
                    }
                }

                // Global Settings Dialog
                if (showSettings) {
                    SettingsDialog(
                        themeMode = themeMode,
                        selectedModel = selectedModel,
                        customApiKey = customApiKey,
                        onThemeChange = { mode -> scope.launch { prefsRepo.setThemeMode(mode) } },
                        onModelChange = { model -> scope.launch { prefsRepo.setSelectedModel(model) } },
                        onSaveApiKey = { key -> scope.launch { prefsRepo.setCustomApiKey(key) } },
                        onClearAllData = {
                            chatViewModel.clearAllHistory()
                            scope.launch { imageGenViewModel.savedGallery.value.forEach { imageGenViewModel.deleteImage(it.id) } }
                        },
                        onDismiss = { showSettings = false }
                    )
                }

                // Global Profile Dialog
                if (showProfile) {
                    ProfileDialog(
                        userName = userName,
                        userEmail = userEmail,
                        tier = userTier,
                        dailyUsageCount = dailyUsageCount,
                        onTierChange = { tier -> scope.launch { prefsRepo.setUserTier(tier) } },
                        onDismiss = { showProfile = false }
                    )
                }

                // Global Quick Prompt Dialog
                if (showPromptLibraryGlobal) {
                    QuickPromptLibraryDialog(
                        onDismiss = { showPromptLibraryGlobal = false },
                        onSelectPrompt = { chosenPrompt ->
                            currentScreen = ScreenDestination.CHAT
                            chatViewModel.sendMessage(chosenPrompt)
                            showPromptLibraryGlobal = false
                        }
                    )
                }
            }
        }
    }
}
