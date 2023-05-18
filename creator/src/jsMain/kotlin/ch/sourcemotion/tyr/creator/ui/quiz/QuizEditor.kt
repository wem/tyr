package ch.sourcemotion.tyr.creator.ui.quiz

import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.ui.OutletContextParams
import ch.sourcemotion.tyr.creator.ui.coroutine.executeExceptionHandled
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.ext.centeredGridElements
import ch.sourcemotion.tyr.creator.ui.ext.rowFlow
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
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.onChange
import react.router.useNavigate
import react.router.useOutletContext
import react.router.useParams
import web.cssom.pct
import web.cssom.px
import web.html.InputType

private val logger = KotlinLogging.logger("QuizEditor")

val QuizEditor = FC<Props> {

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
        container = true
        sx {
            width = 80.pct
            rowFlow()
            centeredGridElements()
            rowGap = 16.px
        }

        quiz?.let { loadedQuiz ->
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

            Box {
                sx { width = 100.pct }
                Divider {
                    Typography {
                        variant = TypographyVariant.h4
                        +"Seiten"
                    }
                }
            }

            Grid {
                container = true
                sx {
                    rowFlow()
                    centeredGridElements()
                    rowGap = 16.px
                    columnGap = 16.px
                }
                loadedQuiz.stages.sortedBy { it.number }.forEach { stage ->
                    QuizStageCard {
                        quizStage = stage
                        onChosen = {
                            println("shjflskjfl")
                            navigate(nav, loadedQuiz.id, stage.id)
                        }
                        onDelete = { stageToDelete ->
                            launch {
                                runCatching { rest.stages.delete(stageToDelete.id) }
                                    .onSuccess {
                                        // We show the deletion success independent of the quiz reload success or fail
                                        shortMsgTrigger.showSuccessMsg(
                                            "Quiz Seite '${stageToDelete.number}' erfolgreich gelöscht"
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
                                            "Quiz Seite '${stageToDelete.number}' konnte nicht gelöscht werden. Versuche es noch einmal."
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
        quizId = currentQuizId()
        onClose = { showNewStageCreator = false }
        onFailure = { msg ->
            globalMsgTrigger.showError("Fehler beim erstellen der Quizseite", msg)
        }
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