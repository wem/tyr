package ch.sourcemotion.tyr.creator.ui.global

import ch.sourcemotion.tyr.creator.ui.global.FabKind.*
import mui.icons.material.Done
import mui.icons.material.Redo
import mui.material.*
import mui.material.styles.Theme
import mui.material.styles.useTheme
import mui.system.sx
import react.FC
import react.Props
import web.cssom.AlignItems
import web.cssom.JustifyContent
import web.cssom.Position
import web.cssom.px



enum class FabKind {
    SAVE, RESET, NEW
}

data class FabSpec(
    val text: String,
    val color: FabColor,
    val kind: FabKind,
    val disabled: Boolean = false,
    val onClick: () -> Unit,
)

external interface FloatingButtonsProps : Props {
    var fabs: List<FabSpec>?
}

val FloatingButtons = FC<FloatingButtonsProps> { props ->
    val theme = useTheme<Theme>()

    Grid {
        container = true
        sx {
            position = Position.absolute
            bottom = theme.spacing(4)
            right = theme.spacing(4)
            justifyContent = JustifyContent.end
            alignItems = AlignItems.center
            columnGap = theme.spacing(1)
        }

        props.fabs?.forEach { fabSpec ->
            Fab {
                color = fabSpec.color
                variant = FabVariant.extended
                disabled = fabSpec.disabled

                when(fabSpec.kind) {
                    NEW -> SpeedDialIcon {
                        sx {
                            marginRight = 4.px
                        }
                    }
                    RESET -> Redo {
                        sx {
                            marginRight = 4.px
                        }
                    }
                    SAVE -> Done {
                        sx {
                            marginRight = 4.px
                        }
                    }
                }

                onClick = {
                    fabSpec.onClick()
                }

                +fabSpec.text
            }
        }
    }
}