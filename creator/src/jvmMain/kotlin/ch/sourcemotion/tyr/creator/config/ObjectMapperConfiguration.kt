package ch.sourcemotion.tyr.creator.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.blackbird.BlackbirdModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.json.jackson.DatabindCodec

fun configureObjectMapper() {
    DatabindCodec.mapper().applyCommonConfiguration()
    DatabindCodec.prettyMapper().applyCommonConfiguration()
}

fun ObjectMapper.applyCommonConfiguration(): ObjectMapper {
    propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
    enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    return registerModule(JavaTimeModule()).registerKotlinModule().registerModule(BlackbirdModule())
}