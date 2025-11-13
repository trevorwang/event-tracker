package mingsin.event

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import mingsin.event.di.appModule
import mingsin.event.di.desktopModule
import org.koin.core.context.startKoin

fun main() = application {
    // Initialize Koin
    startKoin {
        modules(appModule, desktopModule)
    }
    
    Window(
        onCloseRequest = {
            // Stop server gracefully before exiting
            DesktopServerManager.stop()
            exitApplication()
        },
        title = "EventTracker",
    ) {
        DesktopApp()
    }
}

@Composable
private fun DesktopApp() {
    MaterialTheme {
        DesktopServerScreen(modifier = Modifier.fillMaxSize())
    }
}