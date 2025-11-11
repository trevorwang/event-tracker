package mingsin.event

import kotlinx.serialization.Serializable

/**
 * 事件数据模型
 * @param id 事件唯一标识符
 * @param type 事件类型
 * @param timestamp 事件时间戳（毫秒）
 * @param data 事件数据（JSON 字符串）
 * @param source 事件来源（App 标识）
 */
@Serializable
data class Event(
    val id: String,
    val name: String,
    val type: String,
    val timestamp: Long,
    val data: String? = null,
    val source: String
)

