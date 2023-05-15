package ch.sourcemotion.tyr.creator.ui.quiz

import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.ui.global.Confirm
import ch.sourcemotion.tyr.creator.ui.global.ConfirmSpec
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.useState
import web.cssom.TextAlign

external interface QuizStageCardProps : Props {
    var quizStage: QuizStageDto
    var onDelete : (QuizStageDto) -> Unit
}

val QuizStageCard = FC<QuizStageCardProps>() { props ->

    var showDeletionConfirm by useState(false)

    Card {
        CardActionArea {
            Typography {
                sx {
                    textAlign = TextAlign.center
                }
                variant = TypographyVariant.h3

                +"${props.quizStage.number}"
            }
            CardContent {
                Typography {
                    +"Kategorien: ${props.quizStage.categories.size}"
                }
                Typography {
                    +"Beschreibung: ${props.quizStage.description ?: "Keine"}"
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
            "Quiz Seite wirklich löschen?",
            "Soll die Quiz Seite mit der Nummer '${props.quizStage.number}' wirklich gelöscht werden?"
        )

        onCancel = {
            showDeletionConfirm = false
        }

        onConfirm = {
            showDeletionConfirm = false
            props.onDelete(props.quizStage)
        }
    }
}