package mingsin.event

import kotlinx.serialization.Serializable

/**
 * API 响应数据模型
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

/**
 * 事件创建响应
 */
@Serializable
data class EventResponse(
    val success: Boolean,
    val message: String,
    val eventId: String
)

/**
 * 服务器状态响应
 */
@Serializable
data class StatusResponse(
    val status: String,
    val appClients: Int,
    val desktopClients: Int
)

