package mingsin.event.api

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import mingsin.event.SERVER_PORT

/**
 * API Client Manager
 * Manages Ktorfit client and provides API interface
 * Supports dynamic server URL configuration
 */
class ApiClientManager {
    private var ktorfit: Ktorfit? = null
    private var eventApi: EventApi? = null
    
    private val _serverUrl = MutableStateFlow<String>(getDefaultServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()
    
    /**
     * Get default server URL
     */
    private fun getDefaultServerUrl(): String {
        return "http://localhost:$SERVER_PORT"
    }
    
    /**
     * Set server URL and recreate API client
     * @param url Server URL (e.g., "http://localhost:8080" or "https://api.example.com")
     */
    fun setServerUrl(url: String) {
        val normalizedUrl = normalizeUrl(url)
        if (_serverUrl.value != normalizedUrl) {
            _serverUrl.value = normalizedUrl
            recreateClient(normalizedUrl)
        }
    }
    
    /**
     * Normalize URL (remove trailing slash, ensure protocol)
     */
    private fun normalizeUrl(url: String): String {
        var normalized = url.trim()
        if (normalized.isEmpty()) {
            normalized = getDefaultServerUrl()
        }
        // Remove trailing slash
        if (normalized.endsWith("/")) {
            normalized = normalized.dropLast(1)
        }
        // Add protocol if missing
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://$normalized"
        }
        return normalized
    }
    
    /**
     * Recreate Ktorfit client with new base URL
     */
    private fun recreateClient(baseUrl: String) {
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    isLenient = true
                })
            }
        }
        
        ktorfit = Ktorfit.Builder()
            .baseUrl(baseUrl)
            .httpClient(httpClient)
            .build()
        
        @Suppress("DEPRECATION")
        eventApi = ktorfit!!.create<EventApi>()
    }
    
    /**
     * Get EventApi instance
     * Creates client if not already created
     */
    fun getEventApi(): EventApi {
        if (eventApi == null) {
            recreateClient(_serverUrl.value)
        }
        return eventApi!!
    }
    
    /**
     * Get current server URL
     */
    fun getCurrentServerUrl(): String {
        return _serverUrl.value
    }
}

