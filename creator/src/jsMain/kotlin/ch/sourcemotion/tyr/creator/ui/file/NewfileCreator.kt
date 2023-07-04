package ch.sourcemotion.tyr.creator.ui.file

import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.global.FileUpload
import ch.sourcemotion.tyr.creator.ui.global.GlobalMessageTrigger
import ch.sourcemotion.tyr.creator.ui.global.ShortMessageTrigger
import ch.sourcemotion.tyr.creator.ui.global.showError
import ch.sourcemotion.tyr.creator.ui.rest.rest
import com.benasher44.uuid.uuid4
import js.buffer.ArrayBuffer
import mu.KotlinLogging
import mui.material.*
import mui.material.styles.Theme
import mui.material.styles.useTheme
import react.FC
import react.Props
import react.useState

external interface NewFileCreatorProps : Props {
    var show: Boolean
    var onClose: () -> Unit
    var globalMessageTrigger: GlobalMessageTrigger
    var shortMessageTrigger: ShortMessageTrigger
}

private val logger = KotlinLogging.logger("NewFileCreator")

val NewFileCreator = FC<NewFileCreatorProps> { props ->
    val theme = useTheme<Theme>()

    var newFileDataState by useState<ArrayBuffer>()
    var newFileMimeTypeState by useState<MimeTypeDto>()
    var newFileDescriptionState by useState<String>()

    val isValid = { newFileDataState != null && newFileMimeTypeState != null }

    val clearStates = {
        newFileDataState = null
        newFileMimeTypeState = null
        newFileDescriptionState = null
    }

    Dialog {
        fullWidth = true
        open = props.show

        DialogTitle {
            +"Neue Datei hochladen"
        }

        DialogContent {
            FileUpload {
                fileData = newFileDataState
                description = newFileDescriptionState
                globalMessageTrigger = props.globalMessageTrigger
                onUploadFileData = { fileData, fileMimeType ->
                    newFileDataState = fileData
                    newFileMimeTypeState = fileMimeType
                }
                onUploadFileDescription = { description ->
                    newFileDescriptionState = description
                }
            }
        }

        DialogActions {
            Button {
                disabled = isValid()
                onClick = {
                    val newFileData = newFileDataState
                    val newFileMimeType = newFileMimeTypeState
                    if (newFileData != null && newFileMimeType != null) {
                        launch {
                            try {
                                rest.files.put(uuid4(), newFileDescriptionState, newFileData, newFileMimeType)
                            } catch (failure: Throwable) {
                                logger.error(failure) { "Failed to upload file" }
                                props.globalMessageTrigger.showError(
                                    "Hochladen fehlgeschlagen",
                                    "Das Hochladen der Datei ist fehlgeschlagen"
                                )
                            }
                            clearStates()
                        }
                    }
                }
                +"Erstellen / Hochladen"
            }
            Button {
                onClick = {
                    clearStates()
                    props.onClose()
                }
                +"Abbrechen"
            }
        }
    }
}