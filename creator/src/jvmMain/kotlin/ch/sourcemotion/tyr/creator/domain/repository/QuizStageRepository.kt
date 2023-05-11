package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.domain.entity.Entity.Companion.ID_COLUMN
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage.Companion.COLUMN_NAMES_EXP
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage.Companion.INSERT_PARAMS_EXP
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage.Companion.QUIZ_FK_COLUMN
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage.Companion.SELECT_COLUMNS_EXP
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage.Companion.TABLE
import ch.sourcemotion.tyr.creator.domain.entity.QuizStage.Companion.UPDATE_SET_EXP
import ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb.QuizStageRowMapper
import ch.sourcemotion.tyr.creator.domain.repository.mapping.todb.QuizStageTupleMapper
import ch.sourcemotion.tyr.creator.ext.encodeToJson
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.templates.SqlTemplate
import io.vertx.sqlclient.templates.TupleMapper
import mu.KLogging
import java.util.*

class QuizStageRepository(pool: PgPool) : AbstractRepository(pool) {

    private companion object : KLogging() {
        const val SAVE_QUERY = "INSERT INTO $TABLE ($COLUMN_NAMES_EXP) VALUES ($INSERT_PARAMS_EXP) " +
                "ON CONFLICT($ID_COLUMN) " +
                "DO UPDATE SET $UPDATE_SET_EXP"

        const val DELETE_QUERY = "DELETE FROM $TABLE WHERE $ID_COLUMN = #{$ID_COLUMN}"

        const val FIND_ALL_OF_QUIZ_QUERY = "SELECT $SELECT_COLUMNS_EXP FROM $TABLE qs"

        const val FIND_BY_ID_QUERY = "SELECT $SELECT_COLUMNS_EXP FROM $TABLE qs " +
                "WHERE $ID_COLUMN = #{$ID_COLUMN}"

        const val FIND_BY_ID_WITH_CATEGORIES_QUERY =
            "SELECT ${SELECT_COLUMNS_EXP},${QuizCategory.SELECT_COLUMNS_EXP} " +
                    "FROM $TABLE qs " +
                    "LEFT OUTER JOIN ${QuizCategory.TABLE} qc ON (qs.$ID_COLUMN = qc.${QuizCategory.STAGE_FK_COLUMN}) " +
                    "WHERE qs.$ID_COLUMN = #{$ID_COLUMN}"
    }

    suspend fun save(quizId: UUID, stage: QuizStage, client: SqlClient = pool) {
        runCatching {
            SqlTemplate.forUpdate(client, SAVE_QUERY).mapFrom(TupleMapper.mapper(QuizStageTupleMapper(quizId)))
                .execute(stage).await()
        }.getOrElse { failure ->
            throw QuizStageRepositoryException(
                "Failed to save quiz stage '${stage.encodeToJson()}' in quiz with '$quizId'", failure
            )
        }
    }

    suspend fun delete(id: UUID, client: SqlClient = pool) {
        runCatching {
            SqlTemplate.forUpdate(client, DELETE_QUERY).execute(mapOf(ID_COLUMN to id)).await()
        }.getOrElse { failure -> throw QuizStageRepositoryException("Failed to delete quiz with id '$id'", failure) }
    }

    suspend fun findAllOfQuiz(quizId: UUID, client: SqlClient = pool): List<QuizStage> {
        val mapper = QuizStageRowMapper()
        return runCatching {
            SqlTemplate.forQuery(client, FIND_ALL_OF_QUIZ_QUERY).mapTo(mapper).execute(mapOf(QUIZ_FK_COLUMN to quizId))
                .await()
            mapper.buildFromRows()
        }.getOrElse { failure ->
            throw QuizStageRepositoryException("Failed to find all stages of quiz with id '${quizId}'", failure)
        }
    }

    suspend fun findById(id: UUID, client: SqlClient = pool): QuizStage? {
        val mapper = QuizStageRowMapper()
        return runCatching {
            SqlTemplate.forQuery(client, FIND_BY_ID_QUERY)
                .mapTo(mapper)
                .execute(mapOf(ID_COLUMN to id))
                .await()
            mapper.buildFromRows().firstOrNull()
        }.getOrElse { failure -> throw QuizStageRepositoryException("Failed to find stage by id '${id}'", failure) }
    }

    suspend fun findByIdWithCategories(id: UUID, client: SqlClient = pool): QuizStage? {
        val mapper = QuizStageRowMapper()
        return runCatching {
            SqlTemplate.forQuery(client, FIND_BY_ID_WITH_CATEGORIES_QUERY)
                .mapTo(mapper)
                .execute(mapOf(ID_COLUMN to id))
                .await()
            mapper.buildFromRows().firstOrNull()
        }.getOrElse { failure ->
            throw QuizStageRepositoryException("Failed to find stage (incl. its categories) by id '${id}'", failure)
        }
    }
}

class QuizStageRepositoryException(message: String?, cause: Throwable? = null) : RepositoryException(message, cause)