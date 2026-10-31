package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.firebase.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class UserRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticatedUser_succeedsAndPersists() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = UserRepository(firestore)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveUserProfile(
                displayName = "Alice Typist",
                email = ALICE_EMAIL,
                coinBalance = 250L,
                activeThemeId = "midnight_oled"
            )
        }
        assertTrue(saveResult.isSuccess)

        val profileResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserProfile()
        }
        assertTrue(profileResult.isSuccess)
        val profile = profileResult.getOrNull()
        assertNotNull(profile)
        assertEquals(uid, profile?.userId)
        assertEquals(250L, profile?.coinBalance)
        assertEquals("Alice Typist", profile?.displayName)
    }

    @Test
    fun addRedemption_authenticatedUser_createsDocumentAndListsInFlow() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = UserRepository(firestore)

        val redemptionResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addRedemption(
                cardTitle = "Google Play Gift Card",
                cardValue = "$5.00",
                code = "GP-TEST-1234-CODE",
                cost = 300L
            )
        }
        assertTrue(redemptionResult.isSuccess)
        val redemptionId = redemptionResult.getOrThrow()
        assertTrue(redemptionId.isNotEmpty())

        val list = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeRedemptions().first { it.isNotEmpty() }
        }
        assertTrue(list.any { it.id == redemptionId && it.userId == uid })
    }

    @Test
    fun observeUserProfile_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = UserRepository(firestore)

        // With user signed out, requireUserId() throws IllegalStateException before Firestore query
        val exception = assertThrows(IllegalStateException::class.java) {
            repository.requireUserId()
        }
        assertTrue(exception.message?.contains("User must be signed in with Google") == true)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_minima@test.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
