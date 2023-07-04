package ch.sourcemotion.tyr.creator.ui.ext

import csstype.PropertiesBuilder
import web.cssom.AlignItems
import web.cssom.GridAutoFlow
import web.cssom.JustifyContent


fun PropertiesBuilder.centeredGridElements() {
    justifyContent = JustifyContent.center
    alignItems = AlignItems.center
}

fun PropertiesBuilder.rowFlow() {
    gridAutoFlow = GridAutoFlow.row
}

fun PropertiesBuilder.columnFlow() {
    gridAutoFlow = GridAutoFlow.column
}

