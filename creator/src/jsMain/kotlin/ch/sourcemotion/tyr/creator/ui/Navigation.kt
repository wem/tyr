package ch.sourcemotion.tyr.creator.ui

import com.benasher44.uuid.Uuid
import react.router.NavigateFunction

const val QUIZ_NAV_PARAM = "quizId"
const val QUIZ_STAGE_NAV_PARAM = "quizStageId"
const val QUIZ_CATEGORY_NAV_PARAM = "quizCategoryId"

fun navigate(func: NavigateFunction, quizId: Uuid? = null, quizStageId: Uuid? = null, quizCategoryId: Uuid? = null) {
    var path = "/"
    quizId?.let { path += "$it" }
    quizStageId?.let { path += "/$it" }
    quizCategoryId?.let { path += "/$it" }

    func(path)
}