package ch.sourcemotion.tyr.creator.ui.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private val scope = MainScope()

fun launch(block: suspend CoroutineScope.() -> Unit) {
    scope.launch(block = block)
}

fun executeExceptionHandled(onFailure: (Throwable) -> Unit, block: suspend () -> Unit) {
    launch {
        runCatching {
            block()
        }.onFailure { onFailure(it) }
    }
}