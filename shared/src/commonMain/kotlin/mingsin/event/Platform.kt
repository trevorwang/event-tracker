package mingsin.event

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform