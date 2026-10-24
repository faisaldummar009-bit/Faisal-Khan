package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PromptCategory
import com.example.data.model.QuickPromptLibrary
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
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Light GPT", appName)
  }

  @Test
  fun `verify quick prompt library has categories and items`() {
    val prompts = QuickPromptLibrary.prompts
    assertTrue("Prompts should have more than 20 entries", prompts.size >= 20)

    val categories = prompts.map { it.category }.toSet()
    assertTrue(categories.contains(PromptCategory.WRITING))
    assertTrue(categories.contains(PromptCategory.CODING))
    assertTrue(categories.contains(PromptCategory.LEARNING))
    assertTrue(categories.contains(PromptCategory.CREATIVE))
  }
}
