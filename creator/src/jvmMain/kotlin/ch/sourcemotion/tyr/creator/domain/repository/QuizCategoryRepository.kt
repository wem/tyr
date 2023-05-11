package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.domain.entity.Entity.Companion.ID_COLUMN
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory.Companion.COLUMN_NAMES_EXP
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory.Companion.INSERT_PARAMS_EXP
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory.Companion.SELECT_COLUMNS_EXP
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory.Companion.STAGE_FK_COLUMN
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory.Companion.TABLE
import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory.Companion.UPDATE_SET_EXP
import ch.sourcemotion.tyr.creator.domain.repository.mapping.fromdb.QuizCategoryRowMapper
import ch.sourcemotion.tyr.creator.domain.repository.mapping.todb.QuizCategoryTupleMapper
import ch.sourcemotion.tyr.creator.ext.encodeToJson
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.templates.SqlTemplate
import io.vertx.sqlclient.templates.TupleMapper
import java.util.*

class QuizCategoryRepository(pool: PgPool) : AbstractRepository(pool) {

    private companion object {
        const val SAVE_QUERY = "INSERT INTO $TABLE ($COLUMN_NAMES_EXP) VALUES ($INSERT_PARAMS_EXP) " +
                "ON CONFLICT($ID_COLUMN) " +
                "DO UPDATE SET $UPDATE_SET_EXP"

        const val DELETE_QUERY = "DELETE FROM $TABLE WHERE $ID_COLUMN = #{$ID_COLUMN}"

        const val FIND_ALL_OF_STAGE_QUERY =
            "SELECT $SELECT_COLUMNS_EXP FROM $TABLE qc WHERE qc.$STAGE_FK_COLUMN = #{$STAGE_FK_COLUMN}"

        const val FIND_BY_ID_QUERY = "SELECT $SELECT_COLUMNS_EXP FROM $TABLE qc " +
                "WHERE qc.$ID_COLUMN = #{$ID_COLUMN}"
    }

    suspend fun save(stageId: UUID, category: QuizCategory, client: SqlClient = pool) {
        runCatching {
            SqlTemplate.forUpdate(client, SAVE_QUERY)
                .mapFrom(TupleMapper.mapper(QuizCategoryTupleMapper(stageId)))
                .execute(category).await()
        }.getOrElse { failure ->
            throw QuizCategoryRepositoryException(
                "Failed to save quiz category '${category.encodeToJson()}' in stage with id '$stageId'", failure
            )
        }
    }

    suspend fun delete(id: UUID, client: SqlClient = pool) {
        runCatching {
            SqlTemplate.forUpdate(client, DELETE_QUERY).execute(mapOf(ID_COLUMN to id)).await()
        }.getOrElse { failure ->
            throw QuizCategoryRepositoryException("Failed to delete quiz category with id '$id'", failure)
        }
    }

    suspend fun findAllOfStage(stageId: UUID, client: SqlClient = pool): List<QuizCategory> {
        val mapper = QuizCategoryRowMapper()
        return runCatching {
            SqlTemplate.forQuery(client, FIND_ALL_OF_STAGE_QUERY)
                .mapTo(mapper).execute(mapOf(STAGE_FK_COLUMN to stageId))
                .await()
            mapper.buildFromRows()
        }.getOrElse { failure ->
            throw QuizCategoryRepositoryException("Failed to find all quiz categories of stage '$stageId'", failure)
        }
    }

    suspend fun findById(id: UUID, client: SqlClient = pool): QuizCategory? {
        val mapper = QuizCategoryRowMapper()
        return runCatching {
            SqlTemplate.forQuery(client, FIND_BY_ID_QUERY)
                .mapTo(mapper)
                .execute(mapOf(ID_COLUMN to id))
                .await()
            mapper.buildFromRows().firstOrNull()
        }.getOrElse { failure ->
            throw QuizCategoryRepositoryException("Failed to find quiz category by id '$id'", failure)
        }
    }
}

class QuizCategoryRepositoryException(message: String?, cause: Throwable? = null) : RepositoryException(message, cause)