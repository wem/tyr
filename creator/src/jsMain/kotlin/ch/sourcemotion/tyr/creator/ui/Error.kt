package ch.sourcemotion.tyr.creator.ui

import js.errors.JsError
import mui.material.Typography
import react.FC
import react.Props
import react.router.useRouteError

val Error = FC<Props> {
    val error = useRouteError().unsafeCast<JsError>()

    Typography {
        +error.message
    }
}
