package mingsin.event.logger

import co.touchlab.kermit.Logger

actual class FileLogWriterFactory {
    actual fun create(): Logger {
        return serverLogger
    }
}