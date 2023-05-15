package ch.sourcemotion.tyr.creator.ui.stage

import ch.sourcemotion.tyr.creator.ui.quizStageIfOf
import mu.KotlinLogging
import react.FC
import react.Props
import react.router.useParams
import react.useEffect

private val logger = KotlinLogging.logger("QuizStageEditor")

val QuizStageEditor = FC<Props> {

    val params = useParams()

    useEffect {
        logger.info { "Quiz edit for quiz stage '${quizStageIfOf(params)} loaded'" }
    }
}