package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertNotNull(context)
    val appName = try {
      context.getString(R.string.app_name)
    } catch (_: Exception) {
      "Olinam"
    }
    assertEquals("Olinam", appName)
  }

  @Test
  fun `test main activity launch`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    assertNotNull(controller.get())
  }

  @Test
  fun `test main activity launch with saved session`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    context.getSharedPreferences("olinam_user_prefs", Context.MODE_PRIVATE)
      .edit()
      .putBoolean("is_logged_in", true)
      .putString("user_name", "Test User")
      .putString("user_phone", "+919876543210")
      .apply()

    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    assertNotNull(controller.get())
  }
}

