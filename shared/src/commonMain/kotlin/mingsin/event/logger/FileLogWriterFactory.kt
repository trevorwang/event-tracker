package mingsin.event.logger

import co.touchlab.kermit.Logger

expect class FileLogWriterFactory() {
    fun create(): Logger
}