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

    // 界面销毁时停止服务器
    // 服务器生命周期现在由 DesktopServerManager 管理，不再随 Compose 销毁

    Column(
        modifier = modifier.padding(PaddingValues(horizontal = 16.dp, vertical = 24.dp))
    ) {
        Text(
            text = "桌面内置服务器",
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
                    isRunning -> "服务器已启动"
                    isStarting -> "启动中..."
                    else -> "启动服务器"
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
                        appendLine("可访问地址：")
                        endpoints.forEach { appendLine(it) }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


