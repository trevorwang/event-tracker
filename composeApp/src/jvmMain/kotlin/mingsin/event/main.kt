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
        var tabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("服务器", "客户端")
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (tabIndex) {
                0 -> DesktopServerScreen(modifier = Modifier.fillMaxSize())
                else -> App()
            }
        }

    }
}