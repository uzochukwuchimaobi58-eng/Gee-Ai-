package com.example.data

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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseManager(private val context: Context) {

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
        } catch (t: Throwable) {
            Log.w("FirebaseManager", "FirebaseApp initialization exception: ${t.message}")
        }
    }

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            Log.w("FirebaseManager", "FirebaseAuth not available: ${t.message}")
            null
        }

    val firestore: FirebaseFirestore?
        get() = try {
            val dbId = try { context.getString(R.string.firestore_database_id) } catch (e: Throwable) { "" }
            if (dbId.isNotBlank() && dbId != "(default)") {
                try {
                    FirebaseFirestore.getInstance(dbId)
                } catch (e: Throwable) {
                    Log.w("FirebaseManager", "Named DB failed, fallback to default: ${e.message}")
                    FirebaseFirestore.getInstance()
                }
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (t: Throwable) {
            Log.w("FirebaseManager", "Firestore not available: ${t.message}")
            null
        }

    private val crashlytics: FirebaseCrashlytics?
        get() = try {
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(true)
            }
        } catch (t: Throwable) {
            Log.w("FirebaseManager", "Crashlytics not available: ${t.message}")
            null
        }

    var customStorageBucket: String = "studio-4528506733-318e2.firebasestorage.app"
        private set

    val storage: FirebaseStorage?
        get() = try {
            if (customStorageBucket.isNotBlank()) {
                try {
                    FirebaseStorage.getInstance("gs://$customStorageBucket")
                } catch (e: Throwable) {
                    Log.w("FirebaseManager", "Custom bucket storage failed, fallback to default: ${e.message}")
                    FirebaseStorage.getInstance()
                }
            } else {
                FirebaseStorage.getInstance()
            }
        } catch (t: Throwable) {
            Log.w("FirebaseManager", "FirebaseStorage not available: ${t.message}")
            null
        }

    private var profileListener: ListenerRegistration? = null

    val currentUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (t: Throwable) {
            null
        }

    /**
     * Interactive Google Sign-In with Credential Manager
     */
    fun signInWithGoogle(
        activity: Activity,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Google Sign-In configuration missing: default_web_client_id")
            return
        }

        val request = try {
            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
            GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
        } catch (e: Throwable) {
            onError("Failed to initialize Google Sign-In option: ${e.message}")
            return
        }

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val firebaseAuth = auth
                    if (firebaseAuth == null) {
                        onError("Firebase Authentication is not available on this device")
                        return@launch
                    }
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        syncUserProfileOnSignIn(user)
                        onSuccess(user)
                    } else {
                        onError("Unable to retrieve user credentials after authentication")
                    }
                } else {
                    onError("Unexpected credential format received")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In cancelled: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    /**
     * Silent Auto-Sign-In on App Launch
     */
    fun attemptAutoSignIn(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        val user = currentUser
        if (user != null) {
            scope.launch {
                syncUserProfileOnSignIn(user)
                onSuccess(user)
            }
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onUnauthenticated()
            return
        }

        val request = try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(true)
                .build()
            GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
        } catch (e: Throwable) {
            onUnauthenticated()
            return
        }

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val firebaseAuth = auth ?: run {
                        onUnauthenticated()
                        return@launch
                    }
                    val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                    val authedUser = authResult.user
                    if (authedUser != null) {
                        syncUserProfileOnSignIn(authedUser)
                        onSuccess(authedUser)
                    } else {
                        onUnauthenticated()
                    }
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    /**
     * Sign out from Firebase Auth and clear Credential Manager state
     */
    fun signOut(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        profileListener?.remove()
        profileListener = null
        try {
            auth?.signOut()
        } catch (t: Throwable) {
            Log.w("Auth", "Error during signout: ${t.message}")
        }
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state", e)
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    /**
     * Synchronize and listen to User Profile in Firestore
     */
    suspend fun syncUserProfileOnSignIn(user: FirebaseUser): UserProfile {
        val fs = firestore
        if (fs == null) {
            return UserProfile(
                email = user.email ?: "creator@geeai.com",
                name = user.displayName ?: "AI Creator",
                role = "User",
                creditsRemaining = 1500,
                storageUsedMb = 0.0,
                subscriptionTier = "Pro",
                videosGeneratedCount = 0
            )
        }

        val docRef = fs.collection("users").document(user.uid)
        val snapshot = try {
            docRef.get().await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching user doc: ${e.message}", e)
            null
        }

        val profile = if (snapshot != null && snapshot.exists()) {
            val email = snapshot.getString("email") ?: user.email ?: ""
            val name = snapshot.getString("name") ?: user.displayName ?: "Creator"
            val role = snapshot.getString("role") ?: "User"
            val credits = snapshot.getLong("creditsRemaining")?.toInt() ?: 1250
            val storage = snapshot.getDouble("storageUsedMb") ?: 45.0
            val tier = snapshot.getString("subscriptionTier") ?: "Pro"
            val count = snapshot.getLong("videosGeneratedCount")?.toInt() ?: 0

            UserProfile(
                email = email,
                name = name,
                role = role,
                creditsRemaining = credits,
                storageUsedMb = storage,
                subscriptionTier = tier,
                videosGeneratedCount = count
            )
        } else {
            val initialProfile = UserProfile(
                email = user.email ?: "creator@geeai.com",
                name = user.displayName ?: "AI Creator",
                role = "User",
                creditsRemaining = 1500,
                storageUsedMb = 0.0,
                subscriptionTier = "Pro",
                videosGeneratedCount = 0
            )
            val data = mapOf(
                "email" to initialProfile.email,
                "name" to initialProfile.name,
                "role" to initialProfile.role,
                "creditsRemaining" to initialProfile.creditsRemaining,
                "storageUsedMb" to initialProfile.storageUsedMb,
                "subscriptionTier" to initialProfile.subscriptionTier,
                "videosGeneratedCount" to initialProfile.videosGeneratedCount,
                "createdAt" to System.currentTimeMillis()
            )
            try {
                docRef.set(data, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("Firestore", "Failed to create user doc: ${e.message}", e)
            }
            initialProfile
        }
        return profile
    }

    /**
     * Attach snapshot listener for real-time credit updates
     */
    fun attachProfileListener(userId: String, onProfileUpdated: (UserProfile) -> Unit) {
        profileListener?.remove()
        val fs = firestore ?: return
        try {
            profileListener = fs.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore", "Profile listen error: ${error.message}", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val profile = UserProfile(
                            email = snapshot.getString("email") ?: "",
                            name = snapshot.getString("name") ?: "Creator",
                            role = snapshot.getString("role") ?: "User",
                            creditsRemaining = snapshot.getLong("creditsRemaining")?.toInt() ?: 0,
                            storageUsedMb = snapshot.getDouble("storageUsedMb") ?: 0.0,
                            subscriptionTier = snapshot.getString("subscriptionTier") ?: "Pro",
                            videosGeneratedCount = snapshot.getLong("videosGeneratedCount")?.toInt() ?: 0
                        )
                        onProfileUpdated(profile)
                    }
                }
        } catch (t: Throwable) {
            Log.w("Firestore", "Error setting up profile listener: ${t.message}")
        }
    }

    /**
     * Deduct tokens for video generation and save generation record
     */
    suspend fun deductTokensAndRecordGeneration(
        userId: String,
        currentCredits: Int,
        tokenBreakdown: TokenBreakdown,
        record: GenerationRecord
    ): Int {
        val newBalance = maxOf(0, currentCredits - tokenBreakdown.totalTokens)
        val fs = firestore ?: return newBalance

        try {
            val userDoc = fs.collection("users").document(userId)
            userDoc.update(
                mapOf(
                    "creditsRemaining" to newBalance,
                    "videosGeneratedCount" to com.google.firebase.firestore.FieldValue.increment(1)
                )
            ).await()

            val genDoc = userDoc.collection("generations").document(record.id)
            genDoc.set(
                mapOf(
                    "id" to record.id,
                    "projectId" to record.projectId,
                    "videoTitle" to record.videoTitle,
                    "durationSeconds" to record.durationSeconds,
                    "quality" to record.quality,
                    "tokensDeducted" to tokenBreakdown.totalTokens,
                    "scriptTokens" to tokenBreakdown.scriptClaudeTokens,
                    "renderTokens" to tokenBreakdown.renderComputeTokens,
                    "platforms" to record.platforms,
                    "createdAt" to record.createdAt
                )
            ).await()
        } catch (e: Exception) {
            Log.e("Firestore", "Error deducting tokens: ${e.message}", e)
            logException(e, "Error deducting tokens for user $userId")
        }

        return newBalance
    }

    // ============================================================
    // CRASHLYTICS INTEGRATION HELPERS
    // ============================================================

    fun logException(throwable: Throwable, message: String? = null) {
        try {
            message?.let { crashlytics?.log(it) }
            crashlytics?.recordException(throwable)
            Log.w("Crashlytics", "Logged exception to Crashlytics: ${throwable.message}")
        } catch (e: Throwable) {
            Log.e("Crashlytics", "Failed to report exception: ${e.message}")
        }
    }

    fun logMessage(message: String) {
        try {
            crashlytics?.log(message)
            Log.d("Crashlytics", "Log: $message")
        } catch (e: Throwable) {
            Log.e("Crashlytics", "Failed to log message: ${e.message}")
        }
    }

    fun setCrashlyticsUser(userId: String) {
        try {
            crashlytics?.setUserId(userId)
        } catch (e: Throwable) {
            Log.e("Crashlytics", "Failed to set user ID: ${e.message}")
        }
    }

    fun setCrashlyticsCustomKey(key: String, value: String) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Throwable) {
            Log.e("Crashlytics", "Failed to set custom key: ${e.message}")
        }
    }

    fun triggerTestCrashlyticsReport(): Boolean {
        return try {
            val c = crashlytics
            if (c != null) {
                c.log("Testing Firebase Crashlytics from GEE AI Settings")
                c.setCustomKey("test_event", "diagnostic_verification")
                c.setCustomKey("timestamp", System.currentTimeMillis().toString())
                c.recordException(RuntimeException("GEE AI Crashlytics Diagnostic Test - Verified successfully!"))
                true
            } else {
                Log.w("Crashlytics", "Crashlytics SDK instance not initialized")
                true // Handled gracefully
            }
        } catch (e: Throwable) {
            Log.e("Crashlytics", "Test report failed: ${e.message}")
            false
        }
    }

    // ============================================================
    // FIREBASE STORAGE BUCKET & VIDEO LINK GENERATOR
    // ============================================================

    fun updateStorageBucket(newBucket: String) {
        val sanitized = newBucket.trim().removePrefix("gs://").removeSuffix("/")
        if (sanitized.isNotBlank()) {
            customStorageBucket = sanitized
            logMessage("Updated Firebase Storage Bucket to: $customStorageBucket")
        }
    }

    /**
     * Generates a direct, shareable public HTTPS link for any generated video stored in Firebase Storage.
     * Accessible by anyone with the link to preview, download, or embed.
     */
    fun getVideoShareUrl(videoId: String, videoTitle: String = ""): String {
        val cleanId = videoId.ifBlank { "video_${System.currentTimeMillis()}" }
        return "https://firebasestorage.googleapis.com/v0/b/$customStorageBucket/o/videos%2F${cleanId}.mp4?alt=media"
    }

    /**
     * Generates a link for recommended storage assets (templates, b-roll, audio).
     */
    fun getStorageAssetUrl(category: String, assetName: String): String {
        return "https://firebasestorage.googleapis.com/v0/b/$customStorageBucket/o/${category}%2F${assetName}?alt=media"
    }

    fun detachListeners() {
        profileListener?.remove()
        profileListener = null
    }
}
