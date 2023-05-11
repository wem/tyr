package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.domain.entity.Entity.Companion.ID_COLUMN
import ch.sourcemotion.tyr.creator.domain.entity.Quiz
import ch.sourcemotion.tyr.creator.domain.entity.Quiz.Companion.COLUMN_NAMES_EXP
import ch.sourcemotion.tyr.creator.domain.entity.Quiz.Companion.INSERT_PARAMS_EXP
import ch.sourcemotion.tyr.creator.domain.entity.Quiz.Companion.SELECT_COLUMNS_EXP
import ch.sourcemotion.tyr.creator.domain.entity.Quiz.Companion.TABLE
import ch.sourcemotion.tyr.creator.domain.entity.Quiz.Companion.UPDATE_SET_EXP
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory.Companion.STAGE_FK_COLUMN
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage.Companion.QUIZ_FK_COLUMN
import ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb.QuizRowMapper
import ch.sourcemotion.tyr.creator.domain.repository.mapping.todb.QuizTupleMapper
import ch.sourcemotion.tyr.creator.ext.encodeToJson
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.templates.SqlTemplate
import io.vertx.sqlclient.templates.TupleMapper
import mu.KLogging
import java.util.*

class QuizRepository(pool: PgPool) : AbstractRepository(pool) {

    private companion object : KLogging() {
        const val SAVE_QUERY = "INSERT INTO $TABLE ($COLUMN_NAMES_EXP) VALUES ($INSERT_PARAMS_EXP) " +
                "ON CONFLICT($ID_COLUMN) " +
                "DO UPDATE SET $UPDATE_SET_EXP"

        const val DELETE_QUERY = "DELETE FROM $TABLE WHERE $ID_COLUMN = #{$ID_COLUMN}"

        const val FIND_ALL_QUERY = "SELECT $SELECT_COLUMNS_EXP FROM $TABLE q"

        const val FIND_BY_ID_QUERY = "SELECT $SELECT_COLUMNS_EXP FROM $TABLE q " +
                "WHERE q.$ID_COLUMN = #{$ID_COLUMN}"

        const val FIND_BY_ID_WITH_STAGES_QUERY = "SELECT $SELECT_COLUMNS_EXP,${QuizStage.SELECT_COLUMNS_EXP} " +
                "FROM $TABLE q " +
                "LEFT OUTER JOIN ${QuizStage.TABLE} qs ON (q.$ID_COLUMN = qs.$QUIZ_FK_COLUMN) " +
                "WHERE q.$ID_COLUMN = #{$ID_COLUMN}"

        const val FIND_BY_ID_WITH_STAGES_CATEGORIES_QUERY =
            "SELECT $SELECT_COLUMNS_EXP,${QuizStage.SELECT_COLUMNS_EXP},${QuizCategory.SELECT_COLUMNS_EXP} " +
                    "FROM $TABLE q " +
                    "LEFT OUTER JOIN ${QuizStage.TABLE} qs ON (q.$ID_COLUMN = qs.$QUIZ_FK_COLUMN) " +
                    "LEFT OUTER JOIN ${QuizCategory.TABLE} qc ON (qs.$ID_COLUMN = qc.$STAGE_FK_COLUMN) " +
                    "WHERE q.$ID_COLUMN = #{$ID_COLUMN}"
    }

    suspend fun save(quiz: Quiz, client: SqlClient = pool) {
        runCatching {
            SqlTemplate.forUpdate(client, SAVE_QUERY).mapFrom(TupleMapper.mapper(QuizTupleMapper)).execute(quiz).await()
        }.getOrElse { failure ->
            throw QuizRepositoryException("Failed to save quiz '${quiz.encodeToJson()}'", failure)
        }
    }

    suspend fun delete(id: UUID, client: SqlClient = pool) {
        runCatching {
            SqlTemplate.forUpdate(client, DELETE_QUERY).execute(mapOf(ID_COLUMN to id)).await()
        }.getOrElse { failure -> throw QuizRepositoryException("Failed to delete quiz with id '$id'", failure) }
    }

    suspend fun findAll(client: SqlClient = pool): List<Quiz> {
        val mapper = QuizRowMapper()
        return runCatching {
            SqlTemplate.forQuery(client, FIND_ALL_QUERY).mapTo(mapper).execute(emptyMap()).await()
            mapper.buildFromRows()
        }.getOrElse { failure -> throw QuizRepositoryException("Failed to find all quizzes", failure) }
    }

    suspend fun findById(id: UUID, client: SqlClient = pool): Quiz? {
        val mapper = QuizRowMapper()
        return runCatching {
            SqlTemplate.forQuery(client, FIND_BY_ID_QUERY)
                .mapTo(mapper)
                .execute(mapOf(ID_COLUMN to id))
                .await()
            mapper.buildFromRows().firstOrNull()
        }.getOrElse { failure -> throw QuizRepositoryException("Failed to find quiz by id '$id'", failure) }
    }

    suspend fun findByIdWithStages(id: UUID, client: SqlClient = pool): Quiz? {
        val mapper = QuizRowMapper()
        return runCatching {
            SqlTemplate.forQuery(client, FIND_BY_ID_WITH_STAGES_QUERY)
                .mapTo(mapper)
                .execute(mapOf(ID_COLUMN to id))
                .await()
            mapper.buildFromRows().firstOrNull()
        }.getOrElse { failure ->
            throw QuizRepositoryException("Failed to find quiz (incl. its stages) by id '$id'", failure)
        }
    }

    suspend fun findByIdWithStagesAndCategories(id: UUID, client: SqlClient = pool): Quiz? {
        val mapper = QuizRowMapper()
        return runCatching {
            SqlTemplate.forQuery(client, FIND_BY_ID_WITH_STAGES_CATEGORIES_QUERY)
                .mapTo(mapper)
                .execute(mapOf(ID_COLUMN to id))
                .await()
            mapper.buildFromRows().firstOrNull()
        }.getOrElse { failure ->
            throw QuizRepositoryException("Failed to find quiz (incl. its stages and categories) by id '$id'", failure)
        }
    }
}

class QuizRepositoryException(message: String?, cause: Throwable? = null) : RepositoryException(message, cause)