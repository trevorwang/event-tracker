package mingsin.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DesktopServerScreen(
    modifier: Modifier = Modifier
) {
    val isStarting by DesktopServerManager.isStarting.collectAsState()
    val isRunning by DesktopServerManager.isRunning.collectAsState()
    val endpoints by DesktopServerManager.endpoints.collectAsState()

    var portText by remember { mutableStateOf(SERVER_PORT.toString()) }
    var portError by remember { mutableStateOf<String?>(null) }

    if (!isRunning) {
        Column(
            modifier = modifier.padding(PaddingValues(horizontal = 16.dp, vertical = 24.dp))
        ) {
            Text(
                text = "Desktop Embedded Server",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Server not running: show port input and start button
            OutlinedTextField(
                value = portText,
                onValueChange = { newValue ->
                    portText = newValue
                    portError = null
                },
                label = { Text("Port") },
                singleLine = true,
                enabled = !isStarting,
                isError = portError != null,
                supportingText = portError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    val port = portText.toIntOrNull()
                    if (port == null || port < 1 || port > 65535) {
                        portError = "Please enter a valid port number (1-65535)"
                    } else {
                        DesktopServerManager.start(port)
                    }
                },
                enabled = !isStarting && portError == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isStarting) "Starting..." else "Start Server"
                )
            }
        }
    } else {
        // Server running: show addresses and stop button
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Server Address:",
                style = MaterialTheme.typography.titleMedium,
            )
            endpoints.firstOrNull()?.let { endpoint ->
                Text(
                    text = endpoint,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    DesktopServerManager.stop()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
            ) {
                Text("Stop Server")
            }
        }
    }
}


