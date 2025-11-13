package mingsin.event.di

import mingsin.event.service.WebSocketService
import org.koin.dsl.module

val appModule = module {
    // WebSocket Service
    single { WebSocketService() }
}
