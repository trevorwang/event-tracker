package mingsin.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mingsin.event.feature.list.EventListScreen
import mingsin.event.feature.server.DesktopServerIntent
import mingsin.event.feature.server.DesktopServerViewModel
import org.koin.compose.koinInject

@Composable
fun DesktopServerScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: DesktopServerViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    if (!uiState.isRunning) {
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
                value = uiState.portText,
                onValueChange = { newValue ->
                    viewModel.dispatch(DesktopServerIntent.UpdatePortText(newValue))
                },
                label = { Text("Port") },
                singleLine = true,
                enabled = !uiState.isStarting,
                isError = uiState.portError != null,
                supportingText = uiState.portError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    viewModel.dispatch(DesktopServerIntent.StartServer)
                },
                enabled = !uiState.isStarting && uiState.portError == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (uiState.isStarting) "Starting..." else "Start Server"
                )
            }
        }
    } else {
        Column(modifier = modifier) {
            // Server running: show addresses and stop button
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Server Address:",
                    style = MaterialTheme.typography.titleMedium,
                )
                uiState.endpoints.firstOrNull()?.let { endpoint ->
                    Text(
                        text = endpoint,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        viewModel.dispatch(DesktopServerIntent.StopServer)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                ) {
                    Text("Stop Server")
                }
            }
            Box {
                EventListScreen()
            }
        }
    }
}


