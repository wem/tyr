package ch.sourcemotion.tyr.creator.ui.stage

import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.ext.centeredGridElements
import ch.sourcemotion.tyr.creator.ui.ext.rowFlow
import ch.sourcemotion.tyr.creator.ui.global.*
import ch.sourcemotion.tyr.creator.ui.navigate
import ch.sourcemotion.tyr.creator.ui.rest.rest
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import js.core.jso
import mu.KotlinLogging
import mui.material.*
import mui.system.sx
import react.*
import react.dom.onChange
import react.router.useNavigate
import web.cssom.pct
import web.cssom.px
import web.html.InputType

external interface NewQuizStageCreatorProps : Props {
    var parentQuizId: Uuid
    var show: Boolean
    var globalMessageTrigger: GlobalMessageTrigger
    var shortMessageTrigger: ShortMessageTrigger
    var onClose: () -> Unit
}

private val logger = KotlinLogging.logger("NewQuizStageCreator")

val NewQuizStageCreator = FC<NewQuizStageCreatorProps> { props ->

    val nav = useNavigate()

    var openNewQuizStageDialog by useState(false)

    var newQuizStageDescription by useState("")

    val showCreationFailure = {
        props.globalMessageTrigger.showError(
            SAVE_FAILURE_TITLE,
            "Quiz Seite konnte nicht erstellt werden. Bitte versuche es erneut."
        )
    }

    useEffect {
        openNewQuizStageDialog = props.show
    }

    Dialog {
        open = openNewQuizStageDialog
        onClose = { _, _ ->
            props.onClose()
        }

        DialogTitle {
            +"Neue Quiz Seite erstellen"
        }
        DialogContent {
            Grid {
                container = true
                sx {
                    rowFlow()
                    centeredGridElements()
                    paddingBottom = 8.px
                    paddingTop = 8.px
                    rowGap = 24.px
                    width = 100.pct
                }

                TextField {
                    sx {
                        width = 51.pct
                    }

                    label = ReactNode("Beschreibung")
                    variant = FormControlVariant.outlined
                    type = InputType.text
                    value = newQuizStageDescription
                    InputLabelProps = jso { shrink = true }
                    onChange = {
                        newQuizStageDescription = it.target.asDynamic().value
                    }
                }
            }
            DialogActions {
                Button {
                    onClick = {
                        val newQuizStageId = uuid4()
                        launch {
                            val nextStageOrderNumber =
                                runCatching {
                                    val existingStages = rest.stages.getAll(props.parentQuizId)
                                    if (existingStages.isEmpty()) 0 else existingStages.maxOf { it.orderNumber }
                                }.getOrElse {
                                    logger.error(it) { "Failed to evaluate next stage order number on quiz '${props.parentQuizId}'" }
                                    showCreationFailure()
                                    null
                                }

                            if (nextStageOrderNumber != null) {
                                runCatching {
                                    rest.stages.put(
                                        props.parentQuizId,
                                        QuizStageDto(newQuizStageId, nextStageOrderNumber, newQuizStageDescription)
                                    )
                                }.onSuccess {
                                    logger.info { "New quiz stage created in quiz '${props.parentQuizId}'" }
                                    navigate(nav, props.parentQuizId, newQuizStageId)
                                    props.shortMessageTrigger.showSuccessMsg("Neue Quiz Seite erstellt")
                                    props.onClose()
                                }.onFailure {
                                    logger.error(it) { "Failed to create stage on quiz '${props.parentQuizId}'" }
                                    showCreationFailure()
                                }
                            }
                        }
                    }
                    +"Erstellen"
                }
                Button {
                    onClick = { props.onClose() }
                    +"Abbrechen"
                }
            }
        }
    }
}