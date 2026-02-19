package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studyasist.data.local.entity.QA
import kotlinx.coroutines.flow.Flow

@Dao
interface QADao {

    @Query("SELECT * FROM qa_bank WHERE id = :id")
    suspend fun getById(id: Long): QA?

    @Query("SELECT * FROM qa_bank WHERE id IN (:ids) ORDER BY id")
    suspend fun getByIds(ids: List<Long>): List<QA>

    @Query("SELECT * FROM qa_bank WHERE (:subject IS NULL OR subject = :subject) AND (:chapter IS NULL OR chapter = :chapter) ORDER BY createdAt DESC")
    fun getBySubjectChapter(subject: String?, chapter: String?): Flow<List<QA>>

    @Query("SELECT * FROM qa_bank WHERE (:subject IS NULL OR subject = :subject) AND (:chapter IS NULL OR chapter = :chapter) ORDER BY createdAt DESC")
    suspend fun getBySubjectChapterOnce(subject: String?, chapter: String?): List<QA>

    @Query("SELECT * FROM qa_bank WHERE (:subject IS NULL OR subject = :subject) AND (:chapter IS NULL OR chapter = :chapter) ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomBySubjectChapter(subject: String?, chapter: String?, limit: Int): List<QA>

    @Query("SELECT COUNT(*) FROM qa_bank WHERE (:subject IS NULL OR subject = :subject) AND (:chapter IS NULL OR chapter = :chapter)")
    suspend fun countBySubjectChapter(subject: String?, chapter: String?): Int

    @Query("SELECT * FROM qa_bank ORDER BY createdAt DESC")
    fun getAll(): Flow<List<QA>>

    @Query("SELECT * FROM qa_bank ORDER BY createdAt DESC")
    suspend fun getAllOnce(): List<QA>

    @Query("DELETE FROM qa_bank")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT subject FROM qa_bank WHERE subject IS NOT NULL AND subject != '' ORDER BY subject")
    suspend fun getDistinctSubjects(): List<String>

    @Query("SELECT DISTINCT chapter FROM qa_bank WHERE chapter IS NOT NULL AND chapter != '' ORDER BY chapter")
    suspend fun getDistinctChapters(): List<String>

    @Query("SELECT DISTINCT chapter FROM qa_bank WHERE subject = :subject AND chapter IS NOT NULL AND chapter != '' ORDER BY chapter")
    suspend fun getDistinctChaptersForSubject(subject: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QA): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<QA>): List<Long>

    @Update
    suspend fun update(entity: QA)

    @Query("DELETE FROM qa_bank WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM qa_bank WHERE nextReviewDate <= :now AND nextReviewDate > 0 ORDER BY nextReviewDate ASC LIMIT :limit")
    suspend fun getDueCards(now: Long, limit: Int): List<QA>

    @Query("SELECT COUNT(*) FROM qa_bank WHERE nextReviewDate <= :now AND nextReviewDate > 0")
    suspend fun getDueCount(now: Long): Int

    @Query("SELECT COUNT(*) FROM qa_bank WHERE nextReviewDate > :start AND nextReviewDate <= :end")
    suspend fun getDueCountInRange(start: Long, end: Long): Int

    @Query("UPDATE qa_bank SET easeFactor = :easeFactor, srsInterval = :interval, repetitions = :repetitions, nextReviewDate = :nextReviewDate, lastReviewDate = :lastReviewDate WHERE id = :id")
    suspend fun updateSRS(id: Long, easeFactor: Double, interval: Int, repetitions: Int, nextReviewDate: Long, lastReviewDate: Long)

    @Query("SELECT * FROM qa_bank WHERE nextReviewDate = 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getNewCardsForReview(limit: Int): List<QA>
}
