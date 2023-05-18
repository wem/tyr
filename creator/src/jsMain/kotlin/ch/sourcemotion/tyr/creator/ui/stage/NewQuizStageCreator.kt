package ch.sourcemotion.tyr.creator.ui.stage

import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.ext.centeredGridElements
import ch.sourcemotion.tyr.creator.ui.ext.rowFlow
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
    var quizId: Uuid
    var show: Boolean
    var onClose: () -> Unit
    var onFailure: (message: String) -> Unit
}

private val logger = KotlinLogging.logger("NewQuizStageCreator")

val NewQuizStageCreator = FC<NewQuizStageCreatorProps> { props ->

    val nav = useNavigate()

    var openNewQuizStageDialog by useState(false)

    var newQuizStageDescription by useState("")
    var newQuizStageNumber by useState(1)
    var newQuizStageNumberValidationMessage by useState<String>()

    val loadQuizStages = { onFailure: (Throwable) -> Unit, requester: (List<QuizStageDto>) -> Unit ->
        launch {
            runCatching { rest.stages.getAll(props.quizId) }
                .onSuccess { requester(it) }
                .onFailure { failure -> onFailure(failure) }
        }
    }

    useEffect {
        openNewQuizStageDialog = props.show
        if (props.show) {
            if (newQuizStageNumber == 1) {
                loadQuizStages(
                    { props.onFailure("Fehler beim ermitteln der nächsten Seitenumber") }) { existingStages ->
                    val lastSideNumber = existingStages.map { stage -> stage.number }.sorted()
                    logger.info { "Last side number '${lastSideNumber.last()}'" }
                    newQuizStageNumber = lastSideNumber.last() + 1
                }
            }
        }
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

                    label = ReactNode("Seitennummer")
                    required = true
                    error = newQuizStageNumberValidationMessage != null
                    variant = FormControlVariant.outlined
                    type = InputType.number
                    value = newQuizStageNumber
                    InputLabelProps = jso { shrink = true }
                    helperText = ReactNode(newQuizStageNumberValidationMessage)
                    onChange = {
                        val enteredSideNumber: Int = it.target.asDynamic().value
                        if (enteredSideNumber <= 0) {
                            newQuizStageNumberValidationMessage = "Die Seitennummern müssen bei '1' beginnen"
                        } else {
                            loadQuizStages({ props.onFailure("Fehler beim Laden der Validierungsdaten") }) { existingStages ->

                                if (existingStages.isEmpty()) {
                                    newQuizStageNumberValidationMessage = null
                                    newQuizStageNumber = enteredSideNumber
                                    return@loadQuizStages
                                }

                                val alreadyAssignedSideNumberStrings = existingStages.map { stage -> "${stage.number}" }
                                val alreadyAssigned = alreadyAssignedSideNumberStrings.contains("$enteredSideNumber")

                                if (!alreadyAssigned) {
                                    newQuizStageNumberValidationMessage = null
                                    newQuizStageNumber = enteredSideNumber
                                } else {
                                    newQuizStageNumberValidationMessage =
                                        "Seitennummer bereits vergeben, bitte eine andere wählen."
                                }
                            }
                        }
                        newQuizStageNumber = enteredSideNumber
                    }
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
                    disabled = newQuizStageNumber == 0 || newQuizStageNumberValidationMessage != null
                    onClick = {
                        val newQuizStageId = uuid4()
                        launch {
                            runCatching {
                                rest.stages.put(
                                    props.quizId,
                                    QuizStageDto(newQuizStageId, newQuizStageNumber, newQuizStageDescription)
                                )
                            }
                                .onSuccess { navigate(nav, props.quizId, newQuizStageId) }
                                .onFailure {
                                    logger.error(it) { "Failed to create stage on quiz '${props.quizId}'" }
                                    props.onFailure("Fehler beim erstellen der Quizseite. Bitte noch einmal versuchen.")
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