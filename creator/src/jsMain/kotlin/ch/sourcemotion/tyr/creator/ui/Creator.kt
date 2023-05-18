package ch.sourcemotion.tyr.creator.ui

import ch.sourcemotion.tyr.creator.ui.global.*
import mui.material.Box
import mui.material.Grid
import mui.system.sx
import react.FC
import react.Props
import react.router.Outlet
import react.useState
import web.cssom.AlignItems
import web.cssom.JustifyContent
import web.cssom.px

val Creator = FC<Props> {

    var pendingAlerts by useState(listOf<AlertSpec>())

    var currentShortMessage by useState<ShortMessageSpec>()

    Box {
        MainBar()
    }

    Grid {
        container = true
        sx {
            marginTop = 16.px
            alignItems = AlignItems.center
            justifyContent = JustifyContent.center
            rowGap = 16.px
        }

        Alerts {
            alerts = pendingAlerts
        }

        Outlet {
            val globalMessageTrigger = GlobalMessageTrigger { msg ->
                pendingAlerts =
                    (pendingAlerts + AlertSpec(msg.title, msg.description, msg.severity.color) { acknowledgedAlert ->
                        pendingAlerts = pendingAlerts.toMutableList().apply { remove(acknowledgedAlert) }.toList()
                    })
            }

            val shortMessageTrigger = ShortMessageTrigger { msg ->
                currentShortMessage = msg
            }

            context = OutletContextParams(globalMessageTrigger, shortMessageTrigger)
        }

        ShortMessage {
            messageSpec = currentShortMessage
            onClose = {
                currentShortMessage = null
            }
        }
    }
}

data class OutletContextParams(
    val globalMessageTrigger: GlobalMessageTrigger,
    val shortMessageTrigger: ShortMessageTrigger
)