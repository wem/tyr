package ch.sourcemotion.tyr.creator.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.FileWriter

class LocalConfigurationSerializer {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val localConfig = CreatorConfig(
                WebServerConfig(port = 8887, fileUploadFolder = "/home/michel/private/tyr-filestore", true),
                PostgresConfig("postgres", "tyr", "localhost", 5432, false, "postgres"),
                FileStorageConfig("/home/michel/private/tyr-filestore")
            )
            val outputFilePath = "${System.getProperty("user.dir")}/creator/local-config/local.yaml"

            val objectMapper = ObjectMapper(YAMLFactory())
            objectMapper.applyCommonConfiguration()

            FileWriter(outputFilePath).use { fw ->
                objectMapper.writeValue(fw, localConfig)
            }
        }
    }
}