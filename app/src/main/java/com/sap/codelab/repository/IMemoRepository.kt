package com.sap.codelab.repository

import com.sap.codelab.model.Memo

/**
 * Interface for a repository offering memo related CRUD operations.
 */
internal interface IMemoRepository {

    /**
     * Saves the given memo to the database.
     *
     * @return the id of the saved memo.
     */
    fun saveMemo(memo: Memo): Long

    /**
     * @return all memos currently in the database.
     */
    fun getAll(): List<Memo>

    /**
     * @return all memos currently in the database, except those that have been marked as "done".
     */
    fun getOpen(): List<Memo>

    /**
     * @return all memos that have not been marked as "done" and have a reminder location.
     */
    fun getOpenWithLocation(): List<Memo>

    /**
     * @return the memo whose id matches the given id, or null if there is no such memo.
     */
    fun getMemoById(id: Long): Memo?
}
