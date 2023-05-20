package ch.sourcemotion.tyr.creator.ui.global

import ch.sourcemotion.tyr.creator.ui.quiz.ExistingQuizChooser
import ch.sourcemotion.tyr.creator.ui.quiz.NewQuizCreator
import mui.icons.material.Edit
import mui.icons.material.FileOpen
import mui.material.*
import mui.material.styles.Theme
import mui.material.styles.TypographyVariant
import mui.material.styles.useTheme
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.useState

val MainBar = FC<Props> {

    val theme = useTheme<Theme>()
    var showNewQuizCreator by useState(false)
    var showExistingQuizChooser by useState(false)

    AppBar {
        position = AppBarPosition.static

        Toolbar {
            variant = ToolbarVariant.dense
            Typography {
                variant = TypographyVariant.h6
                noWrap = true
                component = div

                +"Pub Quiz Creator"
            }

            Tooltip {
                title = ReactNode("Neues Quiz erstellen")

                IconButton {
                    sx { marginLeft = theme.spacing(2) }
                    color = IconButtonColor.inherit

                    SpeedDialIcon()

                    onClick = {
                        showNewQuizCreator = true
                    }
                }
            }

            Tooltip {
                title = ReactNode("Bestehendes Quiz bearbeiten")

                IconButton {
                    sx { marginLeft = theme.spacing(1) }

                    color = IconButtonColor.inherit

                    Edit()

                    onClick = {
                        showExistingQuizChooser = true
                    }
                }
            }

            Tooltip {
                title = ReactNode("Dateien bearbeiten (Bilder, Filme, Musik)")

                IconButton {
                    sx {
                        marginLeft = theme.spacing(1)
                        marginRight = theme.spacing(4)
                    }

                    color = IconButtonColor.inherit

                    FileOpen()
                }
            }

            BreadCrumbs()
        }
    }

    // TODO Global / central failure visualisation

    NewQuizCreator {
        show = showNewQuizCreator
        onClose = {
            showNewQuizCreator = false
        }
    }

    ExistingQuizChooser {
        show = showExistingQuizChooser
        onClose = {
            showExistingQuizChooser = false
        }
    }
}