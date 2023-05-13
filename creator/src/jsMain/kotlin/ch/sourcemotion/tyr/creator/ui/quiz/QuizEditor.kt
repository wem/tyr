package ch.sourcemotion.tyr.creator.ui.quiz

import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.ui.QUIZ_NAV_PARAM
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.global.*
import ch.sourcemotion.tyr.creator.ui.global.FabKind.*
import ch.sourcemotion.tyr.creator.ui.global.ShortMessageSeverity.SUCCESS
import ch.sourcemotion.tyr.creator.ui.rest.rest
import com.benasher44.uuid.uuidFrom
import js.core.jso
import kotlinx.datetime.LocalDate
import mu.KotlinLogging
import mui.material.*
import mui.system.sx
import react.*
import react.dom.onChange
import react.router.useParams
import web.cssom.*
import web.html.InputType

private val logger = KotlinLogging.logger("QuizEditor")

val QuizEditor = FC<Props> {

    val params = useParams()
    var quiz by useState<QuizDto>()
    var loading by useState(true)

    var pendingAlerts by useState(listOf<AlertSpec>())
    var currentShortMessage by useState<ShortMessageSpec>()

    val currentQuizId = { uuidFrom("${params[QUIZ_NAV_PARAM]}") }

    val showError = { title: String, description: String ->
        pendingAlerts = pendingAlerts + AlertSpec(title, description, AlertColor.error) { acknowledgedAlert ->
            pendingAlerts = pendingAlerts.toMutableList().apply { remove(acknowledgedAlert) }.toList()
            println(pendingAlerts.size)
        }
    }

    val executeWithLoader = { onFailure: (Throwable) -> Unit, block: suspend () -> Unit ->
        launch {
            loading = true
            runCatching {
                block()
            }.onFailure { onFailure(it) }
            loading = false
        }
    }

    val loadQuiz = {
        val quizId = currentQuizId()
        executeWithLoader({ failure ->
            logger.error(failure) { "Failed to load quiz '$quizId'" }
            showError("Fehler beim Laden", "Quiz konnte nicht geladen werden, vielleicht existiert es nicht mehr?. Versuche es noch einmal zu öffnen.")
        }) {
            quiz = rest.quizzes.get(quizId)
            currentShortMessage = ShortMessageSpec("Quiz erfolgreich geladen / zurückgesetzt", SUCCESS)
        }
    }

    useEffectOnce {
        loadQuiz()
    }

    // As the new quiz creator is also rendered in that scope we have to check the quiz to edit has switched
    // to a recently new created one.
    useEffect {
        if (quiz?.id != currentQuizId()) {
            loadQuiz()
        }
    }

    ProcessingOverlay {
        show = loading
    }

    Grid {
        container = true
        sx {
            width = 80.pct
            gridAutoFlow = GridAutoFlow.row
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
            rowGap = 16.px
        }

        Alerts {
            alerts = pendingAlerts
        }

        ShortMessage {
            messageSpec = currentShortMessage
            onClose = {
                currentShortMessage = null
            }
        }

        if (quiz != null) {
            TextField {
                id = "quiz-date"
                label = ReactNode("Quiz Datum")
                variant = FormControlVariant.outlined
                type = InputType.date
                value = quiz?.date?.toString() ?: ""
                InputLabelProps = jso {
                    shrink = true
                }
                onChange = {
                    quiz = quiz?.copy(date = LocalDate.parse(it.target.asDynamic().value))
                }
            }
        }
    }


    FloatingButtons {
        fabs = listOf(
            FabSpec("Quiz speichern", FabColor.success, SAVE) {
                quiz?.let { quizToSave ->
                    executeWithLoader({ failure ->
                        logger.error(failure) { "Failed to save quiz '$${quizToSave.id}'" }
                        showError(
                            "Fehler beim Speichern", "Quiz konnte nicht gespeichert werden. Versuche es noch einmal."
                        )
                    }) {
                        rest.quizzes.put(quizToSave)
                        currentShortMessage = ShortMessageSpec("Quiz erfolgreich gespeichert", SUCCESS)
                    }
                }
            },
            FabSpec("Quiz zurücksetzen", FabColor.warning, RESET) {
                loadQuiz()
                showError("Fehler beim Laden", "Quiz konnte nicht geladen werden. Versuche es noch einmal zu öffnen.")
            },
            FabSpec("Quiz Seite erstellen", FabColor.inherit, NEW) {
                quiz?.let { quizToSave ->
                    executeWithLoader({ failure -> logger.error(failure) { "Failed to save quiz '$${quizToSave.id}'" } }) {
                        rest.quizzes.put(quizToSave)
                    }
                }
            },
        )
    }
}