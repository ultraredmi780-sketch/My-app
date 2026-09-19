package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT 1")
    suspend fun getFirstUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET subscriptionStatus = :status WHERE id = :userId")
    suspend fun updateSubscription(userId: Long, status: String)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLessonsForUser(userId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: Long): LessonEntity?

    @Query("SELECT * FROM lessons WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR extractedText LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchLessons(userId: Long, query: String): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Query("DELETE FROM lessons WHERE id = :lessonId")
    suspend fun deleteLesson(lessonId: Long)

    @Query("SELECT COUNT(*) FROM lessons WHERE userId = :userId")
    fun getLessonCount(userId: Long): Flow<Int>
}

@Dao
interface QuizAttemptDao {
    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAttemptsForUser(userId: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId AND lessonId = :lessonId ORDER BY createdAt DESC")
    fun getAttemptsForLesson(userId: Long, lessonId: Long): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE userId = :userId")
    fun getQuizCount(userId: Long): Flow<Int>

    @Query("SELECT AVG(score) FROM quiz_attempts WHERE userId = :userId")
    fun getAverageScore(userId: Long): Flow<Double?>
}

@Dao
interface UsageDao {
    @Query("SELECT * FROM usage WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getUsage(userId: Long, date: String): UsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(usage: UsageEntity)

    @Query("SELECT * FROM usage WHERE userId = :userId AND date = :date LIMIT 1")
    fun observeUsage(userId: Long, date: String): Flow<UsageEntity?>
}
