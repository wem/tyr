package ch.sourcemotion.tyr.creator.domain.entity.question.element

data class Text(
    val text: String,
    val description: String?
) : Element {
    companion object {
        fun textOf(text: String, description: String? = null) = Text(text, description)
    }
}