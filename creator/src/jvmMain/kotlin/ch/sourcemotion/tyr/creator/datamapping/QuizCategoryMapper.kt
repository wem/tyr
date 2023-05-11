package ch.sourcemotion.tyr.creator.datamapping

import ch.sourcemotion.tyr.creator.domain.entity.QuizCategory
import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto

object QuizCategoryMapper : Mapper<QuizCategory, QuizCategoryDto> {
    override fun mapToDto(entity: QuizCategory) = QuizCategoryDto(
        entity.id, entity.title, entity.number, entity.contextOrQuestionText.toDto(), entity.questions.toDtos()
    )

    override fun mapToEntity(dto: QuizCategoryDto) =
        QuizCategory(dto.id, dto.title, dto.number, dto.contextOrQuestionText.toEntity(), dto.questions.toEntities())
}

fun QuizCategory.toDto() = QuizCategoryMapper.mapToDto(this)
fun QuizCategoryDto.toEntity() = QuizCategoryMapper.mapToEntity(this)

fun List<QuizCategory>.toDtos() = QuizCategoryMapper.mapToDtos(this)
fun List<QuizCategoryDto>.toEntities() = QuizCategoryMapper.mapToEntities(this)
