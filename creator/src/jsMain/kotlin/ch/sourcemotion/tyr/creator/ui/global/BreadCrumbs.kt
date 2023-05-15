package ch.sourcemotion.tyr.creator.ui.global

import ch.sourcemotion.tyr.creator.ui.quizCategoryOf
import ch.sourcemotion.tyr.creator.ui.quizIfOf
import ch.sourcemotion.tyr.creator.ui.quizStageIfOf
import mui.material.Box
import mui.material.Breadcrumbs
import mui.material.Link
import mui.material.LinkUnderline
import mui.system.sx
import react.FC
import react.Props
import react.router.useParams
import web.cssom.FontWeight

val BreadCrumbs = FC<Props> {
    val params = useParams()

    Box {

        Breadcrumbs {
            runCatching { quizIfOf(params) }.getOrNull()?.let { quizId ->
                Link {
                    sx {
                        fontWeight = FontWeight.bold
                    }
                    underline = LinkUnderline.hover
                    color = "inherit"
                    href = "/$quizId"

                    +"Quiz: $quizId"
                }

                runCatching { quizStageIfOf(params) }.getOrNull()?.let { stageId ->
                    Link {
                        sx {
                            fontWeight = FontWeight.bold
                        }
                        underline = LinkUnderline.hover
                        color = "inherit"
                        href = "/$quizId/$stageId"

                        +"Stage: $stageId"
                    }

                    runCatching { quizCategoryOf(params) }.getOrNull()?.let { categoryId ->
                        Link {
                            sx {
                                fontWeight = FontWeight.bold
                            }
                            underline = LinkUnderline.hover
                            color = "inherit"
                            href = "/$quizId/$stageId/$categoryId"

                            +"Category: $categoryId"
                        }
                    }
                }
            }
        }
    }
}
