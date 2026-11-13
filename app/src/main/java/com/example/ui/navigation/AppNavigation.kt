package com.example.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.executor.CodeExecutionEngine
import com.example.data.model.EditorSettings
import com.example.data.model.Project
import com.example.data.model.ProjectFile
import com.example.data.repository.AiRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.ProjectRepository
import com.example.ui.components.AiProjectBuilderDialog
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.StudioBackgroundDark
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch

enum class AppScreen(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    PROJECTS("Projects", Icons.Default.Folder),
    EDITOR("Editor", Icons.Default.Code),
    AI("AI Assistant", Icons.Default.AutoAwesome),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    projectRepository: ProjectRepository,
    aiRepository: AiRepository,
    executionEngine: CodeExecutionEngine
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by authRepository.observeAuthState().collectAsState(initial = authRepository.activeUser.value)
    var isOfflineMode by remember { mutableStateOf(false) }

    val projects by projectRepository.observeProjects().collectAsState(initial = emptyList())
    var activeProject by remember { mutableStateOf<Project?>(null) }
    val activeFiles by projectRepository.observeProjectFiles(activeProject?.id ?: "").collectAsState(initial = emptyList())

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var editorSettings by remember { mutableStateOf(EditorSettings()) }

    var isAiBuilderOpen by remember { mutableStateOf(false) }
    var isAiBuilderLoading by remember { mutableStateOf(false) }
    var aiBuilderError by remember { mutableStateOf<String?>(null) }
    var aiReferencedFile by remember { mutableStateOf<ProjectFile?>(null) }

    // Synchronize active project
    LaunchedEffect(projects) {
        if (activeProject == null && projects.isNotEmpty()) {
            activeProject = projects.first()
        }
    }

    // Try silent sign-in on launch
    LaunchedEffect(Unit) {
        authRepository.trySilentSignIn()
    }

    // Back button behavior
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        currentScreen = AppScreen.HOME
    }

    // Auth gate if not in offline mode and no user
    if (currentUser == null && !isOfflineMode) {
        AuthScreen(
            authRepository = authRepository,
            onAuthSuccess = {
                Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
            },
            onContinueOffline = {
                isOfflineMode = true
            }
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Mobile 5-Tab Navigation Bar (hidden in fullscreen terminal)
            NavigationBar(
                containerColor = StudioSurfaceDark,
                contentColor = StudioCyan,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                AppScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StudioCyan,
                            selectedTextColor = StudioCyan,
                            indicatorColor = StudioCyan.copy(alpha = 0.18f),
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name}")
                    )
                }
            }
        },
        containerColor = StudioBackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        currentUser = currentUser,
                        projects = projects,
                        onSelectProject = { proj ->
                            activeProject = proj
                            currentScreen = AppScreen.EDITOR
                        },
                        onCreateNewProject = { template ->
                            coroutineScope.launch {
                                val created = projectRepository.createProject(
                                    name = "New ${template.uppercase()} App",
                                    template = template
                                )
                                activeProject = created
                                currentScreen = AppScreen.EDITOR
                            }
                        },
                        onNavigateToAi = { currentScreen = AppScreen.AI },
                        onOpenAiBuilder = { isAiBuilderOpen = true },
                        onNavigateToProjects = { currentScreen = AppScreen.PROJECTS }
                    )
                }
                AppScreen.PROJECTS -> {
                    ProjectsScreen(
                        projects = projects,
                        onSelectProject = { proj ->
                            activeProject = proj
                            currentScreen = AppScreen.EDITOR
                        },
                        onCreateProject = { name, template ->
                            coroutineScope.launch {
                                val created = projectRepository.createProject(name = name, template = template)
                                activeProject = created
                                currentScreen = AppScreen.EDITOR
                            }
                        },
                        onDeleteProject = { proj ->
                            coroutineScope.launch {
                                projectRepository.deleteProject(proj.id)
                                if (activeProject?.id == proj.id) {
                                    activeProject = projects.firstOrNull { it.id != proj.id }
                                }
                            }
                        },
                        onDuplicateProject = { proj ->
                            coroutineScope.launch {
                                projectRepository.duplicateProject(proj.id)
                                Toast.makeText(context, "Project duplicated", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onExportProject = { proj ->
                            val zip = projectRepository.exportProjectZip(proj.id)
                            Toast.makeText(context, "ZIP exported: ${zip.size} bytes", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                AppScreen.EDITOR -> {
                    val currentProj = activeProject ?: projects.firstOrNull()
                    if (currentProj != null) {
                        EditorScreen(
                            project = currentProj,
                            files = activeFiles,
                            executionEngine = executionEngine,
                            onBack = { currentScreen = AppScreen.HOME },
                            onSaveFile = { fileId, content ->
                                coroutineScope.launch {
                                    projectRepository.saveFileContent(currentProj.id, fileId, content)
                                }
                            },
                            onCreateFile = { name, isFolder ->
                                coroutineScope.launch {
                                    projectRepository.createFile(currentProj.id, name, "", isFolder)
                                }
                            },
                            onDeleteFile = { file ->
                                coroutineScope.launch {
                                    projectRepository.deleteFile(currentProj.id, file.id)
                                }
                            },
                            onDuplicateFile = { file ->
                                coroutineScope.launch {
                                    projectRepository.createFile(
                                        currentProj.id,
                                        "copy_${file.name}",
                                        file.content,
                                        file.isFolder
                                    )
                                }
                            },
                            onOpenAiWithFile = { file ->
                                aiReferencedFile = file
                                currentScreen = AppScreen.AI
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(StudioBackgroundDark),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(text = "No project selected. Go to Projects tab.", color = TextSecondaryDark)
                        }
                    }
                }
                AppScreen.AI -> {
                    AiAssistantScreen(
                        aiRepository = aiRepository,
                        currentFileContext = aiReferencedFile,
                        onApplySnippetToProject = { snippet ->
                            coroutineScope.launch {
                                val currentProj = activeProject ?: projects.firstOrNull()
                                if (currentProj != null) {
                                    val existingFile = activeFiles.find { it.name == snippet.fileName }
                                    if (existingFile != null) {
                                        projectRepository.saveFileContent(currentProj.id, existingFile.id, snippet.code)
                                    } else {
                                        projectRepository.createFile(currentProj.id, snippet.fileName, snippet.code)
                                    }
                                    Toast.makeText(context, "Applied ${snippet.fileName} to project!", Toast.LENGTH_SHORT).show()
                                    currentScreen = AppScreen.EDITOR
                                }
                            }
                        }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        currentUser = currentUser,
                        editorSettings = editorSettings,
                        onUpdateSettings = { editorSettings = it },
                        onSignOut = {
                            coroutineScope.launch {
                                authRepository.signOut()
                                isOfflineMode = false
                            }
                        }
                    )
                }
            }
        }
    }

    // AI Project Builder Dialog
    if (isAiBuilderOpen) {
        AiProjectBuilderDialog(
            isLoading = isAiBuilderLoading,
            errorMessage = aiBuilderError,
            onBuildProject = { prompt ->
                isAiBuilderLoading = true
                aiBuilderError = null
                coroutineScope.launch {
                    val result = aiRepository.generateProject(prompt)
                    isAiBuilderLoading = false
                    if (result.isSuccess) {
                        val genResult = result.getOrThrow()
                        val newProj = projectRepository.createProject(
                            name = genResult.projectName,
                            description = genResult.description,
                            template = genResult.template
                        )
                        for (gf in genResult.files) {
                            val existing = projectRepository.getProjectFiles(newProj.id).find { it.name == gf.name }
                            if (existing != null) {
                                projectRepository.saveFileContent(newProj.id, existing.id, gf.content)
                            } else {
                                projectRepository.createFile(newProj.id, gf.name, gf.content)
                            }
                        }
                        activeProject = newProj
                        isAiBuilderOpen = false
                        currentScreen = AppScreen.EDITOR
                        Toast.makeText(context, "Project '${genResult.projectName}' created!", Toast.LENGTH_SHORT).show()
                    } else {
                        aiBuilderError = result.exceptionOrNull()?.localizedMessage ?: "Failed to generate project"
                    }
                }
            },
            onDismiss = { isAiBuilderOpen = false }
        )
    }
}
