package ch.sourcemotion.tyr.creator.ext

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T> Result<T>.onFailureAndRethrow(onFailure: (Throwable) -> Unit) : T {
     contract {
         callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
     }
     return getOrElse { failure ->
         onFailure(failure)
         throw failure
     }
 }