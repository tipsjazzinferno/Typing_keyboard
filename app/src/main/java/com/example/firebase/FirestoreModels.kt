package com.example.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfileDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val coinBalance: Long = 0L,
    val activeThemeId: String = "midnight_oled",
    val activeLanguage: String = "en",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RedemptionDocument(
    val id: String = "",
    val userId: String = "",
    val cardTitle: String = "",
    val cardValue: String = "",
    val code: String = "",
    val cost: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomThemeDocument(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val backgroundColorHex: String = "",
    val keyColorHex: String = "",
    val textColorHex: String = "",
    val accentColorHex: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
