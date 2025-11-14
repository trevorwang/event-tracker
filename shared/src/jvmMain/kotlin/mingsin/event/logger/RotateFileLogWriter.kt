package mingsin.event.logger

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class RotateFileLogWriter(
    private val logFile: File,
    private val maxSizeBytes: Long = 5L * 1024 * 1024, // 5 MB
    private val keep: Int = 3 // keep 3 backup files: app.log.1, app.log.2, app.log.3
) : LogWriter() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    override fun isLoggable(tag: String, severity: Severity): Boolean = true

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        rotateIfNeeded()

        val time = dateFormat.format(Date())
        val logLine = buildString {
            append("$time [$severity] [$tag] $message")
            if (throwable != null) {
                append("\n${throwable.stackTraceToString()}")
            }
            append("\n")
        }

        synchronized(this) {
            FileWriter(logFile, true).use { writer ->
                writer.append(logLine)
            }
        }
    }

    private fun rotateIfNeeded() {
        if (logFile.exists() && logFile.length() >= maxSizeBytes) {
            synchronized(this) {
                // rotate: app.log.(keep-1) → app.log.keep
                for (i in keep downTo 1) {
                    val older = File("${logFile.path}.$i")
                    val newer = if (i == 1) logFile else File("${logFile.path}.${i - 1}")

                    if (newer.exists()) {
                        newer.renameTo(older)
                    }
                }

                // create new empty app.log
                logFile.writeText("") 
            }
        }
    }
}