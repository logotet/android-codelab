package com.sap.codelab.view.create

import com.sap.codelab.domain.SaveMemoUseCase
import com.sap.codelab.location.LocationReminder
import com.sap.codelab.testing.FakeGeofenceMonitor
import com.sap.codelab.testing.FakeMemoRepository
import com.sap.codelab.testing.WALLDORF
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateMemoViewModelTest {

    private val repository = FakeMemoRepository()
    private val geofenceMonitor = FakeGeofenceMonitor()
    private val scope = TestScope()
    private val model = CreateMemoViewModel(SaveMemoUseCase(repository, geofenceMonitor), scope)

    @Test
    fun `a memo with title, text and location is valid`() {
        model.updateLocation(WALLDORF)
        model.updateMemo("Title", "Text")

        assertTrue(model.isMemoValid())
    }

    @Test
    fun `a memo without location is invalid`() {
        model.updateMemo("Title", "Text")

        assertFalse(model.isMemoValid())
        assertTrue(model.hasLocationError())
        assertFalse(model.hasTitleError())
        assertFalse(model.hasTextError())
    }

    @Test
    fun `a memo with blank title and text is invalid`() {
        model.updateLocation(WALLDORF)
        model.updateMemo(" ", "")

        assertFalse(model.isMemoValid())
        assertTrue(model.hasTitleError())
        assertTrue(model.hasTextError())
        assertFalse(model.hasLocationError())
    }

    @Test
    fun `saving stores the memo with its location and monitors it`() = scope.runTest {
        model.updateLocation(WALLDORF)
        model.updateMemo("Title", "Text")

        model.saveMemo()
        advanceUntilIdle()

        val saved = repository.memos.values.single()
        assertEquals(WALLDORF.latitude, saved.reminderLatitude!!, 0.0)
        assertEquals(WALLDORF.longitude, saved.reminderLongitude!!, 0.0)
        assertEquals(listOf(LocationReminder(saved.id, WALLDORF)), geofenceMonitor.registrations.single())
    }
}
