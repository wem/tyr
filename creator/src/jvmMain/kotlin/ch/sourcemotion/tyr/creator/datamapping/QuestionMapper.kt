package ch.sourcemotion.tyr.creator.datamapping

import ch.sourcemotion.tyr.creator.domain.entity.question.*
import ch.sourcemotion.tyr.creator.dto.question.*

object QuestionMapper : Mapper<Question, QuestionDto> {
    override fun mapToDto(entity: Question) = when(entity) {
        is SimpleElementQuestion<*> -> entity.toDto()
        is SortElementQuestion<*> -> entity.toDto()
        is MultiChoiceQuestion<*> -> entity.toDto()
        is AssociationQuestion<*> -> entity.toDto()
    }

    override fun mapToEntity(dto: QuestionDto) = when(dto) {
        is SimpleElementQuestionDto<*> -> dto.toEntity()
        is SortElementQuestionDto<*> -> dto.toEntity()
        is MultiChoiceQuestionDto<*> -> dto.toEntity()
        is AssociationQuestionDto<*> -> dto.toEntity()
    }
}

fun Question.toDto() = QuestionMapper.mapToDto(this)
fun QuestionDto.toEntity() = QuestionMapper.mapToEntity(this)

fun List<Question>.toDtos() = QuestionMapper.mapToDtos(this)
fun List<QuestionDto>.toEntities() = QuestionMapper.mapToEntities(this)

object SimpleElementQuestionMapper : Mapper<SimpleElementQuestion<*>, SimpleElementQuestionDto<*>> {
    override fun mapToDto(entity: SimpleElementQuestion<*>) =
        SimpleElementQuestionDto(entity.question.toDto(), entity.answer.toDto())

    override fun mapToEntity(dto: SimpleElementQuestionDto<*>) =
        SimpleElementQuestion(dto.question.toEntity(), dto.answer.toEntity())
}

fun SimpleElementQuestion<*>.toDto() = SimpleElementQuestionMapper.mapToDto(this)
fun SimpleElementQuestionDto<*>.toEntity() = SimpleElementQuestionMapper.mapToEntity(this)


object SortElementQuestionMapper : Mapper<SortElementQuestion<*>, SortElementQuestionDto<*>> {
    override fun mapToDto(entity: SortElementQuestion<*>) =
        SortElementQuestionDto(entity.questionElements.toDtos(), entity.answerElements.toDtos())

    override fun mapToEntity(dto: SortElementQuestionDto<*>) =
        SortElementQuestion(dto.questionElements.toEntities(), dto.answerElements.toEntities())
}

fun SortElementQuestion<*>.toDto() = SortElementQuestionMapper.mapToDto(this)
fun SortElementQuestionDto<*>.toEntity() = SortElementQuestionMapper.mapToEntity(this)


object MultiChoiceQuestionMapper : Mapper<MultiChoiceQuestion<*>, MultiChoiceQuestionDto<*>> {
    override fun mapToDto(entity: MultiChoiceQuestion<*>) = MultiChoiceQuestionDto(entity.answerElements.toDtos())

    override fun mapToEntity(dto: MultiChoiceQuestionDto<*>) = MultiChoiceQuestion(dto.answerElements.toEntities())
}

fun MultiChoiceQuestion<*>.toDto() = MultiChoiceQuestionMapper.mapToDto(this)
fun MultiChoiceQuestionDto<*>.toEntity() = MultiChoiceQuestionMapper.mapToEntity(this)


object AssociationQuestionMapper : Mapper<AssociationQuestion<*>, AssociationQuestionDto<*>> {
    override fun mapToDto(entity: AssociationQuestion<*>) =
        AssociationQuestionDto(entity.questionElements.toDtos(), entity.answerElements.toDtos())

    override fun mapToEntity(dto: AssociationQuestionDto<*>) =
        AssociationQuestion(dto.questionElements.toEntities(), dto.answerElements.toEntities())
}

fun AssociationQuestion<*>.toDto() = AssociationQuestionMapper.mapToDto(this)
fun AssociationQuestionDto<*>.toEntity() = AssociationQuestionMapper.mapToEntity(this)
