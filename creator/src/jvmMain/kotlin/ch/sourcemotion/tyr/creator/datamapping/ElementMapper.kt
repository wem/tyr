package ch.sourcemotion.tyr.creator.datamapping

import ch.sourcemotion.tyr.creator.domain.entity.question.element.Image
import ch.sourcemotion.tyr.creator.domain.entity.question.element.QuestionElement
import ch.sourcemotion.tyr.creator.domain.entity.question.element.Sound
import ch.sourcemotion.tyr.creator.domain.entity.question.element.Text
import ch.sourcemotion.tyr.creator.dto.element.ImageDto
import ch.sourcemotion.tyr.creator.dto.element.QuestionElementDto
import ch.sourcemotion.tyr.creator.dto.element.SoundDto
import ch.sourcemotion.tyr.creator.dto.element.TextDto

object QuestionElementMapper : Mapper<QuestionElement, QuestionElementDto> {
    override fun mapToDto(entity: QuestionElement) = when(entity) {
        is Text -> entity.toDto()
        is Image -> entity.toDto()
        is Sound -> entity.toDto()
    }

    override fun mapToEntity(dto: QuestionElementDto) = when(dto) {
        is TextDto -> dto.toEntity()
        is ImageDto -> dto.toEntity()
        is SoundDto -> dto.toEntity()
    }
}

fun QuestionElement.toDto() = QuestionElementMapper.mapToDto(this)
fun QuestionElementDto.toEntity() = QuestionElementMapper.mapToEntity(this)

fun List<QuestionElement>.toDtos() = QuestionElementMapper.mapToDtos(this)
fun List<QuestionElementDto>.toEntities() = QuestionElementMapper.mapToEntities(this)

object TextMapper : Mapper<Text, TextDto> {
    override fun mapToDto(entity: Text) = TextDto(entity.text, entity.description)

    override fun mapToEntity(dto: TextDto) = Text(dto.text, dto.description)
}

fun Text.toDto() = TextMapper.mapToDto(this)
fun TextDto.toEntity() = TextMapper.mapToEntity(this)


object ImageMapper : Mapper<Image, ImageDto> {
    override fun mapToDto(entity: Image) = ImageDto(entity.id, entity.description, entity.mimeType.toDto())

    override fun mapToEntity(dto: ImageDto) = Image(dto.id, dto.mimeType.toEntity(), dto.description)
}

fun Image.toDto() = ImageMapper.mapToDto(this)
fun ImageDto.toEntity() = ImageMapper.mapToEntity(this)


object SoundMapper: Mapper<Sound, SoundDto> {
    override fun mapToDto(entity: Sound) = SoundDto(entity.id, entity.description, entity.mimeType.toDto())

    override fun mapToEntity(dto: SoundDto) = Sound(dto.id, dto.mimeType.toEntity(), dto.description)
}

fun Sound.toDto() = SoundMapper.mapToDto(this)
fun SoundDto.toEntity() = SoundMapper.mapToEntity(this)
