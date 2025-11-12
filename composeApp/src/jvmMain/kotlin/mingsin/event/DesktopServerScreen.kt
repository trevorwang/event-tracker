package mingsin.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DesktopServerScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val isStarting by DesktopServerManager.isStarting.collectAsState()
    val isRunning by DesktopServerManager.isRunning.collectAsState()
    val endpoints by DesktopServerManager.endpoints.collectAsState()

    // Server lifecycle is now managed by DesktopServerManager, not tied to Compose lifecycle

    Column(
        modifier = modifier.padding(PaddingValues(horizontal = 16.dp, vertical = 24.dp))
    ) {
        Text(
            text = "Desktop Embedded Server",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (!isRunning && !isStarting) {
                    DesktopServerManager.start()
                }
            },
            enabled = !isRunning && !isStarting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    isRunning -> "Server Running"
                    isStarting -> "Starting..."
                    else -> "Start Server"
                }
            )
        }

        if (endpoints.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = buildString {
                        appendLine("Accessible URLs:")
                        endpoints.forEach { appendLine(it) }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


