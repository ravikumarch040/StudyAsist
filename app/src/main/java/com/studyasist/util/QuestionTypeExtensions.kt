package com.studyasist.util

import com.studyasist.R
import com.studyasist.data.local.entity.QuestionType

fun QuestionType.labelResId(): Int = when (this) {
    QuestionType.MCQ -> R.string.question_type_mcq
    QuestionType.FILL_BLANK -> R.string.question_type_fill_blank
    QuestionType.SHORT -> R.string.question_type_short
    QuestionType.ESSAY -> R.string.question_type_essay
    QuestionType.NUMERIC -> R.string.question_type_numeric
    QuestionType.TRUE_FALSE -> R.string.question_type_true_false
    QuestionType.MATCHING -> R.string.question_type_matching
    QuestionType.DIAGRAM -> R.string.question_type_diagram
}
