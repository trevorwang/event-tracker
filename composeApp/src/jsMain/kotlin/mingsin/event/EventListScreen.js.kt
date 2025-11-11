package mingsin.event

import kotlin.js.Date

actual fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp.toDouble())
    val year = date.getFullYear().toInt()
    val month = (date.getMonth().toInt() + 1).toString().padStart(2, '0')
    val day = date.getDate().toInt().toString().padStart(2, '0')
    val hours = date.getHours().toInt().toString().padStart(2, '0')
    val minutes = date.getMinutes().toInt().toString().padStart(2, '0')
    val seconds = date.getSeconds().toInt().toString().padStart(2, '0')
    return "$year-$month-$day $hours:$minutes:$seconds"
}

