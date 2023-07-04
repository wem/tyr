package ch.sourcemotion.tyr.creator.ui.global

import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto
import ch.sourcemotion.tyr.creator.ui.ext.centeredGridElements
import ch.sourcemotion.tyr.creator.ui.ext.columnFlow
import ch.sourcemotion.tyr.creator.ui.ext.rowFlow
import js.buffer.ArrayBuffer
import js.core.jso
import mu.KotlinLogging
import mui.material.*
import mui.material.styles.Theme
import mui.material.styles.TypographyVariant
import mui.material.styles.useTheme
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.input
import react.dom.onChange
import web.cssom.Display
import web.file.FileReader
import web.html.HTMLInputElement
import web.html.InputType

external interface FileUploadProps : Props {
    var fileData: ArrayBuffer?
    var description: String?
    var globalMessageTrigger: GlobalMessageTrigger
    var onUploadFileData: (fileData: ArrayBuffer, mimeType: MimeTypeDto) -> Unit
    var onUploadFileDescription: (description: String) -> Unit
}

private const val KB_SIZE = 1024
private const val MB_SIZE = KB_SIZE * 1024

private val logger = KotlinLogging.logger("FileUpload")

val FileUpload = FC<FileUploadProps> { props ->
    val inputRef = createRef<HTMLInputElement>()
    val theme = useTheme<Theme>()

    var uploadFileDisplayNameState by useState<String>()

    val showFailLoadError = {
        props.globalMessageTrigger.showError(
            "Fehler beim Lesen der Datei",
            "Die Datei ${inputRef.current?.value} konnte nicht gelesen werden"
        )
    }

    val catchFileBinary = {
        inputRef.current?.let { input ->
            val fileReader = FileReader()

            fileReader.onload = {
                val fileName = input.value
                val mimeType = detectMimeType(fileName)
                if (mimeType != null) {
                    val binaryData: ArrayBuffer = it.target.asDynamic().result
                    uploadFileDisplayNameState = input.value.substringAfterLast("/").substringAfterLast("\\")
                    props.onUploadFileData(binaryData, mimeType)
                } else {
                    logger.info { "Could not detect mimetype, based on the file name '$fileName'" }
                }
            }

            fileReader.onerror = {
                logger.error { "File reader failed to read file content" }
                uploadFileDisplayNameState = null
                showFailLoadError()
            }

            val file = input.files?.get(0)
            if (file != null) {
                fileReader.readAsArrayBuffer(file)
            } else {
                logger.error { "File list empty on file upload" }
                showFailLoadError()
            }
        }
    }

    Box {
        sx {
            margin = theme.spacing(1)
            display = Display.grid
            centeredGridElements()
            rowFlow()
            rowGap = theme.spacing(1)
        }

        Box {
            TextField {
                fullWidth = true
                label = ReactNode("Beschreibung")
                variant = FormControlVariant.outlined
                type = InputType.text
                value = props.description ?: ""
                InputLabelProps = jso { shrink = true }
                onChange = {
                    props.onUploadFileDescription(it.target.asDynamic().value)
                }
            }
        }

        Box {
            sx {
                display = Display.grid
                centeredGridElements()
                columnFlow()
                columnGap = theme.spacing(1)
            }

            IconButton {
                input {
                    ref = inputRef
                    type = InputType.file
                    hidden = true
                    onChange = {
                        catchFileBinary()
                    }
                }

                mui.icons.material.FileUpload()

                onClick = {
                    inputRef.current?.click()
                }
            }

            val fileData = props.fileData
            val uploadFileName = uploadFileDisplayNameState
            if (fileData != null && uploadFileName != null) {
                val fileSize = fileData.calcSizeDescription()
                Box {
                    +"$uploadFileName - $fileSize"
                }
            } else {
                Typography {
                    variant = TypographyVariant.h6
                    onClick = {
                        inputRef.current?.click()
                    }

                    +"Keine Datei ausgewählt"
                }
            }
        }
    }
}

private fun detectMimeType(fileName: String): MimeTypeDto? {
    return when (fileName.substringAfterLast(".").lowercase()) {
        "jpeg" -> MimeTypeDto.JPEG
        "jpg" -> MimeTypeDto.JPEG
        "bmp" -> MimeTypeDto.BMP
        "png" -> MimeTypeDto.PNG
        "mp3" -> MimeTypeDto.PNG
        else -> null
    }
}

private fun ArrayBuffer.calcSizeDescription(): String {
    return if (byteLength > MB_SIZE) {
        "${byteLength / MB_SIZE} MB"
    } else "${byteLength / KB_SIZE} KB"
}