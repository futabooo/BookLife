package com.futabooo.android.booklife.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futabooo.android.booklife.R
import com.futabooo.android.booklife.ui.theme.BookLifeColors
import com.futabooo.android.booklife.ui.theme.BookLifeSpacing
import com.futabooo.android.booklife.ui.theme.BookLifeTextSizes
import com.futabooo.android.booklife.ui.theme.BookLifeTheme

/**
 * "Register as read" / "edit review" form (old `dialog_book_add.xml`). Stateless w.r.t. the network:
 * the submit callback receives `(review, readAt, netabare)` where `readAt` is formatted `yyyy/M/d`.
 * When editing, `readAt` is null unless the user picked a date (the stored date is then left alone,
 * which also covers an unknown [initialReadAt]).
 *
 * Display it through the [com.futabooo.android.booklife.ui.navigation.ReadBookDialog] key (wired in
 * `BookActionEntries`); this composable only draws the dialog card.
 *
 * @param initialReadAt `yyyy/M/d` date of the existing review; null = unknown (today when adding,
 *   an untouched "-" when editing).
 * @param isEditing true shows "Update" instead of "Register" on the confirm button.
 * @param enabled false while a submit is in flight (disables the buttons).
 * @param showError shows the "update failed" text below the buttons.
 */
@Composable
fun AddReadBookDialog(
    onSubmit: (review: String, readAt: String?, netabare: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialReview: String = "",
    initialReadAt: String? = null,
    initialNetabare: Boolean = false,
    isEditing: Boolean = false,
    enabled: Boolean = true,
    showError: Boolean = false,
) {
    var review by rememberSaveable { mutableStateOf(initialReview) }
    var netabare by rememberSaveable { mutableStateOf(initialNetabare) }
    // Selected date as UTC millis. Adding defaults to today; editing keeps the stored (possibly
    // unknown) date unless the user picks one.
    val initialMillis = remember(initialReadAt, isEditing) {
        when {
            initialReadAt != null -> ReadDates.parse(initialReadAt)
            isEditing -> null
            else -> ReadDates.todayMillis()
        }
    }
    var dateMillis by rememberSaveable { mutableStateOf(initialMillis) }
    var datePicked by rememberSaveable { mutableStateOf(false) }
    var showPicker by rememberSaveable { mutableStateOf(false) }

    val dateDisplay = dateMillis?.let(ReadDates::display) ?: initialReadAt ?: UNKNOWN_DATE
    val dateSubmit: String? = if (isEditing && !datePicked) null else dateMillis?.let(ReadDates::submit)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        shape = RoundedCornerShape(4.dp),
        color = BookLifeColors.primary,
    ) {
        Column {
            TextField(
                value = review,
                onValueChange = { review = it },
                enabled = enabled,
                placeholder = { Text(stringResource(R.string.book_impressions_review)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BookLifeColors.primary,
                    unfocusedContainerColor = BookLifeColors.primary,
                    disabledContainerColor = BookLifeColors.primary,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
                modifier = Modifier
                    .padding(top = BookLifeSpacing.Large)
                    .fillMaxWidth()
                    .height(170.dp),
            )
            HorizontalDivider(
                color = BookLifeColors.divider,
                modifier = Modifier.padding(
                    start = BookLifeSpacing.Large,
                    end = BookLifeSpacing.Large,
                    top = BookLifeSpacing.Medium,
                    bottom = BookLifeSpacing.Medium,
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BookLifeSpacing.Large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.book_read_date),
                    color = BookLifeColors.primaryText,
                    fontSize = BookLifeTextSizes.Large,
                )
                Text(
                    text = dateDisplay,
                    color = BookLifeColors.primaryText,
                    fontSize = BookLifeTextSizes.Large,
                    modifier = Modifier
                        .clickable(enabled = enabled) { showPicker = true }
                        .padding(end = 7.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BookLifeSpacing.Large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.book_spoilers),
                    color = BookLifeColors.primaryText,
                    fontSize = BookLifeTextSizes.Large,
                )
                Checkbox(checked = netabare, onCheckedChange = { netabare = it }, enabled = enabled)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = BookLifeSpacing.Large,
                        top = BookLifeSpacing.Medium,
                        bottom = BookLifeSpacing.Large,
                    ),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss, enabled = enabled) {
                    Text(stringResource(R.string.cancel).uppercase())
                }
                TextButton(
                    onClick = { onSubmit(review, dateSubmit, netabare) },
                    enabled = enabled,
                ) {
                    val label = if (isEditing) R.string.update else R.string.book_register
                    Text(stringResource(label).uppercase())
                }
            }
            if (showError) {
                Text(
                    text = stringResource(R.string.error_review_update),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = BookLifeTextSizes.Small,
                    modifier = Modifier.padding(
                        start = BookLifeSpacing.Large,
                        end = BookLifeSpacing.Large,
                        bottom = BookLifeSpacing.Large,
                    ),
                )
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis ?: ReadDates.todayMillis())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        dateMillis = it
                        datePicked = true
                    }
                    showPicker = false
                }) { Text(stringResource(R.string.confirm).uppercase()) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.cancel).uppercase())
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
private fun AddReadBookDialogPreview() {
    BookLifeTheme {
        AddReadBookDialog(onSubmit = { _, _, _ -> }, onDismiss = {}, initialReadAt = "2017/5/7")
    }
}

private const val UNKNOWN_DATE = "-"
