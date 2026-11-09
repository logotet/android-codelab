package com.sap.codelab.view.create

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.permission.ReminderPermissionRequester
import com.sap.codelab.utils.extensions.empty
import com.sap.codelab.view.pick.PickLocationContract

/**
 * Result extra: true if the user will be reminded of the saved memo; false if a permission for that is missing.
 */
internal const val RESULT_REMINDERS_ENABLED: String = "remindersEnabled"

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private lateinit var model: CreateMemoViewModel
    private val permissionRequester = ReminderPermissionRequester(this)
    private val pickLocationLauncher = registerForActivityResult(PickLocationContract()) { location ->
        if (location != null) {
            model.updateLocation(location)
            updateLocationViews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        model = ViewModelProvider(this)[CreateMemoViewModel::class.java]

        binding.contentCreateMemo.selectLocation.setOnClickListener {
            // The map can show the user's own position if the location permission is granted. It works without too.
            permissionRequester.requestPreciseLocation {
                pickLocationLauncher.launch(model.location)
            }
        }
        updateLocationViews()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveMemo()
                true
            }

            else             -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Saves the memo if the input is valid; otherwise shows the corresponding error messages.
     */
    private fun saveMemo() {
        binding.contentCreateMemo.run {
            model.updateMemo(memoTitle.text.toString(), memoDescription.text.toString())
            if (model.isMemoValid()) {
                // The memo is saved whether or not the user grants the permissions; they can still grant them later.
                permissionRequester.requestReminderPermissions { remindersEnabled ->
                    model.saveMemo()
                    setResult(RESULT_OK, Intent().putExtra(RESULT_REMINDERS_ENABLED, remindersEnabled))
                    finish()
                }
            } else {
                memoTitleContainer.error = getErrorMessage(model.hasTitleError(), R.string.memo_title_empty_error)
                memoDescription.error = getErrorMessage(model.hasTextError(), R.string.memo_text_empty_error)
                memoLocationError.isVisible = model.hasLocationError()
            }
        }
    }

    /**
     * Shows the location the user picked.
     */
    private fun updateLocationViews() {
        binding.contentCreateMemo.run {
            val location = model.location
            memoLocation.text = location?.toDisplayString() ?: getString(R.string.memo_location_not_set)
            if (location != null) {
                memoLocationError.isVisible = false
            }
        }
    }

    /**
     * Returns the error message if there is an error, or an empty string otherwise.
     *
     * @param hasError          - whether there is an error.
     * @param errorMessageResId - the resource id of the error message to show.
     * @return the error message if there is an error, or an empty string otherwise.
     */
    private fun getErrorMessage(hasError: Boolean, @StringRes errorMessageResId: Int): String {
        return if (hasError) {
            getString(errorMessageResId)
        } else {
            String.empty()
        }
    }
}
