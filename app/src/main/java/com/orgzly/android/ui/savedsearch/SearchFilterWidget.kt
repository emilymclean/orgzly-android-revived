package com.orgzly.android.ui.savedsearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cl.emilym.compose.units.rdp
import com.orgzly.R
import com.orgzly.android.query.SimpleFilter
import com.orgzly.android.query.StateType
import com.orgzly.android.ui.compose.widgets.CheckboxFormLockup

@Composable
fun SearchFilterWidget(
    filter: SimpleFilter,
    onChange: (SimpleFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.then(modifier),
        verticalArrangement = Arrangement.spacedBy(1.rdp)
    ) {
        CheckboxFormLockup(
            filter.excludeDone,
            {
                onChange(
                    filter.copy(
                        excludeDone = it,
                        stateTypes = when (it) {
                            true -> filter.stateTypes.filter { it != StateType.DONE }.toSet()
                            else -> filter.stateTypes
                        }
                    )
                )
            },
            stringResource(R.string.search_filter_exclude_done),
            modifier = Modifier.fillMaxWidth()
        )


    }
}