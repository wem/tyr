package ch.sourcemotion.tyr.creator.ui.global

import mui.material.*
import react.FC
import react.Props

data class ConfirmSpec(
    var show: Boolean,
    var title: String,
    var text: String
)

external interface ConfirmProps : Props {
    var spec: ConfirmSpec?
    var onConfirm: (() -> Unit)?
    var onCancel: (() -> Unit)?
}

val Confirm = FC<ConfirmProps> { props ->
    Dialog {
        open = props.spec?.show ?: false

        onClose = { _, _ ->
            println("on close")
            props.onCancel?.invoke()
        }

        DialogTitle {
            +props.spec?.title
        }
        DialogContent {
            DialogContentText {
                +props.spec?.text
            }
            DialogActions {
                Button {
                    onClick = {
                        props.onConfirm?.invoke()
                    }
                    +"Bestätigen"
                }
                Button {
                    onClick = {
                        props.onCancel?.invoke()
                    }
                    +"Abbrechen"
                }
            }
        }
    }
}