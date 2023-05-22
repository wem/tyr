package ch.sourcemotion.tyr.creator.ui

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import react.router.NavigateFunction
import remix.run.router.Params

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

fun quizIdOf(params: Params) = uuidFrom(params[QUIZ_NAV_PARAM] ?: throw QuizParameterMissingException())
fun quizStageIfOf(params: Params) = uuidFrom(params[QUIZ_STAGE_NAV_PARAM] ?: throw QuizStageParameterMissingException())
fun quizCategoryIdOf(params: Params) =
    uuidFrom(params[QUIZ_CATEGORY_NAV_PARAM] ?: throw QuizCategoryParameterMissingException())

abstract class ParameterException(message: String?) : Exception(message)

class QuizParameterMissingException : ParameterException("Quiz parameter missing")
class QuizStageParameterMissingException : Exception("Quiz stage parameter missing")
class QuizCategoryParameterMissingException : Exception("Quiz category parameter missing")

