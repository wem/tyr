package ch.sourcemotion.tyr.creator.datamapping

import ch.sourcemotion.tyr.creator.domain.entity.Quiz
import ch.sourcemotion.tyr.creator.dto.QuizDto
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate

object QuizMapper : Mapper<Quiz, QuizDto> {
    override fun mapToDto(entity: Quiz) = QuizDto(entity.id, entity.date.toKotlinLocalDate(), entity.stages.toDtos())

    override fun mapToEntity(dto: QuizDto) = Quiz(dto.id, dto.date.toJavaLocalDate(), dto.stages.toEntities())
}

fun Quiz.toDto() = QuizMapper.mapToDto(this)
fun QuizDto.toEntity() = QuizMapper.mapToEntity(this)

fun List<Quiz>.toDtos() = QuizMapper.mapToDtos(this)
fun List<QuizDto>.toEntities() = QuizMapper.mapToEntities(this)