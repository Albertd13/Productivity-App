package com.example.productivitygame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productivitygame.R
import com.example.productivitygame.data.FocusPlanDetails
import com.example.productivitygame.data.toFocusPlanDetails
import com.example.productivitygame.navigation.NavigationDestination
import com.example.productivitygame.ui.AppViewModelProvider
import com.example.productivitygame.ui.components.BetterTextField
import com.example.productivitygame.ui.components.DefaultTopAppBar
import com.example.productivitygame.ui.viewmodels.FocusPlanViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// Navigation to this screen only possible from Timer Screen for now
object FocusPlanDestination : NavigationDestination {
    override val route = "focus_plan_selection"
    override val titleRes = R.string.focus_plan_selection_title
}
@Composable
fun FocusPlanSelectionScreen(
    viewModel: FocusPlanViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onSelectFocusPlan: (focusPlanName: String) -> Unit,
    navigateBack: () -> Unit // Used when nothing is selected
) {
    val focusPlans by viewModel.focusPlanList.collectAsState()
    var isAddFocusPlanDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    Scaffold(
        topBar = {
            DefaultTopAppBar(
                titleText = stringResource(id = FocusPlanDestination.titleRes),
                navigateBack = navigateBack,
                canNavigateBack = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddFocusPlanDialogVisible = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_focus_plan)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(focusPlans) {
                FocusPlanItem(
                    focusPlanDetails = it.toFocusPlanDetails(),
                    onSelectFocusPlan = onSelectFocusPlan
                )
            }
        }
    }
    if (isAddFocusPlanDialogVisible) {
        AddFocusPlanDialog(
            onDiscard = { /*TODO*/ },
            isInputValid = {false },
            onAdd = {}
        )
    }
}

@Composable
fun AddFocusPlanDialog(
    onDiscard: () -> Unit,
    isInputValid: (FocusPlanDetails) -> Boolean,
    onAdd: (focusPlanDetails: FocusPlanDetails) -> Unit
) {
    var focusPlaneName by remember {
        mutableStateOf("")
    }
    var workDuration by remember {
        mutableStateOf(0.minutes)
    }
    var shortBreakDuration by remember {
        mutableStateOf(0.minutes)
    }
    var longBreakDuration by remember {
        mutableStateOf(0.minutes)
    }
    var cycles by remember {
        mutableIntStateOf(0)
    }
    AlertDialog(
        title = {
            Text(text = stringResource(R.string.new_focus_plan_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Name:")
                    BetterTextField(
                        value = focusPlaneName,
                        onValueChange = { focusPlaneName = it },
                        modifier = Modifier.padding(end = 24.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Work:")
                    DurationMinutesTextField(
                        duration = workDuration,
                        onDurationChange = {workDuration = it},
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Break (short):")
                    DurationMinutesTextField(
                        duration = shortBreakDuration,
                        onDurationChange = {shortBreakDuration = it}
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Break (long):")
                    DurationMinutesTextField(
                        duration = longBreakDuration,
                        onDurationChange = {longBreakDuration = it}
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cycles:")
                    BetterTextField(
                        value = cycles.toString(),
                        onValueChange = { cycles = it.toInt() },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.padding(end = 24.dp)
                    )
                }
            }
        },
        onDismissRequest = onDiscard,
        confirmButton = { 
            TextButton(
                onClick = {
                    val focusPlanDetails = FocusPlanDetails(
                        name = focusPlaneName,
                        workDuration = workDuration,
                        shortBreakDuration = shortBreakDuration,
                        longBreakDuration = longBreakDuration,
                        cycles = cycles
                    )
                    if (isInputValid(focusPlanDetails)) {
                        onAdd(focusPlanDetails)
                    }
                }
            ) {
                Text(text = "Add Focus Plan")
            } 
        },
        dismissButton = { 
            TextButton(onClick = onDiscard) {
                Text(text = "Discard")
            } 
        }
    )
}

@Composable
fun DurationMinutesTextField(
    duration: Duration,
    onDurationChange: (duration: Duration) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BetterTextField(
            value = "${duration.inWholeMinutes}",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = {
                if (it.toInt() in 0..300)
                onDurationChange(it.toInt().minutes)
            }
        )
        Text(text = "min")
    }
}

@Composable
fun FocusPlanItem(
    focusPlanDetails: FocusPlanDetails,
    modifier: Modifier = Modifier,
    onSelectFocusPlan: (focusPlanName: String) -> Unit
) {
    Card(
        modifier = modifier.padding(12.dp),
        onClick = { onSelectFocusPlan(focusPlanDetails.name) }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = focusPlanDetails.name, style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Work: ${focusPlanDetails.workDuration.inWholeMinutes} min")
                Text(text = "Short Break: ${focusPlanDetails.shortBreakDuration.inWholeMinutes} min")

            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (focusPlanDetails.cycles != null && focusPlanDetails.longBreakDuration != null) {
                    Text(text = "Long Break after ${focusPlanDetails.cycles} cycles: ${focusPlanDetails.longBreakDuration.inWholeMinutes} min")
                }
            }
        }

    }
}
/*
@Composable
fun FocusPlanIllustration(focusPlanDetails: FocusPlanDetails, modifier: Modifier = Modifier) {

}
 */

@Preview(showBackground = true)
@Composable
fun AddFocusPlanDialogPreview() {
    AddFocusPlanDialog(
        onDiscard = { /*TODO*/ },
        onAdd = { /*TODO*/ },
        isInputValid = {false}
    )
}
