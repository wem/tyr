package ch.sourcemotion.tyr.creator.ui.file

import ch.sourcemotion.tyr.creator.dto.FileInfoDto
import ch.sourcemotion.tyr.creator.dto.element.MimeTypeDto
import ch.sourcemotion.tyr.creator.ui.OutletContextParams
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.global.FabKind
import ch.sourcemotion.tyr.creator.ui.global.FabSpec
import ch.sourcemotion.tyr.creator.ui.global.FloatingButtons
import ch.sourcemotion.tyr.creator.ui.rest.rest
import mui.material.*
import react.*
import react.dom.html.ReactHTML.img
import react.router.useOutletContext

val FileList = FC<Props> {

    val ctx = useOutletContext<OutletContextParams>()

    var fileSegmentsState by useState<List<FileSegment>>()

    var showNewFileCreatorState by useState(false)

    val loadFiles = {
        launch {
            val filesInfo = rest.files.getFilesInfo()
            fileSegmentsState = listOf(
                FileSegment("Bilder", filesInfo.filter {
                    it.mimeType == MimeTypeDto.BMP
                            || it.mimeType == MimeTypeDto.JPEG
                            || it.mimeType == MimeTypeDto.PNG
                }.map { FileInfoAndSource(it, rest.files.fileContentPathOf(it)) }),
                FileSegment(
                    "Musik",
                    filesInfo.filter { it.mimeType == MimeTypeDto.MP3 }
                        .map { FileInfoAndSource(it, rest.files.fileContentPathOf(it)) }),
            )
        }
    }

    useEffectOnce {
        loadFiles()
    }

    val fileSegments = fileSegmentsState
    if (fileSegments?.isNotEmpty() == true) {
        fileSegments.forEach { fileSegment ->
            Box {
                Typography {
                    +fileSegment.title
                }
                ImageList {
                    fileSegment.filesInfoAndSource.forEach { fileInfoAndSource ->
                        ImageListItem {
                            img {
                                src = fileInfoAndSource.src
                            }
                            ImageListItemBar {
                                title = ReactNode(fileInfoAndSource.fileInfo.description)
                                subtitle = ReactNode(fileInfoAndSource.fileInfo.mimeType.name)
                            }
                        }
                    }
                }
            }
        }
    }

    FloatingButtons {
        fabs = listOf(
            FabSpec("Neue Datei hochladen", FabColor.inherit, FabKind.NEW) {
                showNewFileCreatorState = true
            }
        )
    }

    NewFileCreator {
        show = showNewFileCreatorState
        globalMessageTrigger = ctx.globalMessageTrigger
        shortMessageTrigger = ctx.shortMessageTrigger
        onClose = {
            showNewFileCreatorState = false
        }
    }
}

private data class FileSegment(val title: String, val filesInfoAndSource: List<FileInfoAndSource>)
private data class FileInfoAndSource(val fileInfo: FileInfoDto, val src: String)