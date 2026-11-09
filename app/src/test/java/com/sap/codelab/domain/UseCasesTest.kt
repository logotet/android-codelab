package com.sap.codelab.domain

import com.sap.codelab.location.LocationReminder
import com.sap.codelab.model.GeoLocation
import com.sap.codelab.testing.FakeGeofenceMonitor
import com.sap.codelab.testing.FakeMemoRepository
import com.sap.codelab.testing.FakeReminderNotifier
import com.sap.codelab.testing.WALLDORF
import com.sap.codelab.testing.memo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveMemoUseCaseTest {

    private val repository = FakeMemoRepository(memo(id = 4))
    private val geofenceMonitor = FakeGeofenceMonitor()
    private val saveMemo = SaveMemoUseCase(repository, geofenceMonitor)

    @Test
    fun `saves the memo and monitors its location under the new id`() = runTest {
        val remindersActive = saveMemo(memo(title = "New"))

        assertTrue(remindersActive)
        assertEquals("New", repository.memos.getValue(5).title)
        assertEquals(listOf(LocationReminder(5, WALLDORF)), geofenceMonitor.registrations.single())
    }

    @Test
    fun `still saves the memo if its location can't be monitored`() = runTest {
        geofenceMonitor.registerResult = Result.failure(SecurityException())

        val remindersActive = saveMemo(memo(title = "New"))

        assertFalse(remindersActive)
        assertEquals("New", repository.memos.getValue(5).title)
    }

    @Test
    fun `does not monitor a memo without location`() = runTest {
        val remindersActive = saveMemo(memo(location = null))

        assertFalse(remindersActive)
        assertTrue(geofenceMonitor.registrations.isEmpty())
    }
}

class CompleteMemoUseCaseTest {

    @Test
    fun `marks the memo done, stops monitoring it and removes its reminder`() = runTest {
        val repository = FakeMemoRepository(memo(id = 1))
        val geofenceMonitor = FakeGeofenceMonitor()
        val notifier = FakeReminderNotifier()

        CompleteMemoUseCase(repository, geofenceMonitor, notifier)(memo(id = 1))

        assertTrue(repository.memos.getValue(1).isDone)
        assertEquals(listOf(1L), geofenceMonitor.unregistered)
        assertEquals(listOf(1L), notifier.cancelled)
    }
}

class RestoreRemindersUseCaseTest {

    @Test
    fun `monitors all open memos with a location`() = runTest {
        val berlin = GeoLocation(52.52, 13.405)
        val repository = FakeMemoRepository(
            memo(id = 1, location = WALLDORF),
            memo(id = 2, location = berlin),
            memo(id = 3, location = WALLDORF, isDone = true),
        )
        val geofenceMonitor = FakeGeofenceMonitor()

        RestoreRemindersUseCase(repository, geofenceMonitor)()

        assertEquals(
            listOf(LocationReminder(1, WALLDORF), LocationReminder(2, berlin)),
            geofenceMonitor.registrations.single()
        )
    }
}
