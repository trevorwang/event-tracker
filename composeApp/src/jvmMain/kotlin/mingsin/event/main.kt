package mingsin.event

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mingsin.event.list.EventListScreen

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
        DesktopServerScreen(modifier = Modifier.fillMaxSize()) { webSocketClient ->
            // Pass the auto-connected WebSocketClient to App
            // App can use this client or create its own as needed
            EventListScreen(webSocketClient = webSocketClient)
        }
    }
}