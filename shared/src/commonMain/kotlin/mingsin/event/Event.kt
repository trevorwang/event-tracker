package mingsin.event

import kotlinx.serialization.Serializable

/**
 * Event data model
 * @param id Event unique identifier
 * @param name Event name
 * @param type Event type
 * @param timestamp Event timestamp (milliseconds)
 * @param data Event data (JSON string)
 * @param source Event source (App identifier)
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

