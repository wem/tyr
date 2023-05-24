package ch.sourcemotion.tyr.creator.logging

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.slf4j.MDCContext

const val QUIZ_ID_MDC = "quiz_id"
const val QUIZ_STAGE_ID_MDC = "quiz_stage_id"
const val QUIZ_CATEGORY_ID_MDC = "quiz_category_id"

fun mdcOf(
    quizId: Uuid? = null,
    quizStageId: Uuid? = null,
    quizCategoryId: Uuid? = null,
    fileInfoId: Uuid? = null,
) = MDCContext(buildMap {
    if (quizId != null) {
        put(QUIZ_ID_MDC, "$quizId")
    }
    if (quizStageId != null) {
        put(QUIZ_STAGE_ID_MDC, "$quizStageId")
    }
    if (quizCategoryId != null) {
        put(QUIZ_CATEGORY_ID_MDC, "$quizCategoryId")
    }
})
