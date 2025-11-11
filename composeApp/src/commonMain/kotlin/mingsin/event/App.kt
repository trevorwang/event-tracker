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
 * 页面状态
 */
sealed class AppScreen {
    data object Connect : AppScreen()
    data class EventList(val webSocketClient: WebSocketClient) : AppScreen()
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Connect) }
        val webSocketClient = remember { WebSocketClient() }
        val scope = rememberCoroutineScope()
        
        // 当组件销毁时关闭 WebSocket 客户端
        DisposableEffect(Unit) {
            onDispose {
                webSocketClient.close()
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