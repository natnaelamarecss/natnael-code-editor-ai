package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

private const val TAG = "AuthRepository"
private const val PREFS_ACCOUNTS = "natnael_code_user_accounts"
private const val KEY_ACTIVE_USER = "active_local_user_json"

data class UserAccount(
    val uid: String,
    val displayName: String,
    val email: String,
    val isGoogleAccount: Boolean = false
)

class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)
    private val prefs = context.getSharedPreferences(PREFS_ACCOUNTS, Context.MODE_PRIVATE)

    private val _activeUser = MutableStateFlow<UserAccount?>(null)
    val activeUser = _activeUser.asStateFlow()

    init {
        // Restore existing local session or Firebase session
        val fbUser = auth.currentUser
        if (fbUser != null) {
            _activeUser.value = UserAccount(
                uid = fbUser.uid,
                displayName = fbUser.displayName ?: "Developer",
                email = fbUser.email ?: "",
                isGoogleAccount = true
            )
        } else {
            val localJson = prefs.getString(KEY_ACTIVE_USER, null)
            if (!localJson.isNullOrBlank()) {
                try {
                    val obj = JSONObject(localJson)
                    _activeUser.value = UserAccount(
                        uid = obj.getString("uid"),
                        displayName = obj.getString("displayName"),
                        email = obj.getString("email"),
                        isGoogleAccount = false
                    )
                } catch (e: Exception) {
                    prefs.edit().remove(KEY_ACTIVE_USER).apply()
                }
            }
        }
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun observeAuthState(): Flow<UserAccount?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { currentAuth ->
            val fbUser = currentAuth.currentUser
            if (fbUser != null) {
                val acc = UserAccount(
                    uid = fbUser.uid,
                    displayName = fbUser.displayName ?: "Developer",
                    email = fbUser.email ?: "",
                    isGoogleAccount = true
                )
                _activeUser.value = acc
                trySend(acc)
            } else {
                // If no Firebase user, check local session
                val currentLocal = _activeUser.value
                if (currentLocal != null && !currentLocal.isGoogleAccount) {
                    trySend(currentLocal)
                } else {
                    _activeUser.value = null
                    trySend(null)
                }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun createEmailAccount(
        name: String,
        email: String,
        password: String
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()

        if (cleanName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter your name."))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val accountKey = "account_$cleanEmail"
        if (prefs.contains(accountKey)) {
            return@withContext Result.failure(IllegalStateException("An account with this email already exists."))
        }

        val uid = "usr_" + UUID.randomUUID().toString().take(12)
        val passwordHash = hashPassword(password)

        val accountJson = JSONObject().apply {
            put("uid", uid)
            put("displayName", cleanName)
            put("email", cleanEmail)
            put("passwordHash", passwordHash)
            put("createdAt", System.currentTimeMillis())
        }.toString()

        prefs.edit()
            .putString(accountKey, accountJson)
            .putString(KEY_ACTIVE_USER, accountJson)
            .apply()

        val account = UserAccount(
            uid = uid,
            displayName = cleanName,
            email = cleanEmail,
            isGoogleAccount = false
        )
        _activeUser.value = account
        Result.success(account)
    }

    suspend fun signInWithEmailAccount(
        email: String,
        password: String
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val accountKey = "account_$cleanEmail"

        val accountJson = prefs.getString(accountKey, null)
            ?: return@withContext Result.failure(IllegalArgumentException("No account found with this email. Please create an account."))

        try {
            val obj = JSONObject(accountJson)
            val storedHash = obj.getString("passwordHash")
            val inputHash = hashPassword(password)

            if (storedHash != inputHash) {
                return@withContext Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
            }

            prefs.edit().putString(KEY_ACTIVE_USER, accountJson).apply()

            val account = UserAccount(
                uid = obj.getString("uid"),
                displayName = obj.getString("displayName"),
                email = obj.getString("email"),
                isGoogleAccount = false
            )
            _activeUser.value = account
            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> {
        return withContext(Dispatchers.Main) {
            try {
                val clientId = context.getString(R.string.default_web_client_id)
                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user ?: throw IllegalStateException("Signed in user is null")
                    prefs.edit().remove(KEY_ACTIVE_USER).apply()
                    _activeUser.value = UserAccount(
                        uid = user.uid,
                        displayName = user.displayName ?: "Developer",
                        email = user.email ?: "",
                        isGoogleAccount = true
                    )
                    Result.success(user)
                } else {
                    Result.failure(IllegalStateException("Unexpected credential type: ${credential.type}"))
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow was cancelled: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun trySilentSignIn(): Result<FirebaseUser?> {
        return withContext(Dispatchers.IO) {
            try {
                if (auth.currentUser != null) {
                    val fbUser = auth.currentUser!!
                    _activeUser.value = UserAccount(
                        uid = fbUser.uid,
                        displayName = fbUser.displayName ?: "Developer",
                        email = fbUser.email ?: "",
                        isGoogleAccount = true
                    )
                    return@withContext Result.success(fbUser)
                }
                val clientId = try {
                    context.getString(R.string.default_web_client_id)
                } catch (e: Exception) {
                    return@withContext Result.success(null)
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        prefs.edit().remove(KEY_ACTIVE_USER).apply()
                        _activeUser.value = UserAccount(
                            uid = user.uid,
                            displayName = user.displayName ?: "Developer",
                            email = user.email ?: "",
                            isGoogleAccount = true
                        )
                    }
                    Result.success(user)
                } else {
                    Result.success(null)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Silent sign-in not available: ${e.message}")
                Result.success(null)
            }
        }
    }

    suspend fun signOut() {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clear credential state: ${e.message}")
            } finally {
                auth.signOut()
                prefs.edit().remove(KEY_ACTIVE_USER).apply()
                _activeUser.value = null
            }
        }
    }
}
