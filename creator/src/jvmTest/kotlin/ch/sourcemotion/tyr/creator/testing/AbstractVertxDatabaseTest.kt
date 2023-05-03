package ch.sourcemotion.tyr.creator.testing

import ch.sourcemotion.tyr.creator.config.PostgresConfig
import ch.sourcemotion.tyr.creator.database.DatabaseVersioning
import ch.sourcemotion.tyr.creator.ext.createPool
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import ch.sourcemotion.tyr.creator.ext.shareFactory
import ch.sourcemotion.tyr.creator.testing.extension.OnceStartedContainer
import ch.sourcemotion.tyr.creator.testing.extension.StartContainersOnceExtension
import io.vertx.core.VertxOptions
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgPool
import mu.KLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@ExtendWith(StartContainersOnceExtension::class)
abstract class AbstractVertxDatabaseTest(vertxOptions: VertxOptions = VertxOptions()) :
    AbstractVertxTest(vertxOptions) {

    private companion object : KLogging() {
        private val postgresImage: DockerImageName =
            DockerImageName.parse("postgres:${System.getenv("POSTGRES_VERSION")}")

        @get:OnceStartedContainer
        @JvmStatic
        val postgresContainer = PostgreSQLContainer(postgresImage)
    }

    @BeforeEach
    fun setUpDatabase(testContext: VertxTestContext) = testContext.async {
        val postgresConfig = getPostgresConfig()
        vertx.shareFactory { postgresConfig.createPool(vertx) }

        cleanupSchema()

        DatabaseVersioning(vertx, postgresConfig).updateDatabase()
    }

    private suspend fun cleanupSchema() {
        try {
            val pgPool = vertx.getOrCreateByFactory<PgPool>()
            val connection = pgPool.connection.await()
            connection.query("DROP SCHEMA public CASCADE;").execute().await()
            connection.query("CREATE SCHEMA public;").execute().await()
            connection.close().await()
        } catch (cause: Throwable) {
            logger.warn { "Failed to cleanup database" }
        }
    }

    protected fun getPostgresConfig() = PostgresConfig(
        postgresContainer.username,
        postgresContainer.password,
        postgresContainer.host,
        postgresContainer.firstMappedPort,
        false,
        postgresContainer.databaseName
    )
}