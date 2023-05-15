package ch.sourcemotion.tyr.creator.ui.quiz

import ch.sourcemotion.tyr.creator.dto.QuizDto
import ch.sourcemotion.tyr.creator.ui.coroutine.launch
import ch.sourcemotion.tyr.creator.ui.navigate
import ch.sourcemotion.tyr.creator.ui.rest.rest
import mu.KotlinLogging
import mui.material.*
import react.FC
import react.Props
import react.router.useNavigate
import react.useEffect
import react.useState

external interface ExistingQuizChooserProps : Props {
    var show: Boolean
    var onFailure: (message: String) -> Unit
    var onClose: () -> Unit
}

private val logger = KotlinLogging.logger("ExistingQuizChooser")

val ExistingQuizChooser = FC<ExistingQuizChooserProps> { props ->

    val nav = useNavigate()

    var showQuizChooserDialog by useState(false)
    var quizzes by useState<List<QuizDto>>()

    useEffect {
        if (showQuizChooserDialog != props.show) {
            launch {
                runCatching { rest.quizzes.getAll(withStages = false, withCategories = false) }
                    .onSuccess { quizzes = it }
                    .onFailure { failure ->
                        logger.error(failure) { "Failed to load quizzes to choose for edit" }
                        props.onFailure("Quiz-Liste für die Editier-Auswahl konnte nicht geladen werden")
                        props.onClose()
                    }
            }
        }
        showQuizChooserDialog = props.show
    }

    Dialog {
        open = showQuizChooserDialog
        onClose = { _, _ ->
            props.onClose()
        }

        DialogTitle {
            +"Quiz zum editieren wählen"
        }

        DialogContent {
            Box {
                List {
                    quizzes?.let { loadedQuizzes ->
                        loadedQuizzes.forEachIndexed { idx, quiz ->
                            ListItem {
                                ListItemButton {
                                    +"Quiz vom ${quiz.date}"

                                    onClick = {
                                        props.onClose()
                                        navigate(nav, quiz.id)
                                    }
                                }
                            }
                            if ((loadedQuizzes.size - 1) > idx) {
                                Divider()
                            }
                        }
                    }
                }
            }

            DialogActions {
                Button {
                    onClick = { props.onClose() }
                    +"Abbrechen"
                }
            }
        }
    }
}