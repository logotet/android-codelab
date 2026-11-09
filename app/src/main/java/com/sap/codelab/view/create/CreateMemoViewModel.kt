package com.sap.codelab.view.create

import androidx.lifecycle.ViewModel
import com.sap.codelab.model.GeoLocation
import com.sap.codelab.model.Memo
import com.sap.codelab.model.location
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import com.sap.codelab.utils.extensions.empty
import kotlinx.coroutines.launch

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel : ViewModel() {

    private var memo = Memo(0, String.empty(), String.empty(), 0, null, null, false)

    /**
     * The location the user picked for the reminder, or null if none has been picked yet.
     */
    var location: GeoLocation? = null
        private set

    /**
     * Saves the memo in it's current state.
     */
    fun saveMemo() {
        ScopeProvider.application.launch {
            Repository.saveMemo(memo)
        }
    }

    /**
     * Call this method when the user picked the location they want to be reminded at.
     */
    fun updateLocation(location: GeoLocation) {
        this.location = location
    }

    /**
     * Call this method to update the memo. This is usually needed when the user changed his input.
     */
    fun updateMemo(title: String, description: String) {
        memo = Memo(
            title = title,
            description = description,
            id = 0,
            reminderDate = 0,
            reminderLatitude = location?.latitude,
            reminderLongitude = location?.longitude,
            isDone = false
        )
    }

    /**
     * @return true if the title and content are not blank and a location has been picked; false otherwise.
     */
    fun isMemoValid(): Boolean = !hasTitleError() && !hasTextError() && !hasLocationError()

    /**
     * @return true if the memo text is blank, false otherwise.
     */
    fun hasTextError() = memo.description.isBlank()

    /**
     * @return true if the memo title is blank, false otherwise.
     */
    fun hasTitleError() = memo.title.isBlank()

    /**
     * @return true if no location has been picked for the memo, false otherwise.
     */
    fun hasLocationError() = memo.location == null
}
