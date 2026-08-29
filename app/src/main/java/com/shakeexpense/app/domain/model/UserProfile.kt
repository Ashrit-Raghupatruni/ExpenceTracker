package com.shakeexpense.app.domain.model

data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = true,
    val familyName: String? = null,
    val familyRole: String? = null,
    val isFamilyLinked: Boolean = false
) {
    val familyStatusDisplay: String
        get() = if (isFamilyLinked && !familyName.isNullOrBlank()) {
            "$familyName • ${familyRole ?: "MEMBER"}"
        } else {
            "Not in a Family Group"
        }
}
