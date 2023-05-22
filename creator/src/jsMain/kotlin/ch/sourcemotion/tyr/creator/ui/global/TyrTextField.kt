package ch.sourcemotion.tyr.creator.ui.global

import js.core.jso
import mui.material.Box
import mui.material.FormControlVariant
import mui.material.TextField
import mui.material.styles.Theme
import mui.material.styles.useTheme
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.onChange
import web.html.InputType

object NotEmptyTyrTextFieldValidator : TyrTextFieldValidator {
    override fun isValid(newValue: String) = newValue.isNotEmpty()
}

fun interface TyrTextFieldValidator {
    fun isValid(newValue: String): Boolean
}

data class TyrTextFieldValidation(
    val required: Boolean,
    val invalidMessage: String,
    val valid: Boolean = true,
    val validator: TyrTextFieldValidator
)

enum class TyrTextFieldType(val nativeType: InputType) {
    TEXT(InputType.text), DATE(InputType.date)
}

external interface TyrTextFieldProps : Props {
    var title: String
    var validation: TyrTextFieldValidation? // Presence defines if required
    var type: TyrTextFieldType
    var fullWidth: Boolean
    var value: Any
    var onNewValue: (newValue: String, valid: Boolean) -> Unit
}

val TyrTextField = FC<TyrTextFieldProps> { props ->

    val theme = useTheme<Theme>()

    Box {
        sx {
            height = theme.spacing(10) // We save the space for potential error message
        }
        TextField {
            fullWidth = props.fullWidth
            required = props.validation?.required == true
            error = props.validation?.valid == false
            label = ReactNode(props.title)
            variant = FormControlVariant.outlined
            type = props.type.nativeType
            value = props.value
            InputLabelProps = jso { shrink = true }
            helperText = if (props.validation?.valid == false) {
                ReactNode(props.validation?.invalidMessage)
            } else null
            onChange = {
                val newValue: String = it.target.asDynamic().value
                val newValueIsValid = props.validation?.validator?.isValid(newValue) ?: true
                props.onNewValue(newValue, newValueIsValid)
            }
        }
    }
}