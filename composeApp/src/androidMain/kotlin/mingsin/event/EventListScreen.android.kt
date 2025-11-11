package mingsin.event

actual fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return format.format(date)
}

