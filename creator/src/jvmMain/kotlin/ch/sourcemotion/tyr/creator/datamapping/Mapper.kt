package ch.sourcemotion.tyr.creator.datamapping

interface Mapper<ENTITY, DTO> {
    fun mapToDto(entity: ENTITY) : DTO
    fun mapToEntity(dto: DTO) : ENTITY

    fun mapToDtos(entities: List<ENTITY>) = entities.map { mapToDto(it) }
    fun mapToEntities(dtos: List<DTO>) = dtos.map { mapToEntity(it) }
}