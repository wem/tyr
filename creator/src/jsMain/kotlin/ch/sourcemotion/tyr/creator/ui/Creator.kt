package ch.sourcemotion.tyr.creator.ui

import ch.sourcemotion.tyr.creator.ui.global.MainBar
import mui.material.Box
import mui.material.Grid
import mui.system.sx
import react.FC
import react.Props
import react.router.Outlet
import web.cssom.AlignItems
import web.cssom.JustifyContent
import web.cssom.px

val Creator = FC<Props> {
    Box {
        MainBar()
    }
    Grid {
        container = true
        sx {
            marginTop = 16.px
            alignItems = AlignItems.center
            justifyContent = JustifyContent.center
        }
        Outlet()
    }
}