package ch.sourcemotion.tyr.creator.datamapping

import ch.sourcemotion.tyr.creator.domain.entity.QuizStage
import ch.sourcemotion.tyr.creator.dto.QuizStageDto

object QuizStageMapper : Mapper<QuizStage, QuizStageDto> {
    override fun mapToDto(entity: QuizStage) =
        QuizStageDto(entity.id, entity.orderNumber, entity.description, entity.categories.toDtos())

    override fun mapToEntity(dto: QuizStageDto) = QuizStage(dto.id, dto.orderNumber, dto.description, dto.categories.toEntities())
}

fun QuizStage.toDto() = QuizStageMapper.mapToDto(this)
fun QuizStageDto.toEntity() = QuizStageMapper.mapToEntity(this)

fun List<QuizStage>.toDtos() = QuizStageMapper.mapToDtos(this)
fun List<QuizStageDto>.toEntities() = QuizStageMapper.mapToEntities(this)