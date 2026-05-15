package com.orgzly.android.ui.savedsearch

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.toLowerCase
import cl.emilym.compose.units.rdp
import com.orgzly.R
import com.orgzly.android.prefs.AppPreferences
import com.orgzly.android.prefs.StateWorkflows
import com.orgzly.android.query.SimpleFilter
import com.orgzly.android.query.SimpleSortOrder
import com.orgzly.android.ui.compose.modifiers.noRippleClickable
import com.orgzly.android.ui.compose.providers.appPreference
import com.orgzly.android.ui.compose.widgets.CheckboxFormLockup
import com.orgzly.android.ui.compose.widgets.CollapsePanel
import com.orgzly.android.ui.compose.widgets.Icons
import com.orgzly.android.ui.compose.widgets.RadioButtonFormLockup
import com.orgzly.android.ui.compose.widgets.painterIcon
import java.util.Locale
import java.util.Locale.getDefault
import kotlin.text.equals

@Composable
fun SearchFilterWidget(
    filter: SimpleFilter,
    onChange: (SimpleFilter) -> Unit,
    allTags: List<String>,
    allBooks: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.then(modifier),
        verticalArrangement = Arrangement.spacedBy(1.rdp)
    ) {
        Column {
            CheckboxFormLockup(
                filter.excludeDone,
                {
                    onChange(
                        filter.copy(
                            excludeDone = it
                        )
                    )
                },
                stringResource(R.string.search_filter_exclude_done),
                modifier = Modifier.fillMaxWidth()
            )

            CheckboxFormLockup(
                filter.isAgenda,
                {
                    onChange(
                        filter.copy(
                            isAgenda = it
                        )
                    )
                },
                stringResource(R.string.search_filter_show_as_agenda),
                modifier = Modifier.fillMaxWidth()
            )
        }

        SortOrder(
            filter.sortOrder,
            filter.sortDescending,
            { sortOrder, descending ->
                onChange(
                    filter.copy(
                        sortOrder = sortOrder,
                        sortDescending = descending
                    )
                )
            }
        )

        StateFilter(
            filter.states,
            {
                onChange(
                    filter.copy(
                        states = it
                    )
                )
            }
        )

        TagsFilter(
            filter.tags,
            {
                onChange(
                    filter.copy(
                        tags = it
                    )
                )
            },
            allTags
        )

        if (allBooks.size > 1) {
            BookFilter(
                filter.books,
                { onChange(
                    filter.copy(
                        books = it
                    )
                ) },
                allBooks
            )
        }
    }
}

private val dropdownPadding: PaddingValues
    @Composable
    get() = PaddingValues(
        all = 1.rdp
    )

private data class SimpleSortOrderEntry(
    val sortOrder: SimpleSortOrder,
    @field:StringRes
    val label: Int
)

private val sortOrderEntries = listOf(
    SimpleSortOrderEntry(
        SimpleSortOrder.DEFAULT,
        R.string.search_filter_default_sort_order
    ),
    SimpleSortOrderEntry(
        SimpleSortOrder.BOOK,
        R.string.search_filter_book_sort_order
    ),
    SimpleSortOrderEntry(
        SimpleSortOrder.TITLE,
        R.string.search_filter_title_sort_order
    ),
    SimpleSortOrderEntry(
        SimpleSortOrder.PRIORITY,
        R.string.search_filter_priority_sort_order
    ),
    SimpleSortOrderEntry(
        SimpleSortOrder.STATE,
        R.string.search_filter_state_sort_order
    ),
    SimpleSortOrderEntry(
        SimpleSortOrder.SCHEDULED,
        R.string.search_filter_scheduled_sort_order
    ),
    SimpleSortOrderEntry(
        SimpleSortOrder.DEADLINE,
        R.string.search_filter_deadline_sort_order
    ),
    SimpleSortOrderEntry(
        SimpleSortOrder.EVENT,
        R.string.search_filter_event_sort_order
    ),
    SimpleSortOrderEntry(
        SimpleSortOrder.CLOSED,
        R.string.search_filter_closed_sort_order
    ),
    SimpleSortOrderEntry(
        SimpleSortOrder.CREATED,
        R.string.search_filter_created_sort_order
    ),
)

