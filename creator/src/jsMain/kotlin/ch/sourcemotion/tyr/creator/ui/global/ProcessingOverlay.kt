package ch.sourcemotion.tyr.creator.ui.global

import mui.material.Backdrop
import mui.material.CircularProgress
import mui.material.CircularProgressColor
import mui.system.sx
import react.FC
import react.Props
import web.cssom.Color
import web.cssom.integer

external interface ProcessingOverlayProps : Props {
    var show: Boolean
}

val ProcessingOverlay = FC<ProcessingOverlayProps> { props ->
    Backdrop {
        open = props.show
        sx {
            color = Color("#FFFFFF")
            zIndex = integer(1000)
        }

        CircularProgress {
            color = CircularProgressColor.inherit
        }
    }
}