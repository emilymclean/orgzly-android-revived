package com.orgzly.android.ui.notes.query

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import cl.emilym.compose.units.rdp
import com.orgzly.R
import com.orgzly.android.query.SimpleFilter
import com.orgzly.android.ui.compose.base.PreviewOrgzlyBootstrap
import com.orgzly.android.ui.compose.modifiers.scaffoldPadding
import com.orgzly.android.ui.compose.widgets.OrgzlyButton
import com.orgzly.android.ui.savedsearch.SearchFilterWidget
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterScaffold(
    filter: SimpleFilter?,
    onFilterChange: (SimpleFilter) -> Unit,
    commitFilter: () -> Unit,
    allTags: List<String>,
    allBooks: List<String>,
    content: @Composable () -> Unit
) {
    var sheetVisible by remember { mutableStateOf(false) }

    Scaffold(
        Modifier.fillMaxSize()
    ) { contentPadding ->
        Box(Modifier.fillMaxSize()) {
            content()

            Box(
                Modifier
                    .fillMaxSize()
                    .scaffoldPadding(contentPadding)
            ) {
                OrgzlyButton(
                    onClick = {
                        sheetVisible = true
                    },
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 1.rdp)
                        .padding(bottom = 1.rdp)
                ) {
                    Text(
                        stringResource(R.string.query_filter_search)
                    )
                }

                filter?.let { filter ->
                    if (sheetVisible) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                commitFilter()
                                sheetVisible = false
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(1.rdp),
                                verticalArrangement = Arrangement.spacedBy(1.rdp)
                            ) {
                                SearchFilterWidget(
                                    filter,
                                    onFilterChange,
                                    allTags,
                                    allBooks
                                )

                                OrgzlyButton(
                                    onClick = {
                                        commitFilter()
                                        sheetVisible = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        stringResource(R.string.search)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchFilterScaffoldPreview() {
    PreviewOrgzlyBootstrap {
        SearchFilterScaffold(
            SimpleFilter(
                excludeDone = true,
            ),
            {},
            {},
            emptyList(),
            emptyList()
        ) { }
    }
}