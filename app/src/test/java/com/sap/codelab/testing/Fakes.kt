package com.sap.codelab.testing

import com.sap.codelab.location.GeofenceMonitor
import com.sap.codelab.location.LocationReminder
import com.sap.codelab.model.GeoLocation
import com.sap.codelab.model.Memo
import com.sap.codelab.model.location
import com.sap.codelab.notification.ReminderNotifier
import com.sap.codelab.repository.IMemoRepository

internal val WALLDORF = GeoLocation(latitude = 49.2933, longitude = 8.6420)

internal fun memo(
    id: Long = 0,
    title: String = "Title",
    description: String = "Text",
    location: GeoLocation? = WALLDORF,
    isDone: Boolean = false,
) = Memo(
    id = id,
    title = title,
    description = description,
    reminderDate = 0,
    reminderLatitude = location?.latitude,
    reminderLongitude = location?.longitude,
    isDone = isDone
)

/**
 * In-memory [IMemoRepository] that hands out ids like the database does.
 */
internal class FakeMemoRepository(vararg initialMemos: Memo) : IMemoRepository {

    val memos: MutableMap<Long, Memo> = initialMemos.associateBy { it.id }.toMutableMap()
    private var nextId = (memos.keys.maxOrNull() ?: 0) + 1

    override fun saveMemo(memo: Memo): Long {
        val id = if (memo.id == 0L) nextId++ else memo.id
        memos[id] = memo.copy(id = id)
        return id
    }

    override fun getAll(): List<Memo> = memos.values.toList()

    override fun getOpen(): List<Memo> = memos.values.filter { !it.isDone }

    override fun getOpenWithLocation(): List<Memo> = getOpen().filter { it.location != null }

    override fun getMemoById(id: Long): Memo? = memos[id]
}

internal class FakeGeofenceMonitor(var registerResult: Result<Unit> = Result.success(Unit)) : GeofenceMonitor {

    /**
     * The reminders passed to each call of [register].
     */
    val registrations = mutableListOf<List<LocationReminder>>()
    val unregistered = mutableListOf<Long>()

    override suspend fun register(reminders: List<LocationReminder>): Result<Unit> {
        registrations.add(reminders)
        return registerResult
    }

    override suspend fun unregister(memoId: Long): Result<Unit> {
        unregistered += memoId
        return Result.success(Unit)
    }
}

internal class FakeReminderNotifier : ReminderNotifier {

    val shown = mutableListOf<Memo>()
    val cancelled = mutableListOf<Long>()

    override fun show(memo: Memo) {
        shown += memo
    }

    override fun cancel(memoId: Long) {
        cancelled += memoId
    }
}
