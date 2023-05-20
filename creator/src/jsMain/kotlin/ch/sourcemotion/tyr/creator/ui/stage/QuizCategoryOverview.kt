package ch.sourcemotion.tyr.creator.ui.stage

import ch.sourcemotion.tyr.creator.dto.QuizCategoryDto
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
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.JustifyContent

external interface QuizCategoryOverviewProps : Props {
    var quizCategory: QuizCategoryDto
    var categoryNumber: Int
    var onCategoryChosen: () -> Unit
    var onDeleteCategory: () -> Unit
}

val QuizCategoryOverview = FC<QuizCategoryOverviewProps> { props ->

    val theme = useTheme<Theme>()
    var showDeletionConfirm by useState(false)

    Paper {

        val category = props.quizCategory
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
                    alignItems = AlignItems.center
                    columnGap = theme.spacing(1)
                }
                Typography {
                    variant = TypographyVariant.h4

                    +"${props.quizCategory.questions.size}"
                }
                Typography {
                    variant = TypographyVariant.h5

                    +"Fragen"
                }
            }

            Divider {
                Chip {
                    label = ReactNode("Title")
                }
            }

            Typography {
                variant = TypographyVariant.h6

                +category.title
            }

            Divider {
                Chip {
                    label = ReactNode("Kontext / Kategorie-Frage")
                }
            }

            Typography {
                variant = TypographyVariant.body2

                +category.contextOrQuestionText.text
            }

            Divider {
                Chip {
                    label = ReactNode("Kontext / Kategorie-Fragen Bemerkung")
                }
            }

            Typography {
                variant = TypographyVariant.body2

                val description = if (category.contextOrQuestionText.description?.isNotEmpty() == true) {
                    category.contextOrQuestionText.description
                } else "Keine Bemerkung"

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
                    title = ReactNode("Kategorie bearbeiten")

                    Fab {
                        size = Size.medium
                        color = FabColor.info
                        Edit()

                        onClick = {
                            props.onCategoryChosen()
                        }
                    }
                }

                Tooltip {
                    title = ReactNode("Kategorie löschen")

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
            "Quiz Kategorie wirklich löschen?",
            "Soll die Quiz Kategorie '${props.quizCategory.title}' wirklich gelöscht werden?"
        )

        onCancel = {
            showDeletionConfirm = false
        }

        onConfirm = {
            showDeletionConfirm = false
            props.onDeleteCategory()
        }
    }
}