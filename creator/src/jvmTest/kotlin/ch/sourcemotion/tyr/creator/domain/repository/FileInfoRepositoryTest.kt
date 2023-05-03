package ch.sourcemotion.tyr.creator.domain.repository

import ch.sourcemotion.tyr.creator.domain.MimeType
import ch.sourcemotion.tyr.creator.domain.entity.FileInfoEntity
import ch.sourcemotion.tyr.creator.ext.getOrCreateByFactory
import ch.sourcemotion.tyr.creator.testing.AbstractVertxDatabaseTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class FileInfoRepositoryTest : AbstractVertxDatabaseTest() {

    private lateinit var sut: FileInfoRepository

    @BeforeEach
    fun setUp() {
        sut = FileInfoRepository(vertx.getOrCreateByFactory())
    }

    @Test
    fun `save find by id and delete`(testContext: VertxTestContext) = testContext.async {
        val fileInfoId = UUID.randomUUID()
        val fileInfo = FileInfoEntity(fileInfoId, MimeType.PNG, "description")
        sut.save(fileInfo)
        sut.findById(fileInfoId).shouldBe(fileInfo)
        sut.delete(fileInfoId)
        sut.findById(fileInfoId).shouldBeNull()
    }

    @Test
    fun `save and update`(testContext: VertxTestContext) = testContext.async {
        val fileInfoId = UUID.randomUUID()
        val fileInfo = FileInfoEntity(fileInfoId, MimeType.JPEG, "description")
        sut.save(fileInfo)
        val updatedFileInfo = fileInfo.copy(description = "New description")
        sut.save(updatedFileInfo)
        sut.findById(fileInfoId).shouldBe(updatedFileInfo)
    }
}