package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.keyboard.engine.KeyboardPreferences
import com.example.keyboard.engine.RewardsManager
import com.example.keyboard.model.KeyboardTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Minima Keyboard", appName)
  }

  @Test
  fun `test keyboard preferences initialization`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val preferences = KeyboardPreferences.getInstance(context)
    assertEquals(KeyboardTheme.SystemAuto.id, preferences.themeId.value)
    assertTrue(preferences.swipeLeftDelete.value)
    assertTrue(preferences.spaceCursorGlide.value)
  }

  @Test
  fun `test rewards manager welcome bonus`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val rewards = RewardsManager.getInstance(context)
    assertTrue("User should receive welcome bonus coins", rewards.coins.value >= 150)
  }
}
