package com.sap.codelab.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sap.codelab.model.Memo

/**
 * The Dao representation of a Memo.
 */
@Dao
internal interface MemoDao {

    /**
     * @return all memos that are currently in the database.
     */
    @Query("SELECT * FROM memo")
    fun getAll(): List<Memo>

    /**
     * @return all memos that are currently in the database and have not yet been marked as "done".
     */
    @Query("SELECT * FROM memo WHERE isDone = 0")
    fun getOpen(): List<Memo>

    /**
     * @return all memos that have not yet been marked as "done" and have a reminder location.
     */
    @Query("SELECT * FROM memo WHERE isDone = 0 AND reminderLatitude IS NOT NULL AND reminderLongitude IS NOT NULL")
    fun getOpenWithLocation(): List<Memo>

    /**
     * Inserts the given Memo into the database, replacing an existing memo with the same id.
     *
     * @return the id of the inserted memo.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(memo: Memo): Long

    /**
     * @return the memo whose id matches the given id, or null if there is no such memo.
     */
    @Query("SELECT * FROM memo WHERE id = :memoId")
    fun getMemoById(memoId: Long): Memo?
}
