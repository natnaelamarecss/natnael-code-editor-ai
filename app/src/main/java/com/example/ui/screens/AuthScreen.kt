package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthRepository
import com.example.ui.theme.StudioBackgroundDark
import com.example.ui.theme.StudioBorderDark
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioRose
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onAuthSuccess: () -> Unit,
    onContinueOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Email & Password, 1 = Google Sign-In
    var isCreateAccountMode by remember { mutableStateOf(true) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("auth_screen"),
        color = StudioBackgroundDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // App Brand Emblem
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(StudioCyan, StudioPurple)
                        )
                    )
                    .border(2.dp, StudioCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "</>",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Natnael Code",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimaryDark
            )

            Text(
                text = "Code Anywhere. Build Anything. Powered by AI.",
                fontSize = 12.sp,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
            )

            // Auth Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Method Segmented Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = StudioCardDark,
                        contentColor = StudioCyan,
                        indicator = {},
                        divider = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 0) StudioCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Email Account",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) StudioCyan else TextSecondaryDark
                            )
                        }

                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 1) StudioCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "Google Sign-In",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) StudioCyan else TextSecondaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedTab == 0) {
                        // Email & Password Mode (Create Account vs Sign In)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCreateAccountMode) "Create New Account" else "Welcome Back",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full Name (Only on Create Account)
                        if (isCreateAccountMode) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name", fontSize = 12.sp) },
                                placeholder = { Text("e.g. Natnael Amare") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StudioCyan,
                                    unfocusedBorderColor = StudioBorderDark,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address", fontSize = 12.sp) },
                            placeholder = { Text("developer@example.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioCyan,
                                unfocusedBorderColor = StudioBorderDark,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", fontSize = 12.sp) },
                            placeholder = { Text("Minimum 6 characters") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (isCreateAccountMode) ImeAction.Next else ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioCyan,
                                unfocusedBorderColor = StudioBorderDark,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        // Confirm Password Field (Only on Create Account)
                        if (isCreateAccountMode) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password", fontSize = 12.sp) },
                                placeholder = { Text("Re-enter password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StudioCyan,
                                    unfocusedBorderColor = StudioBorderDark,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_confirm_password_input")
                            )
                        }

                        // Error or Success message
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = errorMessage!!,
                                color = StudioRose,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (successMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = successMessage!!,
                                color = StudioGreen,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                errorMessage = null
                                successMessage = null

                                if (isCreateAccountMode) {
                                    if (password != confirmPassword) {
                                        errorMessage = "Passwords do not match."
                                        return@Button
                                    }
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = authRepository.createEmailAccount(fullName, email, password)
                                        isLoading = false
                                        if (result.isSuccess) {
                                            successMessage = "Account created successfully!"
                                            onAuthSuccess()
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to create account"
                                        }
                                    }
                                } else {
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = authRepository.signInWithEmailAccount(email, password)
                                        isLoading = false
                                        if (result.isSuccess) {
                                            successMessage = "Signed in successfully!"
                                            onAuthSuccess()
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Sign-in failed"
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("email_auth_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = com.example.ui.theme.StudioBackgroundDark,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isCreateAccountMode) "Create Account" else "Sign In",
                                    color = com.example.ui.theme.StudioBackgroundDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Switch between Create and Sign In
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isCreateAccountMode) "Already have an account?" else "Need an account?",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                            TextButton(
                                onClick = {
                                    isCreateAccountMode = !isCreateAccountMode
                                    errorMessage = null
                                }
                            ) {
                                Text(
                                    text = if (isCreateAccountMode) "Sign In" else "Create Account",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioCyan
                                )
                            }
                        }
                    } else {
                        // Google Sign-In Tab
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sign in with your Google Account for real-time cloud synchronization of your projects and settings across devices.",
                                fontSize = 13.sp,
                                color = TextSecondaryDark,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = StudioRose,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    val activity = context as? Activity ?: return@Button
                                    isLoading = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        val result = authRepository.signInWithGoogle(activity)
                                        isLoading = false
                                        if (result.isSuccess) {
                                            onAuthSuccess()
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Google Sign-In cancelled"
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = StudioCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("google_sign_in_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = com.example.ui.theme.StudioBackgroundDark,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "G", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.StudioBackgroundDark)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Continue with Google",
                                            color = com.example.ui.theme.StudioBackgroundDark,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue Offline / Local Sandbox Button
            OutlinedButton(
                onClick = onContinueOffline,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("continue_offline_button")
            ) {
                Text(
                    text = "Continue as Guest (Local Sandbox)",
                    color = TextPrimaryDark,
                    fontSize = 13.sp
                )
            }
        }
    }
}
