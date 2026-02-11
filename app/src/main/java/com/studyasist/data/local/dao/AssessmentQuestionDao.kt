package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studyasist.data.local.entity.AssessmentQuestion

@Dao
interface AssessmentQuestionDao {

    @Query("SELECT * FROM assessment_questions WHERE assessmentId = :assessmentId ORDER BY sequence")
    suspend fun getByAssessmentId(assessmentId: Long): List<AssessmentQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AssessmentQuestion): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AssessmentQuestion>)

    @Query("DELETE FROM assessment_questions WHERE assessmentId = :assessmentId")
    suspend fun deleteByAssessmentId(assessmentId: Long)
}
