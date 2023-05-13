package ch.sourcemotion.tyr.creator.ui.global

import ch.sourcemotion.tyr.creator.ui.quiz.NewQuizCreator
import mui.icons.material.Edit
import mui.icons.material.FileOpen
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.useState
import web.cssom.px

val MainBar = FC<Props> {

    var showNewQuizStarter by useState(false)

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
                    sx { marginLeft = 16.px }
                    color = IconButtonColor.inherit

                    SpeedDialIcon()

                    onClick = {
                        showNewQuizStarter = true
                    }
                }
            }

            Tooltip {
                title = ReactNode("Bestehendes Quiz bearbeiten")

                IconButton {
                    sx { marginLeft = 8.px }

                    color = IconButtonColor.inherit

                    Edit()
                }
            }

            Tooltip {
                title = ReactNode("Dateien bearbeiten (Bilder, Filme, Musik)")

                IconButton {
                    sx { marginLeft = 8.px }

                    color = IconButtonColor.inherit

                    FileOpen()
                }
            }
        }
    }

    NewQuizCreator {
        show = showNewQuizStarter
        onClose = {
            showNewQuizStarter = false
        }
    }
}