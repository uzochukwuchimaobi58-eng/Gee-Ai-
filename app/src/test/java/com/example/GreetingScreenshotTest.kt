package com.example

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent { MyApplicationTheme { Text("GEE AI") } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun testGeeAiAppRender() {
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.GeeAiApp()
      }
    }
  }

  @Test
  fun testAllScreensRender() {
    val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.data.AppViewModel(app)
    
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.GeeAiApp(viewModel = viewModel)
      }
    }

    com.example.data.Screen.entries.forEach { screen ->
      composeTestRule.runOnUiThread {
        viewModel.setScreen(screen)
      }
      composeTestRule.waitForIdle()
    }
  }
}
