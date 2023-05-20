package ch.sourcemotion.tyr.creator.ui.stage

import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.ui.OutletContextParams
import ch.sourcemotion.tyr.creator.ui.category.NewQuizCategoryCreator
import ch.sourcemotion.tyr.creator.ui.coroutine.executeExceptionHandled
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.ext.centeredGridElements
import ch.sourcemotion.tyr.creator.ui.global.*
import ch.sourcemotion.tyr.creator.ui.navigate
import ch.sourcemotion.tyr.creator.ui.quizIdOf
import ch.sourcemotion.tyr.creator.ui.quizStageIfOf
import ch.sourcemotion.tyr.creator.ui.rest.rest
import js.core.jso
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

private val logger = KotlinLogging.logger("QuizStageEditor")

val QuizStageEditor = FC<Props> {

    val theme = useTheme<Theme>()
    val nav = useNavigate()
    val params = useParams()
    val (globalMsgTrigger, shortMsgTrigger) = useOutletContext<OutletContextParams>()

    val parentQuizId = quizIdOf(params)
    val quizStageId = quizStageIfOf(params)

    var quizStage by useState<QuizStageDto>()

    var showNewCategoryCreator by useState(false)

    val loadQuizStage = {
        executeExceptionHandled({ failure ->
            logger.error(failure) { "Failed to load quiz stage '$quizStage'" }
            globalMsgTrigger.showError(
                LOAD_FAILURE_TITLE,
                "Quiz Seite konnte nicht geladen werden, vielleicht existiert es nicht mehr?. Versuche es noch einmal zu öffnen."
            )
        }) {
            quizStage = rest.stages.get(quizStageId, withCategories = true)
            logger.debug { "Quiz stage '${quizStageId} loaded'" }
        }
    }

    useEffectOnce {
        loadQuizStage()
    }

    Grid {
        id = "quiz-stage-edit-container"
        container = true
        direction = responsive(GridDirection.row)
        sx {
            centeredGridElements()
            rowGap = theme.spacing(2)
            columnGap = theme.spacing(2)
        }

        quizStage?.let { loadedQuizStage ->
            Grid {
                item = true
                xs = 3
                TextField {
                    fullWidth = true
                    label = ReactNode("Seiten-Beschreibung")
                    variant = FormControlVariant.outlined
                    type = InputType.text
                    value = loadedQuizStage.description
                    InputLabelProps = jso {
                        shrink = true
                    }
                    onChange = {
                        val value: String? = it.target.asDynamic().value
                        val description = if (value?.isNotEmpty() == true) {
                            value
                        } else null
                        quizStage = quizStage?.copy(description = description)
                    }
                }
            }

            Grid {
                item = true
                xs = 12
                Divider {
                    Typography {
                        variant = TypographyVariant.h4
                        +"Kategorien"
                    }
                }
            }

            loadedQuizStage.categories.sortedBy { it.orderNumber }.forEachIndexed { idx, category ->
                val categoryNumber = idx + 1
                Grid {
                    item = true
                    xs = 2
                    QuizCategoryOverview {
                        quizCategory = category
                        this.categoryNumber = categoryNumber
                        onCategoryChosen = { navigate(nav, parentQuizId, quizStageId, category.id) }
                        onDeleteCategory = {
                            launch {
                                runCatching { rest.categories.delete(category.id) }
                                    .onSuccess {
                                        // We show the deletion success independent of quiz stage reload success or fail
                                        shortMsgTrigger.showSuccessMsg(
                                            "Kateogorie '${category.title}' erfolgreich gelöscht"
                                        )

                                        runCatching {
                                            quizStage = rest.stages.get(
                                                loadedQuizStage.id,
                                                withCategories = true
                                            )
                                        }.onFailure {
                                            globalMsgTrigger.showError(
                                                LOAD_FAILURE_TITLE,
                                                "Quiz Seite konnte nach dem Löschen der Kategories nicht aktualisiert werden. " +
                                                        "Deine Ansicht ist nicht mehr aktuell. Bitte lade die App neu."
                                            )
                                        }
                                    }.onFailure {
                                        globalMsgTrigger.showError(
                                            DELETE_FAILURE_TITLE,
                                            "Kategorie '${category.title}' konnte nicht gelöscht werden. Versuche es noch einmal."
                                        )
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    FloatingButtons {
        fabs = listOf(
            FabSpec("Quiz Seite speichern", FabColor.success, FabKind.SAVE) {
                quizStage?.let { quizStageToSave ->
                    executeExceptionHandled({ failure ->
                        logger.error(failure) { "Failed to save quiz stage '$${quizStageToSave.id}'" }
                        globalMsgTrigger.showError(
                            SAVE_FAILURE_TITLE, "Quiz Seite konnte nicht gespeichert werden. Versuche es noch einmal."
                        )
                    }) {
                        rest.stages.put(parentQuizId, quizStageToSave)
                        shortMsgTrigger.showSuccessMsg("Quiz Seite erfolgreich gespeichert")
                    }
                }
            },
            FabSpec("Quiz Seite zurücksetzen", FabColor.warning, FabKind.RESET) {
                loadQuizStage()
            },
            FabSpec("Kategorie erstellen", FabColor.inherit, FabKind.NEW) {
                showNewCategoryCreator = true
            },
        )
    }
    NewQuizCategoryCreator {
        this.parentQuizId = parentQuizId
        parentQuizStageId = quizStageId
        show = showNewCategoryCreator
        globalMessageTrigger = globalMsgTrigger
        shortMessageTrigger = shortMsgTrigger
        onClose = {
            showNewCategoryCreator = false
        }
    }
}