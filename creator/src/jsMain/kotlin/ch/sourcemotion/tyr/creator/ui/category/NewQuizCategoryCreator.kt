package ch.sourcemotion.tyr.creator.ui.category

import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.dto.element.TextDto
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
import mui.material.styles.Theme
import mui.material.styles.useTheme
import mui.system.sx
import react.*
import react.dom.onChange
import react.router.useNavigate
import web.cssom.Auto
import web.cssom.Display
import web.cssom.array
import web.cssom.pct
import web.html.InputType

external interface NewQuizCategoryCreatorProps : Props {
    var parentQuizId: Uuid
    var parentQuizStageId: Uuid
    var show: Boolean
    var globalMessageTrigger: GlobalMessageTrigger
    var shortMessageTrigger: ShortMessageTrigger
    var onClose: () -> Unit
}

private val logger = KotlinLogging.logger("NewQuizStageCreator")

val NewQuizCategoryCreator = FC<NewQuizCategoryCreatorProps> { props ->

    val theme = useTheme<Theme>()
    val nav = useNavigate()

    var openNewQuizCategoryDialog by useState(false)

    var newQuizCategoryTitle by useState<String>()
    var newQuizCategoryTitleValidationMessage by useState<String>()

    var newQuizCategoryContextText by useState<String>()
    var newQuizCategoryContextTextValidationMessage by useState<String>()

    var newQuizCategoryContextDescription by useState<String>()

    var valid by useState(false)

    val updateValidity = {
        valid = newQuizCategoryTitleValidationMessage == null && newQuizCategoryContextTextValidationMessage == null
    }

    val showCreationFailure = {
        props.globalMessageTrigger.showError(
            SAVE_FAILURE_TITLE,
            "Kategorie konnte nicht erstellt werden. Bitte versuche es erneut."
        )
    }

    useEffect {
        openNewQuizCategoryDialog = props.show
    }

    Dialog {
        fullWidth = true
        open = openNewQuizCategoryDialog
        onClose = { _, _ ->
            props.onClose()
        }

        DialogTitle {
            +"Neue Quiz Kategorie erstellen"
        }
        DialogContent {
            Box {
                sx {
                    display = Display.grid
                    rowFlow()
                    centeredGridElements()
                    gridTemplateColumns = array(
                        100.pct,
                    )
                    gridTemplateRows = array(
                        Auto.auto,
                        Auto.auto,
                        Auto.auto,
                    )
                    paddingBottom = theme.spacing(1)
                    paddingTop = theme.spacing(1)
                    rowGap = theme.spacing(3)
                    width = 100.pct
                }

                TextField {
                    fullWidth = true
                    label = ReactNode("Title")
                    required = true
                    error = newQuizCategoryTitleValidationMessage != null
                    variant = FormControlVariant.outlined
                    type = InputType.text
                    value = newQuizCategoryTitle ?: ""
                    InputLabelProps = jso { shrink = true }
                    helperText = ReactNode(newQuizCategoryTitleValidationMessage)
                    onChange = {
                        val value: String = it.target.asDynamic().value
                        newQuizCategoryTitle = value
                        newQuizCategoryTitleValidationMessage = if (value.isEmpty()) {
                            "Kategorie Titel benötigt"
                        } else {
                            null
                        }
                        updateValidity()
                    }
                }

                TextField {
                    fullWidth = true
                    label = ReactNode("Kontext / Kategorie-Frage")
                    required = true
                    error = newQuizCategoryContextTextValidationMessage != null
                    variant = FormControlVariant.outlined
                    type = InputType.text
                    value = newQuizCategoryContextText ?: ""
                    InputLabelProps = jso { shrink = true }
                    helperText = ReactNode(newQuizCategoryContextTextValidationMessage)
                    onChange = {
                        val value: String = it.target.asDynamic().value
                        newQuizCategoryContextText = value
                        newQuizCategoryContextTextValidationMessage = if (value.isEmpty()) {
                            "Kategorie Kontext / Kategorie-Frage benötigt"
                        } else {
                            null
                        }
                        updateValidity()
                    }
                }

                TextField {
                    fullWidth = true
                    label = ReactNode("Kontext / Kategorie-Fragen Beschreibung")
                    variant = FormControlVariant.outlined
                    type = InputType.text
                    value = newQuizCategoryContextDescription ?: ""
                    InputLabelProps = jso { shrink = true }
                    onChange = {
                        val value: String? = it.target.asDynamic().value
                        newQuizCategoryContextDescription = if (value?.isNotEmpty() == true) {
                            value
                        } else ""
                    }
                }
            }
            DialogActions {
                Button {
                    disabled = !valid
                    onClick = {
                        val newQuizCategoryId = uuid4()
                        launch {
                            val nextCategoryOrderNumber = runCatching {
                                val existingCategories = rest.categories.getAll(props.parentQuizStageId)
                                if (existingCategories.isEmpty()) 0 else existingCategories.maxOf { it.orderNumber }
                            }.getOrElse {
                                logger.error(it) { "Failed to evaluate next category order number on quiz stage '${props.parentQuizStageId}'" }
                                showCreationFailure()
                                null
                            }

                            if (nextCategoryOrderNumber != null) {
                                runCatching {
                                    val newCategory = QuizCategoryDto(
                                        newQuizCategoryId,
                                        newQuizCategoryTitle!!,
                                        nextCategoryOrderNumber,
                                        TextDto(newQuizCategoryContextText!!, newQuizCategoryContextDescription)
                                    )
                                    rest.categories.put(props.parentQuizStageId, newCategory)
                                }.onSuccess {
                                    logger.info { "New quiz category '$newQuizCategoryTitle' created in quiz stage '${props.parentQuizStageId}'" }
                                    navigate(nav, props.parentQuizId, props.parentQuizStageId, newQuizCategoryId)
                                    props.shortMessageTrigger.showSuccessMsg("Neue Kategorie '$newQuizCategoryTitle' erstellt")
                                    props.onClose()
                                }.onFailure {
                                    logger.error(it) { "Failed to create category on quiz stage '${props.parentQuizStageId}'" }
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