package ch.sourcemotion.tyr.creator.ui.category

import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.ui.OutletContextParams
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.ext.centeredGridElements
import ch.sourcemotion.tyr.creator.ui.global.*
import ch.sourcemotion.tyr.creator.ui.quizCategoryIdOf
import ch.sourcemotion.tyr.creator.ui.quizIdOf
import ch.sourcemotion.tyr.creator.ui.quizStageIfOf
import ch.sourcemotion.tyr.creator.ui.rest.rest
import mu.KotlinLogging
import mui.material.Divider
import mui.material.Grid
import mui.material.GridDirection
import mui.material.Typography
import mui.material.styles.Theme
import mui.material.styles.TypographyVariant
import mui.material.styles.useTheme
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.router.useOutletContext
import react.router.useParams
import react.useEffectOnce
import react.useState

private val logger = KotlinLogging.logger("QuizCategoryEditor")

val QuizCategoryEditor = FC<Props> {

    val theme = useTheme<Theme>()
    val params = useParams()
    val (globalMsgTrigger, shortMsgTrigger) = useOutletContext<OutletContextParams>()

    val parentQuizId = quizIdOf(params)
    val parentStageId = quizStageIfOf(params)
    val categoryId = quizCategoryIdOf(params)

    var category by useState<QuizCategoryDto>()

    var titleValidation by useState(
        TyrTextFieldValidation(
            true,
            "Titel muss gesetzt sein",
            true,
            NotEmptyTyrTextFieldValidator
        )
    )
    var contextValidation by useState(
        TyrTextFieldValidation(
            true,
            "Kontext / Kategorie-Frage muss gesetzt sein",
            true,
            NotEmptyTyrTextFieldValidator
        )
    )

    val loadQuizCategory = {
        launch {
            runCatching { rest.categories.get(categoryId) }
                .onSuccess {
                    category = it
                    shortMsgTrigger.showSuccessMsg("Quiz Kategorie erfolgreich geladen / zurückgesetzt")
                    logger.debug { "Quiz category '${categoryId} loaded'" }
                }.onFailure { failure ->
                    logger.error(failure) { "Failed to load quiz category '$categoryId'" }
                    globalMsgTrigger.showError(
                        LOAD_FAILURE_TITLE,
                        "Quiz Kategorie konnte nicht geladen werden, vielleicht existiert sie nicht mehr?. Versuche es noch einmal zu öffnen."
                    )
                }
        }
    }

    useEffectOnce {
        loadQuizCategory()
    }

    category?.let { loadedCategory ->
        Grid {
            id = "quiz-category-edit-container"
            container = true
            direction = responsive(GridDirection.row)
            sx {
                centeredGridElements()
                rowGap = theme.spacing(2)
                columnGap = theme.spacing(2)
            }

            Grid {
                item = true
                xs = 3
                TyrTextField {
                    title = "Titel"
                    fullWidth = true
                    validation = titleValidation
                    type = TyrTextFieldType.TEXT
                    value = loadedCategory.title
                    onNewValue = { newTitle, isValid ->
                        titleValidation = titleValidation.copy(valid = isValid)
                        category = loadedCategory.copy(title = newTitle)
                    }
                }
            }

            Grid {
                item = true
                xs = 4
                TyrTextField {
                    title = "Kontext / Kategorie-Frage"
                    fullWidth = true
                    validation = contextValidation
                    type = TyrTextFieldType.TEXT
                    value = loadedCategory.contextOrQuestionText.text
                    onNewValue = { newContext, isValid ->
                        contextValidation = contextValidation.copy(valid = isValid)
                        category = loadedCategory.copy(
                            contextOrQuestionText = loadedCategory.contextOrQuestionText.copy(text = newContext)
                        )
                    }
                }
            }

            Grid {
                item = true
                xs = 4
                TyrTextField {
                    title = "Kontext / Kategorie-Fragen Beschreibung"
                    fullWidth = true
                    type = TyrTextFieldType.TEXT
                    value = loadedCategory.contextOrQuestionText.description ?: ""
                    onNewValue = { newDescription, _ ->
                        category = loadedCategory.copy(
                            contextOrQuestionText = loadedCategory.contextOrQuestionText.copy(description = newDescription)
                        )
                    }
                }
            }

            Grid {
                item = true
                xs = 12
                Divider {
                    Typography {
                        variant = TypographyVariant.h4
                        +"Fragen"
                    }
                }
            }
        }
    }
}