package com.studyasist.data.local.entity

import androidx.room.TypeConverter

enum class QuestionType {
    MCQ,
    FILL_BLANK,
    SHORT,
    ESSAY,
    NUMERIC,
    TRUE_FALSE,
    MATCHING,
    DIAGRAM
}

class QuestionTypeConverter {
    @TypeConverter
    fun from(value: QuestionType): String = value.name

    @TypeConverter
    fun to(value: String): QuestionType = QuestionType.valueOf(value)
}
