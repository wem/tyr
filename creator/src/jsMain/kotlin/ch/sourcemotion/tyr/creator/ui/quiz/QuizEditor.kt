package ch.sourcemotion.tyr.creator.ui.quiz

import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.ui.OutletContextParams
import ch.sourcemotion.tyr.creator.ui.coroutine.executeExceptionHandled
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.ext.centeredGridElements
import ch.sourcemotion.tyr.creator.ui.global.*
import ch.sourcemotion.tyr.creator.ui.global.FabKind.*
import ch.sourcemotion.tyr.creator.ui.navigate
import ch.sourcemotion.tyr.creator.ui.quizIdOf
import ch.sourcemotion.tyr.creator.ui.rest.rest
import ch.sourcemotion.tyr.creator.ui.stage.NewQuizStageCreator
import js.core.jso
import kotlinx.datetime.LocalDate
import mu.KotlinLogging
import mui.material.*
import mui.material.styles.Theme
import mui.material.styles.TypographyVariant
import mui.material.styles.useTheme
import mui.system.responsive
import mui.system.sx
import react.*
import react.dom.onChange
import react.router.useNavigate
import react.router.useOutletContext
import react.router.useParams
import web.html.InputType

private val logger = KotlinLogging.logger("QuizEditor")

val QuizEditor = FC<Props> {

    val theme = useTheme<Theme>()
    val nav = useNavigate()
    val params = useParams()
    val (globalMsgTrigger, shortMsgTrigger) = useOutletContext<OutletContextParams>()

    var quiz by useState<QuizDto>()

    var showNewStageCreator by useState(false)

    val currentQuizId = { quizIdOf(params) }

    val loadQuiz = {
        val quizId = currentQuizId()
        executeExceptionHandled({ failure ->
            logger.error(failure) { "Failed to load quiz '$quizId'" }
            globalMsgTrigger.showError(
                LOAD_FAILURE_TITLE,
                "Quiz konnte nicht geladen werden, vielleicht existiert es nicht mehr?. Versuche es noch einmal zu öffnen."
            )
        }) {
            quiz = rest.quizzes.get(quizId, withStages = true, withCategories = true)
            shortMsgTrigger.showSuccessMsg("Quiz erfolgreich geladen / zurückgesetzt")
        }
    }

    useEffectOnce {
        logger.debug { "Quiz edit for quiz '${currentQuizId()} loaded'" }
        loadQuiz()
    }

    // As the new quiz creator is also rendered in that scope we have to check the quiz to edit has switched
    // to a recently new created one.
    useEffect {
        if (quiz?.id != currentQuizId()) {
            loadQuiz()
        }
    }

    Grid {
        id = "quiz-edit-container"
        container = true
        direction = responsive(GridDirection.row)
        sx {
            centeredGridElements()
            rowGap = theme.spacing(2)
        }

        quiz?.let { loadedQuiz ->
            Grid {
                item = true
                TextField {
                    label = ReactNode("Quiz Datum")
                    variant = FormControlVariant.outlined
                    type = InputType.date
                    value = loadedQuiz.date.toString()
                    InputLabelProps = jso {
                        shrink = true
                    }
                    onChange = {
                        quiz = quiz?.copy(date = LocalDate.parse(it.target.asDynamic().value))
                    }
                }
            }

            Grid {
                item = true
                xs = 12
                Divider {
                    Typography {
                        variant = TypographyVariant.h4
                        +"Seiten"
                    }
                }
            }

            loadedQuiz.stages.sortedBy { it.orderNumber }.forEachIndexed { idx, stage ->
                val stageNumber = idx + 1

                Grid {
                    item = true
                    QuizStageCard {
                        this.stageNumber = stageNumber
                        quizStage = stage
                        onChosen = {
                            navigate(nav, loadedQuiz.id, stage.id)
                        }
                        onDelete = { stageToDelete ->
                            launch {
                                runCatching { rest.stages.delete(stageToDelete.id) }
                                    .onSuccess {
                                        // We show the deletion success independent of the quiz reload success or fail
                                        shortMsgTrigger.showSuccessMsg(
                                            "Quiz Seite '$stageNumber' erfolgreich gelöscht"
                                        )

                                        runCatching {
                                            quiz = rest.quizzes.get(
                                                loadedQuiz.id,
                                                withStages = true,
                                                withCategories = true
                                            )
                                        }
                                            .onFailure {
                                                globalMsgTrigger.showError(
                                                    LOAD_FAILURE_TITLE,
                                                    "Quiz konnte nach dem Löschen der Seite nicht aktualisiert werden. Deine Ansicht ist nicht mehr aktuell. " +
                                                            "Bitte lade die App neu."
                                                )
                                            }
                                    }.onFailure {
                                        globalMsgTrigger.showError(
                                            DELETE_FAILURE_TITLE,
                                            "Quiz Seite '$stageNumber' konnte nicht gelöscht werden. Versuche es noch einmal."
                                        )
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    NewQuizStageCreator {
        show = showNewStageCreator
        parentQuizId = currentQuizId()
        globalMessageTrigger = globalMsgTrigger
        shortMessageTrigger = shortMsgTrigger
        onClose = { showNewStageCreator = false }
    }

    FloatingButtons {
        fabs = listOf(
            FabSpec("Quiz speichern", FabColor.success, SAVE) {
                quiz?.let { quizToSave ->
                    executeExceptionHandled({ failure ->
                        logger.error(failure) { "Failed to save quiz '$${quizToSave.id}'" }
                        globalMsgTrigger.showError(
                            "Fehler beim Speichern", "Quiz konnte nicht gespeichert werden. Versuche es noch einmal."
                        )
                    }) {
                        rest.quizzes.put(quizToSave)
                        shortMsgTrigger.showSuccessMsg("Quiz erfolgreich gespeichert")
                    }
                }
            },
            FabSpec("Quiz zurücksetzen", FabColor.warning, RESET) { loadQuiz() },
            FabSpec("Quiz Seite erstellen", FabColor.inherit, NEW) {
                showNewStageCreator = true
            },
        )
    }
}