package mingsin.event.architecture

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * 轻量级 MVI 基础设施，避免引入平台特定依赖
 */
interface UiState
interface UiIntent
interface UiEffect

open class SimpleViewModel : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job

    open fun onCleared() {
        coroutineContext.cancel()
    }
}


