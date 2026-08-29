@file:Suppress("DEPRECATION")

package com.shakeexpense.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("shakeexpense_auth_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadInitialProfile())
    val userProfile: Flow<UserProfile> = _userProfile.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val currentUser = auth.currentUser
            if (currentUser != null && !currentUser.isAnonymous) {
                val profile = buildProfile(currentUser)
                persistProfile(profile)
                _userProfile.value = profile
            }
            // Do NOT overwrite with null/anonymous if we have a persisted logged-in session!
        }
    }

    private fun loadInitialProfile(): UserProfile {
        // 1. Check if user was previously logged in in SharedPreferences
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val savedUserId = prefs.getString(KEY_USER_ID, null)

        if (isLoggedIn && !savedUserId.isNullOrBlank()) {
            return UserProfile(
                userId = savedUserId,
                displayName = prefs.getString(KEY_DISPLAY_NAME, "Google User") ?: "Google User",
                email = prefs.getString(KEY_EMAIL, null),
                photoUrl = prefs.getString(KEY_PHOTO_URL, null),
                isAnonymous = false
            )
        }

        // 2. Check FirebaseAuth
        val fbUser = firebaseAuth.currentUser
        if (fbUser != null && !fbUser.isAnonymous) {
            val profile = buildProfile(fbUser)
            persistProfile(profile)
            return profile
        }

        // 3. Check GoogleSignIn last account
        try {
            val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
            if (googleAccount != null) {
                val profile = UserProfile(
                    userId = googleAccount.id ?: googleAccount.email ?: "google_user",
                    displayName = googleAccount.displayName ?: "Google User",
                    email = googleAccount.email,
                    photoUrl = googleAccount.photoUrl?.toString(),
                    isAnonymous = false
                )
                persistProfile(profile)
                return profile
            }
        } catch (e: Exception) {
            // Ignore in test
        }

        // 4. Default Guest Profile
        return UserProfile(
            userId = AppDatabase.DEFAULT_USER_ID,
            displayName = "Local User",
            email = null,
            photoUrl = null,
            isAnonymous = true
        )
    }

    private fun persistProfile(profile: UserProfile) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, !profile.isAnonymous)
            .putString(KEY_USER_ID, profile.userId)
            .putString(KEY_DISPLAY_NAME, profile.displayName)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_PHOTO_URL, profile.photoUrl)
            .apply()
    }

    private fun clearPersistedProfile() {
        prefs.edit().clear().apply()
    }

    private fun buildProfile(user: FirebaseUser?): UserProfile {
        return if (user != null) {
            UserProfile(
                userId = user.uid,
                displayName = user.displayName ?: if (user.isAnonymous) "Guest User" else "Google User",
                email = user.email,
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = user.isAnonymous
            )
        } else {
            UserProfile(
                userId = AppDatabase.DEFAULT_USER_ID,
                displayName = "Local User",
                email = null,
                photoUrl = null,
                isAnonymous = true
            )
        }
    }

    suspend fun saveDirectGoogleProfile(
        userId: String,
        displayName: String,
        email: String?,
        photoUrl: String?
    ): UserProfile = withContext(Dispatchers.IO) {
        val profile = UserProfile(
            userId = userId,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl,
            isAnonymous = false
        )
        persistProfile(profile)
        _userProfile.value = profile
        profile
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = firebaseAuth.currentUser

            val authResult = if (currentUser != null && currentUser.isAnonymous) {
                try {
                    currentUser.linkWithCredential(credential).await()
                } catch (e: Exception) {
                    firebaseAuth.signInWithCredential(credential).await()
                }
            } else {
                firebaseAuth.signInWithCredential(credential).await()
            }

            val user = authResult.user
            val profile = buildProfile(user)
            persistProfile(profile)
            _userProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            clearPersistedProfile()
            try {
                firebaseAuth.signOut()
            } catch (e: Exception) { }
            try {
                GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            } catch (e: Exception) { }
            val defaultProfile = buildProfile(null)
            _userProfile.value = defaultProfile
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentProfile(): UserProfile {
        return _userProfile.value
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "auth_is_logged_in"
        private const val KEY_USER_ID = "auth_user_id"
        private const val KEY_DISPLAY_NAME = "auth_display_name"
        private const val KEY_EMAIL = "auth_email"
        private const val KEY_PHOTO_URL = "auth_photo_url"
    }
}
