package ch.sourcemotion.tyr.creator.ui.quiz

import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
import ch.sourcemotion.tyr.creator.dto.QuizStageDto
import ch.sourcemotion.tyr.creator.ui.global.Confirm
import ch.sourcemotion.tyr.creator.ui.global.ConfirmSpec
import mui.icons.material.Delete
import mui.icons.material.Edit
import mui.material.*
import mui.material.styles.Theme
import mui.material.styles.TypographyVariant
import mui.material.styles.useTheme
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useState
import web.cssom.Display
import web.cssom.FlexWrap
import web.cssom.JustifyContent

external interface QuizStageOverviewProps : Props {
    var quizStage: QuizStageDto
    var stageNumber: Int
    var onStageChosen: (QuizStageDto) -> Unit
    var onCategoryChosen: (QuizCategoryDto) -> Unit
    var onDelete: (QuizStageDto) -> Unit
}

val QuizStageOverview = FC<QuizStageOverviewProps> { props ->

    val theme = useTheme<Theme>()
    var showDeletionConfirm by useState(false)

    Paper {
        elevation = 1
        sx {
            padding = theme.spacing(2)
        }

        Box {
            sx {
                display = Display.grid
                rowGap = theme.spacing(2)
            }

            Box {
                sx {
                    display = Display.flex
                    columnGap = theme.spacing(1)
                    rowGap = theme.spacing(1)
                    flexWrap = FlexWrap.wrap
                }

                props.quizStage.categories.forEach { category ->
                    Tooltip {
                        title = ReactNode("Kategorie bearbeiten")

                        Chip {
                            label = ReactNode(category.title)
                            color = ChipColor.info

                            onClick = {
                                props.onCategoryChosen(category)
                            }
                        }
                    }
                }
            }

            Divider {
                Chip {
                    label = ReactNode("Seitenbeschreibung")
                }
            }

            Typography {
                variant = TypographyVariant.body2

                val description = if (props.quizStage.description?.isNotEmpty() == true) {
                    props.quizStage.description
                } else "Keine Beschreibung"
                +description
            }

            Divider {
                Chip {
                    label = ReactNode("Aktionen")
                }
            }

            Box {
                sx {
                    display = Display.flex
                    columnGap = theme.spacing(1)
                    justifyContent = JustifyContent.end
                }

                Tooltip {
                    title = ReactNode("Seite bearbeiten")

                    Fab {
                        size = Size.medium
                        color = FabColor.info
                        Edit()

                        onClick = {
                            props.onStageChosen(props.quizStage)
                        }
                    }
                }

                Tooltip {
                    title = ReactNode("Seite löschen")

                    Fab {
                        size = Size.medium
                        color = FabColor.warning
                        Delete()

                        onClick = {
                            showDeletionConfirm = true
                        }
                    }
                }
            }
        }
    }

    Confirm {
        spec = ConfirmSpec(
            showDeletionConfirm,
            "Quiz Seite wirklich löschen?",
            "Soll die Quiz Seite '${props.stageNumber}' wirklich gelöscht werden?"
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