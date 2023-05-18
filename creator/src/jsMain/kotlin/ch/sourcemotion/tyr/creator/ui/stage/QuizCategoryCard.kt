package ch.sourcemotion.tyr.creator.ui.stage

import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.ui.global.Confirm
import ch.sourcemotion.tyr.creator.ui.global.ConfirmSpec
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.useState
import web.cssom.TextAlign

external interface QuizCategoryCardProps : Props {
    var quizCategory: QuizCategoryDto
    var onDelete : (QuizCategoryDto) -> Unit
}

val QuizCategoryCard = FC<QuizCategoryCardProps> { props ->

    var showDeletionConfirm by useState(false)

    Card {
        CardActionArea {
            Typography {
                sx {
                    textAlign = TextAlign.center
                }
                variant = TypographyVariant.h3

                +"${props.quizCategory.number}"
            }
            CardContent {
                Typography {
                    +"Fragen: ${props.quizCategory.questions.size}"
                }
                Typography {
                    +"Title: ${props.quizCategory.title}"
                }
                Typography {
                    +"Kontext / Kategorie-Frage: ${props.quizCategory.contextOrQuestionText.text}"
                }
                props.quizCategory.contextOrQuestionText.description?.let { description ->
                    Typography {
                        +"Kontext / Kategorie-Fragen Beschreibung: $description"
                    }
                }
            }
        }
        CardActions {
            Button {
                variant = ButtonVariant.outlined
                +"Löschen"

                onClick = {
                    showDeletionConfirm = true
                }
            }
        }
    }

    Confirm {
        spec = ConfirmSpec(
            showDeletionConfirm,
            "Quiz Kategorie wirklich löschen?",
            "Soll die Quiz Kategorie '${props.quizCategory.title}' wirklich gelöscht werden?"
        )

        onCancel = {
            showDeletionConfirm = false
        }

        onConfirm = {
            showDeletionConfirm = false
            props.onDelete(props.quizCategory)
        }
    }
}