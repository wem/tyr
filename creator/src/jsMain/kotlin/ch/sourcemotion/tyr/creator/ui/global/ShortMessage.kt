package ch.sourcemotion.tyr.creator.ui.global

import ch.sourcemotion.tyr.creator.ui.global.ShortMessageSeverity.INFO
import ch.sourcemotion.tyr.creator.ui.global.ShortMessageSeverity.SUCCESS
import mui.material.Alert
import mui.material.AlertColor
import mui.material.Snackbar
import mui.system.sx
import react.FC
import react.Props
import web.cssom.pct

fun interface ShortMessageTrigger {
    fun showMessage(spec: ShortMessageSpec)
}

fun ShortMessageTrigger.showSuccessMsg(message: String) = showMessage(ShortMessageSpec(message, SUCCESS))

fun ShortMessageTrigger.showInfoMsg(message: String) = showMessage(ShortMessageSpec(message, INFO))

enum class ShortMessageSeverity {
    INFO, SUCCESS
}

data class ShortMessageSpec(
    val message: String,
    val severity: ShortMessageSeverity
)

external interface ShortMessageProps : Props {
    var messageSpec: ShortMessageSpec?
    var onClose: () -> Unit
}

val ShortMessage = FC<ShortMessageProps> { props ->
    Snackbar {
        open = props.messageSpec != null
        autoHideDuration = 3000
        props.messageSpec?.let { messageSpec ->
            Alert {
                sx {
                    width = 100.pct
                }
                severity = when (messageSpec.severity) {
                    SUCCESS -> AlertColor.success
                    INFO -> AlertColor.info
                }

                +messageSpec.message
            }
        }
        onClose = { _, _ ->
            props.onClose()
        }
    }
}