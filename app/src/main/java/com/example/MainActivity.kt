package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.executor.CodeExecutionEngine
import com.example.data.repository.AiRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.ProjectRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.NatnaelCodeTheme
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val authRepository = AuthRepository(this)
        val projectRepository = ProjectRepository(this)
        val aiRepository = AiRepository()
        val executionEngine = CodeExecutionEngine()

        setContent {
            NatnaelCodeTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        authRepository = authRepository,
                        projectRepository = projectRepository,
                        aiRepository = aiRepository,
                        executionEngine = executionEngine
                    )
                }
            }
        }
    }
}
