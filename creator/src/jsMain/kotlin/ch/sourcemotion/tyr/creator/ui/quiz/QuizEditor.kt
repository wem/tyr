package ch.sourcemotion.tyr.creator.ui.quiz

import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.ext.centeredGridElements
import ch.sourcemotion.tyr.creator.ui.ext.rowFlow
import ch.sourcemotion.tyr.creator.ui.global.*
import ch.sourcemotion.tyr.creator.ui.global.FabKind.*
import ch.sourcemotion.tyr.creator.ui.global.ShortMessageSeverity.SUCCESS
import ch.sourcemotion.tyr.creator.ui.quizIfOf
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
import react.router.useParams
import web.cssom.pct
import web.cssom.px
import web.html.InputType

private val logger = KotlinLogging.logger("QuizEditor")

val QuizEditor = FC<Props> {

    val params = useParams()
    var quiz by useState<QuizDto>()
    var loading by useState(true)

    var showNewStageCreator by useState(false)

    var pendingAlerts by useState(listOf<AlertSpec>())
    var currentShortMessage by useState<ShortMessageSpec>()

    val currentQuizId = { quizIfOf(params) }

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
            showError(
                LOAD_FAILURE_TITLE,
                "Quiz konnte nicht geladen werden, vielleicht existiert es nicht mehr?. Versuche es noch einmal zu öffnen."
            )
        }) {
            quiz = rest.quizzes.get(quizId, withStages = true, withCategories = true)
            currentShortMessage = ShortMessageSpec("Quiz erfolgreich geladen / zurückgesetzt", SUCCESS)
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

    ProcessingOverlay {
        show = loading
    }

    Grid {
        container = true
        sx {
            width = 80.pct
            rowFlow()
            centeredGridElements()
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

            Grid {
                container = true
                sx {
                    centeredGridElements()
                    width = 100.pct
                }
                Typography {
                    variant = TypographyVariant.h2
                    +"Seiten"
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
                        onDelete = { stageToDelete ->
                            launch {
                                runCatching { rest.stages.delete(stageToDelete.id) }
                                    .onSuccess {
                                        // We show the deletion success independent of the quiz reload success or fail
                                        currentShortMessage = ShortMessageSpec("Quiz Seite '${stageToDelete.number}' " +
                                                "erfolgreich gelöscht", SUCCESS)

                                        runCatching { quiz = rest.quizzes.get(loadedQuiz.id, withStages = true, withCategories = true) }
                                            .onFailure {
                                                showError(
                                                    LOAD_FAILURE_TITLE,
                                                    "Quiz konnte nach dem löschen der Seite nicht aktualisiert werden. Deine Ansicht ist nicht mehr aktuell. " +
                                                            "Bitte lade die App neu."
                                                )
                                            }
                                    }.onFailure {
                                        showError(
                                            "Fehler beim löschen der Seite '${stageToDelete.number}'",
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
            showError("Fehler beim erstellen der Quizseite", msg)
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
            FabSpec("Quiz zurücksetzen", FabColor.warning, RESET) { loadQuiz() },
            FabSpec("Quiz Seite erstellen", FabColor.inherit, NEW) {
                showNewStageCreator = true
            },
        )
    }
}