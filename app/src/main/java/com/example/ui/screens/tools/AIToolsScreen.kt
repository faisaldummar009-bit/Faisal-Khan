package com.example.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Content
import com.example.data.model.Part
import com.example.data.repository.GeminiRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.components.MarkdownView
import com.example.ui.theme.MintContainerDark
import com.example.ui.theme.MintOnContainerDark
import com.example.ui.theme.MintPrimary
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class ToolMode(val title: String, val icon: ImageVector) {
    CODING("Coding", Icons.Default.Code),
    WRITING("Writing", Icons.Default.Edit),
    TRANSLATE("Translate", Icons.Default.Language),
    LEARNING("Learning", Icons.Default.School)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIToolsScreen(
    prefsRepo: UserPreferencesRepository,
    onOpenDrawer: () -> Unit,
    onStartChatWithPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember { mutableStateOf(ToolMode.CODING) }
    var inputQuery by remember { mutableStateOf("") }
    var secondaryInput by remember { mutableStateOf("") }
    var targetLanguage by remember { mutableStateOf("Spanish") }
    var isRunning by remember { mutableStateOf(false) }
    var outputResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val geminiRepo = remember { GeminiRepository() }

    val languages = listOf("Spanish", "French", "German", "Japanese", "Chinese", "Arabic", "Portuguese", "Italian", "Korean")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Specialized Tools",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("open_drawer_tools_btn")) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row for Specialized Tools
            TabRow(
                selectedTabIndex = selectedTool.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MintPrimary
            ) {
                ToolMode.values().forEach { mode ->
                    Tab(
                        selected = selectedTool == mode,
                        onClick = {
                            selectedTool = mode
                            outputResult = null
                            errorMessage = null
                        },
                        text = { Text(mode.title, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(mode.icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Tool description card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MintContainerDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MintPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(selectedTool.icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = when (selectedTool) {
                                        ToolMode.CODING -> "AI Coding & Engineering Assistant"
                                        ToolMode.WRITING -> "Executive Writing & Summarization"
                                        ToolMode.TRANSLATE -> "Universal Language Translation Studio"
                                        ToolMode.LEARNING -> "Socratic Learning & First Principles"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MintOnContainerDark
                                )
                                Text(
                                    text = when (selectedTool) {
                                        ToolMode.CODING -> "Syntax fixes, performance optimizations, and test generation"
                                        ToolMode.WRITING -> "Polishes emails, expands outlines, and synthesizes key takeaways"
                                        ToolMode.TRANSLATE -> "Context-aware translation with nuance and pronunciation guides"
                                        ToolMode.LEARNING -> "Demystifies complex science, mathematics, and philosophy"
                                    },
                                    fontSize = 11.sp,
                                    color = MintOnContainerDark.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                if (selectedTool == ToolMode.TRANSLATE) {
                    item {
                        Text(
                            text = "TARGET LANGUAGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            languages.take(4).forEach { lang ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (targetLanguage == lang) MintPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { targetLanguage = lang }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = lang,
                                        fontSize = 12.sp,
                                        fontWeight = if (targetLanguage == lang) FontWeight.Bold else FontWeight.Normal,
                                        color = if (targetLanguage == lang) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = when (selectedTool) {
                            ToolMode.CODING -> "PASTE CODE OR ERROR"
                            ToolMode.WRITING -> "INPUT TEXT OR DRAFT"
                            ToolMode.TRANSLATE -> "ORIGINAL TEXT"
                            ToolMode.LEARNING -> "TOPIC OR QUESTION"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = {
                            Text(
                                text = when (selectedTool) {
                                    ToolMode.CODING -> "Paste code snippet or stack trace here..."
                                    ToolMode.WRITING -> "Paste draft email, article, or notes to polish..."
                                    ToolMode.TRANSLATE -> "Enter text to translate..."
                                    ToolMode.LEARNING -> "What concept would you like explained? (e.g. Transformers in ML)"
                                },
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("tool_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (inputQuery.isBlank()) return@Button
                                isRunning = true
                                errorMessage = null
                                outputResult = null

                                scope.launch {
                                    val customKey = prefsRepo.customApiKey.firstOrNull()
                                    val effectiveKey = prefsRepo.getEffectiveApiKey(customKey)

                                    val systemPrompt = when (selectedTool) {
                                        ToolMode.CODING -> "You are an elite principal software engineer and coding assistant. Review the provided code or error, explain root issues, and provide optimized idiomatic code with clean markdown formatting."
                                        ToolMode.WRITING -> "You are a world-class executive editor and communications strategist. Polish, clarify, and elevate the provided draft, noting any improvements made."
                                        ToolMode.TRANSLATE -> "You are a professional multilingual translator. Translate accurately to $targetLanguage, providing pronunciation notes and subtle cultural nuances where applicable."
                                        ToolMode.LEARNING -> "You are a master educator using the Feynman technique. Explain the subject with intuitive analogies, first principles, and zero unnecessary jargon."
                                    }

                                    val contentList = listOf(
                                        Content(
                                            role = "user",
                                            parts = listOf(Part(text = inputQuery))
                                        )
                                    )

                                    val result = geminiRepo.generateChatResponse(
                                        apiKey = effectiveKey,
                                        modelId = "gemini-3.5-flash",
                                        messages = contentList,
                                        systemInstruction = systemPrompt
                                    )

                                    isRunning = false
                                    result.onSuccess { text ->
                                        outputResult = text
                                        prefsRepo.incrementUsage()
                                    }.onFailure { err ->
                                        errorMessage = err.message ?: "Failed to generate tool output"
                                    }
                                }
                            },
                            enabled = inputQuery.isNotBlank() && !isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("run_tool_btn")
                        ) {
                            if (isRunning) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyzing...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Run Analysis", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (inputQuery.isNotBlank()) {
                                    onStartChatWithPrompt(inputQuery)
                                }
                            },
                            enabled = inputQuery.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Open in Chat")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                if (errorMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                if (outputResult != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "AI Result",
                                        fontWeight = FontWeight.Bold,
                                        color = MintPrimary,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                MarkdownView(content = outputResult!!)
                            }
                        }
                    }
                }
            }
        }
    }
}
