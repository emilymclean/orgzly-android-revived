package com.orgzly.android.ui.notes.query

import androidx.compose.runtime.Immutable
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.orgzly.BuildConfig
import com.orgzly.android.data.DataRepository
import com.orgzly.android.db.entity.NoteView
import com.orgzly.android.query.SimpleFilter
import com.orgzly.android.query.user.InternalQueryBuilder
import com.orgzly.android.query.user.InternalQueryParser
import com.orgzly.android.query.user.SimpleFilterMapper
import com.orgzly.android.ui.AppBar
import com.orgzly.android.ui.CommonViewModel
import com.orgzly.android.ui.compose.base.EventFlow
import com.orgzly.android.ui.util.combine
import com.orgzly.android.util.LogUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class QueryViewModelOwner {
    AGENDA, SEARCH
}

@Immutable
data class QueryState(
    val query: String,
    val filter: SimpleFilter?,
    val notes: List<NoteView>,
    val allBooks: List<String>,
    val allTags: List<String>,
    val loading: QueryViewModel.ViewState,
    val showRefineButton: Boolean
) {

    companion object {
        val default = QueryState(
            "",
            null,
            emptyList(),
            emptyList(),
            emptyList(),
            QueryViewModel.ViewState.LOADING,
            true
        )
    }

}

sealed interface QueryEvent {
    data class ChangeQueryView(val query: String): QueryEvent
}

class QueryViewModel @AssistedInject constructor(
    private val dataRepository: DataRepository,
    private val queryParser: InternalQueryParser,
    private val queryBuilder: InternalQueryBuilder,
    private val filterMapper: SimpleFilterMapper,
    @Assisted private val owner: QueryViewModelOwner
) : CommonViewModel() {

    enum class ViewState {
        LOADING,
        LOADED,
        EMPTY
    }

    private val paramUpdateMutex = Mutex()

    private val query = MutableStateFlow<String?>(null)
    private val filter = MutableStateFlow<SimpleFilter?>(null)

    private val allTags = dataRepository.selectAllTagsLiveData().asFlow()
    private val allBooks = dataRepository.getBooksLiveData().asFlow().mapLatest {
        it.map { it.book.name }
    }
    private val queryResult = query.filterNotNull().flatMapLatest { query ->
        dataRepository.selectNotesFromQueryFlow(query)
    }

    val appBar: AppBar = AppBar(mapOf(
        APP_BAR_DEFAULT_MODE to null,
        APP_BAR_SELECTION_MODE to APP_BAR_DEFAULT_MODE))

    val state = combine(
        query,
        queryResult,
        allTags,
        allBooks,
        filter,
        appBar.currentMode
    ) { query, queryResult, allTags, allBooks, filter, currentMode ->
        val parsedQuery = runCatching {
            filterMapper.fromQuery(queryParser.parse(query ?: ""))
        }
        QueryState(
            parsedQuery.getOrNull()?.search ?: query ?: "",
            filter,
            queryResult,
            allBooks,
            allTags,
            when (queryResult.isEmpty()) {
                true -> ViewState.EMPTY
                else -> ViewState.LOADED
            },
            parsedQuery.isSuccess &&
                    currentMode == APP_BAR_DEFAULT_MODE
        )
    }.state(QueryState.default)

    private val _events = EventFlow<QueryEvent>()
    val events = _events.asFlow(viewModelScope)

    @Deprecated("Use state")
    val viewState = state.mapLatest {
        it.loading
    }.asLiveData()

    @Deprecated("Use state")
    val data = state.mapLatest {
        it.notes
    }.asLiveData()

    /* Triggers querying only if parameters changed. */
    fun refresh(query: String?, defaultPriority: String) {
        viewModelScope.launch {
            paramUpdateMutex.withLock {
                if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, query)
                this@QueryViewModel.query.value = query

                filter.value = query?.runCatching {
                    filterMapper.fromQuery(queryParser.parse(this)).filter
                }?.getOrNull()
            }
        }
    }

    fun updateFilter(update: SimpleFilter) {
        viewModelScope.launch {
            paramUpdateMutex.withLock {
                filter.value = update
            }
        }
    }

    fun commitFilter() {
        viewModelScope.launch {
            paramUpdateMutex.withLock {
                val search = filterMapper.fromQuery(queryParser.parse(
                    query.value ?: return@launch
                )).search

                val update = queryBuilder.build(
                    filterMapper.toQuery(
                        search,
                        filter.value ?: SimpleFilter()
                    )
                )

                val hasAgenda = filter.value?.agendaDays != null
                when (owner) {
                    QueryViewModelOwner.SEARCH if hasAgenda -> {
                        _events.send(QueryEvent.ChangeQueryView(update))
                    }
                    QueryViewModelOwner.AGENDA if !hasAgenda -> {
                        _events.send(QueryEvent.ChangeQueryView(update))
                    }
                    else -> {
                        query.value = update
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = QueryViewModel::class.java.name

        const val APP_BAR_DEFAULT_MODE = 0
        const val APP_BAR_SELECTION_MODE = 1
    }
}