package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgressEntity)

    @Update
    suspend fun updateUserProgress(progress: UserProgressEntity)
}

@Dao
interface LibraryItemDao {
    @Query("SELECT * FROM library_items")
    fun getAllLibraryItems(): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM library_items WHERE category = :category")
    fun getLibraryItemsByCategory(category: String): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM library_items WHERE isBookmarked = 1")
    fun getBookmarkedLibraryItems(): Flow<List<LibraryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryItem(item: LibraryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryItems(items: List<LibraryItemEntity>)

    @Update
    suspend fun updateLibraryItem(item: LibraryItemEntity)
}

@Dao
interface CourseProgressDao {
    @Query("SELECT * FROM course_progress")
    fun getAllCourseProgress(): Flow<List<CourseProgressEntity>>

    @Query("SELECT * FROM course_progress WHERE category = :category")
    fun getCourseProgressByCategory(category: String): Flow<List<CourseProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseProgress(progress: CourseProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseProgressList(list: List<CourseProgressEntity>)

    @Update
    suspend fun updateCourseProgress(progress: CourseProgressEntity)
}

@Dao
interface MockExamResultDao {
    @Query("SELECT * FROM mock_exam_results ORDER BY timestamp DESC")
    fun getAllExamResults(): Flow<List<MockExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: MockExamResultEntity)

    @Query("DELETE FROM mock_exam_results")
    suspend fun clearAllResults()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE mode = :mode ORDER BY timestamp ASC")
    fun getChatMessagesByMode(mode: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}
