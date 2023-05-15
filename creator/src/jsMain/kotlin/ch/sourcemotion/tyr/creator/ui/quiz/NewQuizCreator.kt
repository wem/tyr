package ch.sourcemotion.tyr.creator.ui.quiz

import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.navigate
import ch.sourcemotion.tyr.creator.ui.rest.rest
import com.benasher44.uuid.uuid4
import js.core.jso
import kotlinx.datetime.LocalDate
import mu.KotlinLogging
import mui.material.*
import mui.system.sx
import react.*
import react.dom.onChange
import react.router.useNavigate
import web.cssom.px
import web.html.InputType

external interface NewQuizCreatorProps : Props {
    var show: Boolean
    var onClose: () -> Unit
}

private val logger = KotlinLogging.logger("NewQuizStarter")

val NewQuizCreator = FC<NewQuizCreatorProps> { props ->

    val nav = useNavigate()

    var openNewQuizDialog by useState(false)
    var newQuizDateRaw by useState("")

    useEffect {
        openNewQuizDialog = props.show
    }

    Dialog {
        open = openNewQuizDialog
        onClose = { _, _ ->
            props.onClose()
        }

        DialogTitle {
            +"Neues Quiz erstellen"
        }
        DialogContent {
            Box {
                sx {
                    paddingBottom = 8.px
                    paddingTop = 8.px
                }
                TextField {
                    id = "new-quiz-date"
                    label = ReactNode("Quiz Datum")
                    variant = FormControlVariant.outlined
                    type = InputType.date
                    value = newQuizDateRaw
                    InputLabelProps = jso { shrink = true }
                    onChange = {
                        newQuizDateRaw = it.target.asDynamic().value
                    }
                }
            }
            DialogActions {
                Button {
                    onClick = {
                        val newQuizId = uuid4()
                        try {
                            val newQuizDate = LocalDate.parse(newQuizDateRaw)
                            launch {
                                runCatching { rest.quizzes.put(QuizDto(newQuizId, newQuizDate)) }
                                    .onSuccess {
                                        println("Nav to new quiz")

                                        props.onClose()
                                        navigate(nav, newQuizId)
                                    }.onFailure { logger.error(it) { "Unable to create new quiz" } }
                            }
                        } catch (failure: Exception) {
                            logger.error(failure) { "Failed to parse date of new quiz" }
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