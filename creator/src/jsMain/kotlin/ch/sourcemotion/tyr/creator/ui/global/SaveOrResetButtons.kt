package ch.sourcemotion.tyr.creator.ui.global

import mui.material.Button
import mui.material.ButtonGroup
import mui.material.ButtonVariant
import mui.material.Orientation
import react.FC
import react.Props

external interface SaveOrResetButtonsProps : Props {
    var onSave : () -> Unit
    var onCancel : () -> Unit
}

val SaveOrResetButtons = FC<SaveOrResetButtonsProps> { props ->
    ButtonGroup {
        orientation = Orientation.horizontal
        Button {
            variant = ButtonVariant.outlined
            +"Speicher"
            onClick =  {
                props.onSave()
            }
        }
        Button {
            variant = ButtonVariant.contained
            +"Zurücksetzen"
            onClick =  {
                props.onCancel( )
            }
        }
    }
}