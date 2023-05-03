package ch.sourcemotion.tyr.creator.testing

import ch.sourcemotion.tyr.creator.config.configureObjectMapper
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.junit5.Checkpoint
import io.vertx.junit5.RunTestOnContext
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension

@ExtendWith(VertxExtension::class)
abstract class AbstractVertxTest(private val vertxOptions: VertxOptions = VertxOptions()) {

    @RegisterExtension
    val rc = RunTestOnContext(vertxOptions)

    protected lateinit var vertx: Vertx
        private set

    @BeforeEach
    fun setUpVertx() {
        configureObjectMapper()
        vertx = rc.vertx()
    }


    fun VertxTestContext.async(block: suspend CoroutineScope.() -> Unit) {
        val checkpoint = checkpoint()
        CoroutineScope(vertx.orCreateContext.dispatcher()).launch {
            runCatching { block() }
                .onSuccess { checkpoint.flag() }
                .onFailure { failNow(it) }
        }
    }

    fun VertxTestContext.async(
        checkpoint: Checkpoint,
        block: suspend CoroutineScope.(Checkpoint) -> Unit
    ) {
        val controlCheckpoint = checkpoint()
        CoroutineScope(vertx.orCreateContext.dispatcher()).launch {
            runCatching { block(checkpoint) }
                .onSuccess { controlCheckpoint.flag() }
                .onFailure { failNow(it) }
        }
    }

    fun VertxTestContext.async(
        checkpoints: Int,
        block: suspend CoroutineScope.(Checkpoint) -> Unit
    ) = async(checkpoint(checkpoints), block)

    fun VertxTestContext.asyncDelayed(
        checkpoints: Int,
        delay: Long = 0,
        block: suspend CoroutineScope.(Checkpoint) -> Unit
    ) = async(checkpoint(checkpoints)) { checkpoint ->
        val controlCheckpoint = checkpoint()
        block(checkpoint)
        // We start an own coroutine for the control checkpoint, so the usual test block can end and just the control
        // checkpoint is pending.
        launch {
            delay(delay)
            controlCheckpoint.flag()
        }
    }
}