package ch.sourcemotion.tyr.creator.ui

import ch.sourcemotion.tyr.creator.ui.global.*
import mui.material.Box
import mui.material.styles.Theme
import mui.material.styles.useTheme
import mui.system.sx
import react.FC
import react.Props
import react.router.Outlet
import react.useState
import web.cssom.Auto
import web.cssom.Display
import web.cssom.array
import web.cssom.pct

val Creator = FC<Props> {

    val theme = useTheme<Theme>()

    var pendingAlerts by useState(listOf<AlertSpec>())
    var currentShortMessage by useState<ShortMessageSpec>()

    Box {
        id = "creator-container"
        sx {
            display = Display.grid
            gridTemplateRows = array(
                Sizes.Header.height,
                Auto.auto,
                Auto.auto,
            )
            rowGap = theme.spacing(2)
            width = 100.pct
        }

        MainBar()

        Alerts {
            alerts = pendingAlerts
        }

        Outlet {
            val globalMessageTrigger = GlobalMessageTrigger { msg ->
                pendingAlerts =
                    (pendingAlerts + AlertSpec(
                        msg.title,
                        msg.description,
                        msg.severity.color
                    ) { acknowledgedAlert ->
                        pendingAlerts =
                            pendingAlerts.toMutableList().apply { remove(acknowledgedAlert) }.toList()
                    })
            }

            val shortMessageTrigger = ShortMessageTrigger { msg ->
                currentShortMessage = msg
            }

            context = OutletContextParams(globalMessageTrigger, shortMessageTrigger)
        }
    }

    ShortMessage {
        messageSpec = currentShortMessage
        onClose = {
            currentShortMessage = null
        }
    }
}

data class OutletContextParams(
    val globalMessageTrigger: GlobalMessageTrigger,
    val shortMessageTrigger: ShortMessageTrigger
)