package com.example.productivitygame.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivitygame.data.FocusPlan
import com.example.productivitygame.data.FocusPlanDetails
import com.example.productivitygame.data.dao.FocusPlanDao
import com.example.productivitygame.data.toFocusPlan
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.minutes

class FocusPlanViewModel(
    private val focusPlanDao: FocusPlanDao,
): ViewModel() {
    val focusPlanList: StateFlow<List<FocusPlan>> =
        focusPlanDao
        .getAllFocusPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    var newFocusPlanDetailsState by mutableStateOf(NewFocusPlanState())
        private set

    fun updateNewFocusPlanState(newFocusPlanDetails: FocusPlanDetails) {
        newFocusPlanDetailsState = NewFocusPlanState(
            focusPlanDetails = newFocusPlanDetails,
            isEntryValid = validateInput(newFocusPlanDetails)
        )
    }
    suspend fun saveFocusPlan() {
        if (newFocusPlanDetailsState.isEntryValid) {
            focusPlanDao.insert(newFocusPlanDetailsState.focusPlanDetails.toFocusPlan())
        }
    }
    fun resetNewFocusPlanState() {
        newFocusPlanDetailsState = NewFocusPlanState()
    }

    private fun validateInput(focusPlanDetails: FocusPlanDetails): Boolean =
        // Figure out validation for long break and cycles
         with(focusPlanDetails) {
             !((name.isBlank() || name in focusPlanList.value.map { it.name }) ||
               workDuration <= 0.minutes ||
               shortBreakDuration <= 0.minutes)
        }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class NewFocusPlanState(
    val focusPlanDetails: FocusPlanDetails = FocusPlanDetails(),
    val isEntryValid: Boolean = false
)