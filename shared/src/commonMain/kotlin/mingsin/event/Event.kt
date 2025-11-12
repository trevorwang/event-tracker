package mingsin.event

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Event data model
 * @param id Event unique identifier
 * @param name Event name
 * @param type Event type
 * @param timestamp Event timestamp (milliseconds)
 * @param data Event data (JSON string)
 * @param source Event source (App identifier)
 * @param deviceName Device name
 * @param deviceSerial Device serial number
 */
@Serializable
data class Event @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toHexString(),
    val name: String,
    val type: String = "unknown",
    val timestamp: Long = 0L,
    val data: String? = null,
    val source: String = "unknown",
    val extra: String? = null,
    val deviceName: String? = null,
    val deviceSerial: String? = null,
)

