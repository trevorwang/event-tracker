package mingsin.event.architecture

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Lightweight MVI infrastructure, avoiding platform-specific dependencies
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


