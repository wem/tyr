package ch.sourcemotion.tyr.creator.ui

import ch.sourcemotion.tyr.creator.ui.global.AlertSpec
import ch.sourcemotion.tyr.creator.ui.global.Alerts
import ch.sourcemotion.tyr.creator.ui.global.GlobalMessageTrigger
import ch.sourcemotion.tyr.creator.ui.global.MainBar
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
            context = GlobalMessageTrigger { msg ->
                pendingAlerts =
                    (pendingAlerts + AlertSpec(msg.title, msg.description, msg.severity.color) { acknowledgedAlert ->
                        pendingAlerts = pendingAlerts.toMutableList().apply { remove(acknowledgedAlert) }.toList()
                    })
            }
        }
    }
}