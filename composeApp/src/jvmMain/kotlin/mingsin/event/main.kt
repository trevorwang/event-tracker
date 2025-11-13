package mingsin.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "EventTracker",
    ) {
        DesktopApp()
    }
}

@Composable
private fun DesktopApp() {
    MaterialTheme {
        DesktopServerScreen(modifier = Modifier.fillMaxSize()) {
            App()
        }
    }
}