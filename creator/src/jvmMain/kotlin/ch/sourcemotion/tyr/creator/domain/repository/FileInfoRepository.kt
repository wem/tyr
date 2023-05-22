package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.domain.entity.Entity.Companion.ID_COLUMN
import ch.sourcemotion.tyr.creator.domain.entity.FileInfoEntity
import ch.sourcemotion.tyr.creator.domain.entity.FileInfoEntity.Companion.COLUMN_NAMES_EXP
import ch.sourcemotion.tyr.creator.domain.entity.FileInfoEntity.Companion.INSERT_PARAMS_EXP
import ch.sourcemotion.tyr.creator.domain.entity.FileInfoEntity.Companion.SELECT_COLUMNS_EXP
import ch.sourcemotion.tyr.creator.domain.entity.FileInfoEntity.Companion.TABLE
import ch.sourcemotion.tyr.creator.domain.entity.FileInfoEntity.Companion.UPDATE_SET_EXP
import ch.sourcemotion.tyr.creator.ext.encodeToJson
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.templates.SqlTemplate
import java.util.*

class FileInfoRepository(pool: PgPool) : AbstractRepository(pool) {
    private companion object {
        const val SAVE_QUERY = "INSERT INTO $TABLE ($COLUMN_NAMES_EXP) VALUES ($INSERT_PARAMS_EXP) " +
                "ON CONFLICT($ID_COLUMN) " +
                "DO UPDATE SET $UPDATE_SET_EXP"

        const val DELETE_QUERY = "DELETE FROM $TABLE WHERE $ID_COLUMN = #{$ID_COLUMN}"

        const val FIND_BY_ID_QUERY = "SELECT $SELECT_COLUMNS_EXP FROM $TABLE f " +
                "WHERE f.$ID_COLUMN = #{$ID_COLUMN}"

        const val FIND_ALL_QUERY = "SELECT $SELECT_COLUMNS_EXP FROM $TABLE f"
    }


    suspend fun save(fileInfo: FileInfoEntity, client: SqlClient = pool) {
        runCatching {
            SqlTemplate.forUpdate(client, SAVE_QUERY)
                .mapFrom(FileInfoEntity::class.java)
                .execute(fileInfo).await()
        }.getOrElse { failure ->
            throw FileInfoRepositoryException("Failed to save file info '${fileInfo.encodeToJson()}'", failure)
        }
    }

    suspend fun delete(id: UUID, client: SqlClient = pool) {
        runCatching {
            SqlTemplate.forUpdate(client, DELETE_QUERY).execute(mapOf(ID_COLUMN to id)).await()
        }.getOrElse { failure ->
            throw FileInfoRepositoryException(
                "Failed to delete file info with id '$id'", failure
            )
        }
    }

    suspend fun findAll(client: SqlClient = pool) : List<FileInfoEntity> {
        return runCatching {
            SqlTemplate.forQuery(client, FIND_ALL_QUERY)
                .mapTo(FileInfoEntity::class.java)
                .execute(emptyMap())
                .await().map { it }
        }.getOrElse { failure ->
            throw FileInfoRepositoryException("Failed to find all file infos", failure)
        }
    }

    suspend fun findById(id: UUID, client: SqlClient = pool): FileInfoEntity? {
        return runCatching {
            SqlTemplate.forQuery(client, FIND_BY_ID_QUERY)
                .mapTo(FileInfoEntity::class.java)
                .execute(mapOf(ID_COLUMN to id))
                .await().firstOrNull()
        }.getOrElse { failure ->
            throw FileInfoRepositoryException("Failed to find file info by id '$id'", failure)
        }
    }
}

class FileInfoRepositoryException(message: String?, cause: Throwable? = null) : RepositoryException(message, cause)