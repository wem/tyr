package ch.sourcemotion.tyr.creator.ui.global

import mui.material.Alert
import mui.material.AlertColor
import mui.material.AlertTitle
import mui.material.Grid
import mui.system.sx
import react.FC
import react.Props
import web.cssom.*

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