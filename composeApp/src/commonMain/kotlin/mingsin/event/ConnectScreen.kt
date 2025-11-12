package mingsin.event

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ConnectScreen(
    modifier: Modifier = Modifier,
    defaultUrl: String = "ws://localhost:8080/ws/desktop",
    webSocketClient: WebSocketClient,
    onConnected: () -> Unit = {}
) {
    var wsUrl by remember { mutableStateOf(defaultUrl) }
    var isConnecting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PaddingValues(horizontal = 16.dp, vertical = 24.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Connect to WebSocket Server",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = wsUrl,
            onValueChange = { 
                wsUrl = it
                errorMessage = null
            },
            label = { Text("WebSocket URL") },
            singleLine = true,
            enabled = !isConnecting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (wsUrl.isNotBlank()) {
                    isConnecting = true
                    errorMessage = null
                    scope.launch {
                        val result = webSocketClient.connect(wsUrl)
                        isConnecting = false
                        if (result.isSuccess) {
                            onConnected()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Connection failed"
                        }
                    }
                }
            },
            enabled = !isConnecting && wsUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connecting...")
            } else {
                Text("Connect")
            }
        }

        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Error: $errorMessage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun ConnectScreenPreview() {
    MaterialTheme {
        ConnectScreen(
            webSocketClient = WebSocketClient()
        )
    }
}
