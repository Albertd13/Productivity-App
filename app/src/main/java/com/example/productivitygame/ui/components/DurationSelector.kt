package com.example.productivitygame.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.productivitygame.R
import com.example.productivitygame.ui.viewmodels.TimeSelectorState

val hourRange = 0..23
val minuteRange = 0..59
val secondRange = 0..59

@Composable
fun DurationSelector(
    modifier: Modifier = Modifier,
    currentSelectorState: TimeSelectorState,
    onSelectorStateChange: (TimeSelectorState) -> Unit = {}
){
    var isManualEntryEnabled by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row {
            DurationSelectorColumn(
                label = "hours",
                modifier = Modifier.weight(1f),
                intList = hourRange.toList(),
                selectedNum = currentSelectorState.hoursSelected,
                onNumSelected = {
                    onSelectorStateChange(
                        currentSelectorState.copy(hoursSelected = it)
                    )
                },
                onNumClicked = { isManualEntryEnabled = true },
                isManualEntryEnabled = isManualEntryEnabled,
            )
            DurationSelectorColumn(
                label = "minutes",
                modifier = Modifier.weight(1f),
                intList = minuteRange.toList(),
                selectedNum = currentSelectorState.minutesSelected,
                onNumSelected = {
                    onSelectorStateChange(
                        currentSelectorState.copy(minutesSelected = it)
                    )
                },
                onNumClicked = { isManualEntryEnabled = true },
                isManualEntryEnabled = isManualEntryEnabled,
            )
            DurationSelectorColumn(
                label = "seconds",
                modifier = Modifier.weight(1f),
                intList = secondRange.toList(),
                selectedNum = currentSelectorState.secondsSelected,
                onNumSelected = {
                    onSelectorStateChange(
                        currentSelectorState.copy(secondsSelected = it)
                    )
                },
                onNumClicked = { isManualEntryEnabled = true },
                isManualEntryEnabled = isManualEntryEnabled,
            )
        }
        if (isManualEntryEnabled) {
            Button(
                onClick = {
                    isManualEntryEnabled = false
                }
            ) {
                Text(text = "Done")
            }
        }
    }
}

@Composable
fun DurationSelectorColumn(
    modifier: Modifier = Modifier,
    label: String = "",
    selectedNum: Int = 0,
    onNumSelected: (item: Int) -> Unit = {},
    onNumClicked: (item: Int) -> Unit = {},
    isManualEntryEnabled: Boolean = false,
    intList: List<Int>
) {
    val selectedItemScaleFact = 1.5f
    val textStyle = MaterialTheme.typography.displaySmall
    Column(
        modifier = modifier
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        if (!isManualEntryEnabled) {
            InfiniteCircularNumberList(
                itemHeight = dimensionResource(id = R.dimen.duration_selector_item_height),
                intList = intList,
                selectedItemScaleFact = selectedItemScaleFact,
                initialItem = selectedNum,
                textStyle = textStyle,
                modifier = Modifier.fillMaxWidth(),
                textColor = Color.Gray,
                selectedTextColor = Color.Black,
                onNumSelected = onNumSelected,
                onNumClicked = onNumClicked
            )
        } else {
            BetterTextField(
                value = selectedNum.toString(),
                onValueChange = {
                    if (it.toIntOrNull() in hourRange) {
                        onNumSelected(it.toInt())
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .height(dimensionResource(id = R.dimen.duration_selector_item_height)),
                textStyle = textStyle.copy(
                    textAlign = TextAlign.Center,
                    fontSize = textStyle.fontSize * selectedItemScaleFact
                )
            )
        }
    }
}

const val numberOfDisplayedItems = 3

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteCircularNumberList(
    modifier: Modifier = Modifier,
    intList: List<Int>,
    initialItem: Int,
    itemHeight: Dp,
    selectedItemScaleFact: Float = 1.5f, // Selected item's size is scaled by this factor
    textStyle: TextStyle,
    textColor: Color,
    selectedTextColor: Color,
    onNumSelected: (item: Int) -> Unit = {},
    onNumClicked: (item: Int) -> Unit = {},
) {
    val targetIndex = remember {
        intList.indexOf(initialItem) + ((Int.MAX_VALUE / 2) / intList.size) * intList.size - 1
    }
    val scrollState = rememberLazyListState(targetIndex)

    val itemHalfHeight = LocalDensity.current.run { itemHeight.toPx() / 2f }

    var lastSelectedIndex by remember { mutableIntStateOf(targetIndex) }

    LazyColumn(
        modifier = modifier
            .height(itemHeight * numberOfDisplayedItems),
        state = scrollState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = scrollState)
    ) {
        items(
            count = Int.MAX_VALUE,
            itemContent = { lazyColumnIndex ->
                val item = intList[lazyColumnIndex % intList.size]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable {
                            onNumClicked(item)
                        }
                        .onGloballyPositioned { coordinates ->
                            val y = coordinates.positionInParent().y - itemHalfHeight
                            val parentHalfHeight = (itemHalfHeight * numberOfDisplayedItems) / 2f
                            val isSelected =
                                (y in parentHalfHeight - itemHalfHeight..parentHalfHeight + itemHalfHeight)
                            if (isSelected && lastSelectedIndex != lazyColumnIndex) {
                                onNumSelected(item)
                                lastSelectedIndex = lazyColumnIndex
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString(),
                        style = textStyle,
                        color = if (lastSelectedIndex == lazyColumnIndex)
                            selectedTextColor else textColor,
                        fontSize = if (lastSelectedIndex == lazyColumnIndex) {
                            textStyle.fontSize * selectedItemScaleFact
                        } else {
                            textStyle.fontSize
                        }
                    )
                }
            }
        )
    }
}