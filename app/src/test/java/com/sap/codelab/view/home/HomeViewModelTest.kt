package com.sap.codelab.view.home

import com.sap.codelab.domain.CompleteMemoUseCase
import com.sap.codelab.testing.FakeGeofenceMonitor
import com.sap.codelab.testing.FakeMemoRepository
import com.sap.codelab.testing.FakeReminderNotifier
import com.sap.codelab.testing.memo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Test
    fun `completing a memo stops monitoring it`() {
        val repository = FakeMemoRepository(memo(id = 1))
        val geofenceMonitor = FakeGeofenceMonitor()
        val scope = TestScope()
        val model = HomeViewModel(CompleteMemoUseCase(repository, geofenceMonitor, FakeReminderNotifier()), scope)

        scope.runTest {
            model.updateMemo(memo(id = 1), isChecked = true)
            advanceUntilIdle()
        }

        assertTrue(repository.memos.getValue(1).isDone)
        assertEquals(listOf(1L), geofenceMonitor.unregistered)
    }
}
