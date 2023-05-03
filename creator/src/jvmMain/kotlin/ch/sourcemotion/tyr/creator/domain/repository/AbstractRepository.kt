package ch.sourcemotion.tyr.creator.domain.repository

import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.TransactionRollbackException

abstract class AbstractRepository(
    val pool: PgPool
) {
    suspend inline fun <T> withTx(block: (SqlConnection) -> T): T {
        val conn = runCatching {
            pool.connection.await()
        }.getOrElse { cause -> throw RepositoryException("Failed to obtain database connection", cause) }

        val tx = conn.runCatching {
            begin().await()
        }.getOrElse { cause -> throw RepositoryException("Failed to begin transaction", cause) }

        return try {
            block(conn).also { tx.commit().await() }
        } catch (cause: TransactionRollbackException) {
            throw RepositoryException("Transactional operation failed. Rollback executed", cause)
        } catch (cause: Exception) {
            tx.runCatching { rollback().await() }
                .onFailure { throw RepositoryException("Failed to rollback transaction", it) }
            throw RepositoryException("Transactional operation failed. Rollback executed", cause)
        } finally {
            conn.runCatching { close().await() }
                .onFailure { throw RepositoryException("Failed to close connection (Release back to pool)") }
        }
    }
}