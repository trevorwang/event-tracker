package mingsin.event.di

import mingsin.event.feature.server.DesktopServerViewModel
import mingsin.event.feature.list.EventListViewModel
import mingsin.event.service.DesktopWebSocketService
import mingsin.event.service.WebSocketService
import org.koin.dsl.module

val desktopModule = module {
    // Desktop WebSocket Service
    single {
        DesktopWebSocketService(get<WebSocketService>())
    }
    
    // Desktop Server ViewModel (factory - new instance each time)
    factory { DesktopServerViewModel() }
    
    // Event List ViewModel (factory - new instance each time)
    factory {
        EventListViewModel(get<DesktopWebSocketService>())
    }
}

