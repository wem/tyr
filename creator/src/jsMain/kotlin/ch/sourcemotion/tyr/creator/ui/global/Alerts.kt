package ch.sourcemotion.tyr.creator.ui.global

import mui.material.Alert
import mui.material.AlertColor
import mui.material.AlertTitle
import mui.material.Grid
import mui.system.sx
import react.FC
import react.Props
import web.cssom.*

data class GlobalMessage(
    val title: String,
    val description: String,
    val severity: GlobalMessage.Severity
) {
    enum class Severity(val color: AlertColor) {
        SUCCESS(AlertColor.success),
        INFO(AlertColor.info),
        WARN(AlertColor.warning),
        ERROR(AlertColor.error)
    }
}

fun interface GlobalMessageTrigger {
    fun showMessage(message: GlobalMessage)
}

fun GlobalMessageTrigger.showError(title: String, description: String){
    showMessage(GlobalMessage(title, description, GlobalMessage.Severity.ERROR))
}

fun GlobalMessageTrigger.showWarning(title: String, description: String) {
    showMessage(GlobalMessage(title, description, GlobalMessage.Severity.WARN))
}

data class AlertSpec(
    val title: String,
    val description: String,
    val color: AlertColor,
    val onAck: (AlertSpec) -> Unit
)

external interface AlertsProps: Props {
    var alerts: List<AlertSpec>?
}

val Alerts = FC<AlertsProps> { props ->
    Grid {
        container = true
        sx {
            gridAutoFlow = GridAutoFlow.row
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
            rowGap = 4.px
        }

        props.alerts?.forEach { alertSpec ->
            Alert {
                sx {
                    width = 51.pct // Little hack to keep alerts in a row the simple way
                }
                AlertTitle {
                    +alertSpec.title
                }
                severity = alertSpec.color

                onClose = {
                    alertSpec.onAck(alertSpec)
                }

                +alertSpec.description
            }
        }
    }
}