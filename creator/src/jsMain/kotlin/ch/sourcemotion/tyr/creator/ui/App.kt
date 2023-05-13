package ch.sourcemotion.tyr.creator.ui

import ch.sourcemotion.tyr.creator.ui.category.QuizCategoryEditor
import ch.sourcemotion.tyr.creator.ui.quiz.QuizEditor
import ch.sourcemotion.tyr.creator.ui.stage.QuizStageEditor
import js.core.jso
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.router.RouterProvider
import react.router.dom.createBrowserRouter
import web.dom.document
import web.html.HTML

fun main() {
    val rootElement = document.createElement(HTML.div)
        .also { document.body.appendChild(it) }

    createRoot(rootElement).render(App.create())
}

private val browserRouter = createBrowserRouter(
    routes = arrayOf(
        jso {
            path = "/"
            Component = Creator
            ErrorBoundary = Error
            children = arrayOf(
                jso {
                    path = ":$QUIZ_NAV_PARAM"
                    Component = QuizEditor
                    ErrorBoundary = Error
                    children = arrayOf(
                        jso {
                            path = ":$QUIZ_STAGE_NAV_PARAM"
                            Component = QuizStageEditor
                            ErrorBoundary = Error
                            children = arrayOf(
                                jso {
                                    path = ":$QUIZ_CATEGORY_NAV_PARAM"
                                    Component = QuizCategoryEditor
                                    ErrorBoundary = Error
                                }
                            )
                        }
                    )
                }
            )
        },
    )
)

private val App = FC<Props> {
    ThemeModule {
        RouterProvider {
            router = browserRouter
        }
    }
}
