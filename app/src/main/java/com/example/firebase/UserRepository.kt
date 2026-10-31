package com.example.firebase

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(),
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun saveUserProfile(
        displayName: String,
        email: String,
        coinBalance: Long,
        activeThemeId: String = "midnight_oled",
        activeLanguage: String = "en"
    ): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        val docRef = db.collection("users").document(uid)

        return try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val data = hashMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to displayName,
                    "email" to email,
                    "coinBalance" to coinBalance,
                    "activeThemeId" to activeThemeId,
                    "activeLanguage" to activeLanguage,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).await()
            } else {
                val updates = hashMapOf<String, Any>(
                    "displayName" to displayName,
                    "email" to email,
                    "coinBalance" to coinBalance,
                    "activeThemeId" to activeThemeId,
                    "activeLanguage" to activeLanguage,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateCoinBalance(newBalance: Long): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        val docRef = db.collection("users").document(uid)
        return try {
            val updates = mapOf<String, Any>(
                "coinBalance" to newBalance,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfileDocument?> {
        val uid = requireUserId()
        val path = "users/$uid"
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            val doc = if (snapshot.exists()) snapshot.toObject(UserProfileDocument::class.java) else null
            Result.success(doc)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    fun observeUserProfile(): Flow<UserProfileDocument?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfileDocument::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun addRedemption(
        cardTitle: String,
        cardValue: String,
        code: String,
        cost: Long
    ): Result<String> {
        val uid = requireUserId()
        val redemptionId = "redemp_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "users/$uid/redemptions/$redemptionId"
        val docRef = db.collection("users").document(uid).collection("redemptions").document(redemptionId)

        return try {
            val data = hashMapOf<String, Any>(
                "id" to redemptionId,
                "userId" to uid,
                "cardTitle" to cardTitle,
                "cardValue" to cardValue,
                "code" to code,
                "cost" to cost,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            Result.success(redemptionId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeRedemptions(): Flow<List<RedemptionDocument>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/redemptions"
        emitAll(
            db.collection("users").document(uid).collection("redemptions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(RedemptionDocument::class.java)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createCustomTheme(
        name: String,
        backgroundColorHex: String,
        keyColorHex: String,
        textColorHex: String,
        accentColorHex: String
    ): Result<String> {
        val uid = requireUserId()
        val themeId = "theme_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val path = "users/$uid/custom_themes/$themeId"
        val docRef = db.collection("users").document(uid).collection("custom_themes").document(themeId)

        return try {
            val data = hashMapOf<String, Any>(
                "id" to themeId,
                "userId" to uid,
                "name" to name,
                "backgroundColorHex" to backgroundColorHex,
                "keyColorHex" to keyColorHex,
                "textColorHex" to textColorHex,
                "accentColorHex" to accentColorHex,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            Result.success(themeId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeCustomThemes(): Flow<List<CustomThemeDocument>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/custom_themes"
        emitAll(
            db.collection("users").document(uid).collection("custom_themes")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CustomThemeDocument::class.java)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }
}
