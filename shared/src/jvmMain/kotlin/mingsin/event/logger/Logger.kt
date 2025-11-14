package mingsin.event.logger

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import java.io.File

val logFile = desktopLogFile("EventTracker")
fun desktopLogFile(appName: String): File {
    val home = System.getProperty("user.home")

    val dir = when {
        System.getProperty("os.name").contains("Mac", ignoreCase = true) ->
            File(home, "Library/Logs/$appName")

        System.getProperty("os.name").contains("Windows", ignoreCase = true) ->
            File(System.getenv("APPDATA"), "$appName/Logs")

        else ->
            File(home, ".local/share/$appName/logs") // Linux
    }

    dir.mkdirs()
    return File(dir, "app.log")
}

val serverLogger = Logger(
    config = StaticConfig(
        logWriterList = listOf(
            RotateFileLogWriter(logFile, maxSizeBytes = 5L * 1024 * 1024, keep = 3)
        )
    ),
    tag = "DesktopServerManager"
)
val clientLogger = Logger(
    config = StaticConfig(
        logWriterList = listOf(
            RotateFileLogWriter(logFile, maxSizeBytes = 5L * 1024 * 1024, keep = 3)
        )
    ),
    tag = "EventTracker"
)