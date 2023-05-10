package ch.sourcemotion.tyr.creator.commandquery

import ch.sourcemotion.tyr.creator.ext.ack
import io.vertx.core.Handler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import mu.KLogger

interface Addressable {
    val address: String
}

interface EventBusMsg {
    val deliveryOptions: DeliveryOptions
        get() = DeliveryOptions()
}

interface Cmd : Addressable, EventBusMsg

interface Query<RESPONSE> : Addressable, EventBusMsg

suspend fun EventBus.cmdMsg(cmd: Cmd) {
    val replyMsg = request<Any?>(
        cmd.address,
        cmd,
        cmd.deliveryOptions
    ).await()
    val replyBody = replyMsg.body()
    if (replyBody is Throwable) {
        throw replyBody
    }
}

suspend fun <R, Q : Query<R>> EventBus.queryMsg(query: Q): R {
    val replyMsg = request<Any?>(
        query.address,
        query,
        query.deliveryOptions
    ).await()
    val replyBody = replyMsg.body()
    if (replyBody is Throwable) {
        throw replyBody
    } else {
        return (replyMsg as Message<R>).body()
    }
}


fun <C : Cmd> CoroutineVerticle.onCommand(logger: KLogger, block: suspend (C) -> Unit): Handler<Message<C>> {
    return Handler { msg ->
        val exceptionHandler = CoroutineExceptionHandler { ctx, failure ->
            msg.reply(failure)
        }
        launch(exceptionHandler) {
            val cmd = msg.body()
            block(cmd)
            msg.ack()
        }
    }
}

fun <R, Q : Query<R>> CoroutineVerticle.onQuery(logger: KLogger, block: suspend (Q) -> R): Handler<Message<Q>> {
    return Handler { msg ->
        val exceptionHandler = CoroutineExceptionHandler { ctx, failure ->
            msg.reply(failure)
        }
        launch(exceptionHandler) {
            val query = msg.body()
            val result = block(query)
            msg.reply(result)
        }
    }
}