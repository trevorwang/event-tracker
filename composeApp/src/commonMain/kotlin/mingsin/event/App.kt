package mingsin.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Screen state
 */
sealed class AppScreen {
    data object Connect : AppScreen()
    data class EventList(val webSocketClient: WebSocketClient) : AppScreen()
}

@Composable
@Preview
fun App(webSocketClient: WebSocketClient? = null) {
    MaterialTheme {
        val providedClient = webSocketClient
        val webSocketClient = remember { providedClient ?: WebSocketClient() }
        val scope = rememberCoroutineScope()
        
        val isConnected by webSocketClient.isConnected.collectAsState()
        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Connect) }

        // Auto navigate to EventList if webSocketClient is provided and connected
        LaunchedEffect(providedClient, isConnected) {
            if (providedClient != null && isConnected) {
                // If provided client is connected, go directly to EventList
                currentScreen = AppScreen.EventList(webSocketClient)
            }
        }

        // Close WebSocket client when component is disposed (only if we created it)
        DisposableEffect(providedClient) {
            onDispose {
                if (providedClient == null) {
                    webSocketClient.close()
                }
            }
        }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            when (val screen = currentScreen) {
                is AppScreen.Connect -> {
                    ConnectScreen(
                        modifier = Modifier.fillMaxSize(),
                        webSocketClient = webSocketClient,
                        onConnected = {
                            currentScreen = AppScreen.EventList(webSocketClient)
                        }
                    )
                }

                is AppScreen.EventList -> {
                    EventListScreen(
                        modifier = Modifier.fillMaxSize(),
                        webSocketClient = screen.webSocketClient,
                        onDisconnect = {
                            scope.launch {
                                screen.webSocketClient.disconnect()
                                currentScreen = AppScreen.Connect
                            }
                        }
                    )
                }
            }
        }
    }
}