@Composable
private fun SortOrder(
    sortOrder: SimpleSortOrder,
    descending: Boolean,
    onSortOrderChange: (SimpleSortOrder, Boolean) -> Unit
) {

    var collapsed by remember { mutableStateOf(true) }
    CollapsePanel(
        stringResource(R.string.sort_order),
        collapsed,
        { collapsed = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(dropdownPadding)
        ) {
            for (entry in sortOrderEntries) {
                SortOrderEntry(
                    entry,
                    sortOrder,
                    descending,
                    onSortOrderChange
                )
            }
        }
    }
}

@Composable
private fun SortOrderEntry(
    entry: SimpleSortOrderEntry,
    sortOrder: SimpleSortOrder,
    descending: Boolean,
    onSortOrderChange: (SimpleSortOrder, Boolean) -> Unit
) {
    val callback = remember(onSortOrderChange) { {
        onSortOrderChange(
            entry.sortOrder,
            when (entry.sortOrder == sortOrder) {
                true -> !descending
                else -> descending
            }
        )
    } }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButtonFormLockup(
            entry.sortOrder == sortOrder,
            callback,
            stringResource(entry.label),
            modifier = Modifier
                .weight(1f)
                .animateContentSize()
        )

        val rotationAnimation by animateFloatAsState(
            when (descending) {
                true -> 0f
                else -> 180f
            }
        )
        AnimatedVisibility(
            entry.sortOrder == sortOrder &&
                    entry.sortOrder != SimpleSortOrder.DEFAULT,
            enter = expandIn(),
            exit = shrinkOut()
        ) {
            Icon(
                painterIcon(
                    Icons.ARROW_DOWNWARD
                ),
                modifier = Modifier
                    .padding(end = 1.rdp)
                    .noRippleClickable(onClick = callback)
                    .rotate(rotationAnimation),
                contentDescription = stringResource(
                    when (descending) {
                        true -> R.string.content_description_sort_order_descending
                        else -> R.string.content_description_sort_order_ascending
                    }
                )
            )
        }
    }
}

@Composable
private fun TagsFilter(
    tags: Set<String>,
    onTagChange: (Set<String>) -> Unit,
    allTags: List<String>
) {
    var collapsed by remember { mutableStateOf(true) }
    CollapsePanel(
        stringResource(R.string.tags),
        collapsed,
        { collapsed = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(dropdownPadding)
        ) {
            for (tag in allTags) {
                CheckboxFormLockup(
                    tags.any {
                        it.equals(tag, ignoreCase = true)
                    },
                    onCheckedChange = {
                        onTagChange(
                            when (it) {
                                true -> tags + tag
                                else -> tags.filterNot {
                                    it.equals(tag, ignoreCase = true)
                                }.toSet()
                            }
                        )
                    },
                    tag,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StateFilter(
    states: Set<String>,
    onStateChange: (Set<String>) -> Unit,
) {
    val allStatesString by appPreference { AppPreferences.states(it) }
    val allStates = remember(allStatesString) {
        StateWorkflows(allStatesString).flatMap {
            (it.todoKeywords?.toList() ?: emptyList<String>()) +
            (it.doneKeywords?.toList() ?: emptyList<String>())
        }
    }

    var collapsed by remember { mutableStateOf(true) }
    CollapsePanel(
        stringResource(R.string.states),
        collapsed,
        { collapsed = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(dropdownPadding)
        ) {
            for (state in allStates) {
                CheckboxFormLockup(
                    states.any {
                        it.equals(state, ignoreCase = true)
                    },
                    onCheckedChange = {
                        onStateChange(
                            when (it) {
                                true -> states + state
                                else -> states.filterNot {
                                    it.equals(state, ignoreCase = true)
                                }.toSet()
                            }
                        )
                    },
                    state,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BookFilter(
    books: Set<String>,
    onBooksChange: (Set<String>) -> Unit,
    allBooks: List<String>
) {
    var collapsed by remember { mutableStateOf(true) }
    CollapsePanel(
        stringResource(R.string.notebooks),
        collapsed,
        { collapsed = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(dropdownPadding)
        ) {
            for (book in allBooks) {
                CheckboxFormLockup(
                    books.contains(book),
                    onCheckedChange = {
                        onBooksChange(
                            when (it) {
                                true -> books + book
                                else -> books.filterNot {
                                    it == book
                                }.toSet()
                            }
                        )
                    },
                    book,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}