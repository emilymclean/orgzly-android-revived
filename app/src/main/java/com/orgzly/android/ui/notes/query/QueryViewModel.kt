package com.orgzly.android.ui.notes.query

import androidx.compose.runtime.Immutable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.orgzly.BuildConfig
import com.orgzly.android.data.DataRepository
import com.orgzly.android.db.entity.NoteView
import com.orgzly.android.query.SimpleFilter
import com.orgzly.android.query.user.InternalQueryBuilder
import com.orgzly.android.query.user.InternalQueryParser
import com.orgzly.android.query.user.SimpleFilterMapper
import com.orgzly.android.query.user.UnsupportedSimpleFilterException
import com.orgzly.android.ui.AppBar
import com.orgzly.android.ui.CommonViewModel
import com.orgzly.android.ui.util.flatCombineLatest
import com.orgzly.android.util.LogUtils
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Immutable
data class QueryState(
    val query: String?,
    val filter: SimpleFilter?,
    val notes: List<NoteView>,
    val loading: QueryViewModel.ViewState
) {

    companion object {
        val default = QueryState(
            null,
            null,
            emptyList(),
            QueryViewModel.ViewState.LOADING
        )
    }

}

class QueryViewModel @AssistedInject constructor(
    private val dataRepository: DataRepository,
    private val queryParser: InternalQueryParser,
    private val queryBuilder: InternalQueryBuilder,
    private val filterMapper: SimpleFilterMapper
) : CommonViewModel() {

    data class Params(
        val query: String?,
        val defaultPriority: String
    )

    enum class ViewState {
        LOADING,
        LOADED,
        EMPTY
    }

    private val paramUpdateMutex = Mutex()

    private val notesParams = MutableStateFlow<Params?>(null)

    private val filter = MutableStateFlow<SimpleFilter?>(null)

    val state = flatCombineLatest(
        notesParams,
        filter
    ) { notesParams, filter ->
        notesParams?.query ?: return@flatCombineLatest flowOf(
            QueryState(
                "",
                filter,
                emptyList(),
                ViewState.LOADING
            )
        )

        dataRepository.selectNotesFromQueryFlow(notesParams.query).mapLatest { data ->
            QueryState(
                notesParams.query,
                filter,
                data,
                when (data.isEmpty()) {
                    true -> ViewState.EMPTY
                    else -> ViewState.LOADED
                },
            )
        }
    }.state(QueryState.default)

    @Deprecated("Use state")
    val viewState = state.mapLatest {
        it.loading
    }.asLiveData()

    @Deprecated("Use state")
    val data = state.mapLatest {
        it.notes
    }.asLiveData()

    val appBar: AppBar = AppBar(mapOf(
        APP_BAR_DEFAULT_MODE to null,
        APP_BAR_SELECTION_MODE to APP_BAR_DEFAULT_MODE))

    /* Triggers querying only if parameters changed. */
    fun refresh(query: String?, defaultPriority: String) {
        val params = Params(query, defaultPriority)
        viewModelScope.launch {
            paramUpdateMutex.withLock {
                if (BuildConfig.LOG_DEBUG) LogUtils.d(TAG, params)
                notesParams.value = params

                filter.value = params.query?.runCatching {
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
                val params = notesParams.value ?: return@launch

                val search = params.query?.let {
                    filterMapper.fromQuery(queryParser.parse(it))
                }?.search ?: ""

                val query = queryBuilder.build(
                    filterMapper.toQuery(
                        search,
                        filter.value ?: SimpleFilter()
                    )
                )

                notesParams.value = params.copy(
                    query = query,
                )
            }
        }
    }

    companion object {
        private val TAG = QueryViewModel::class.java.name

        const val APP_BAR_DEFAULT_MODE = 0
        const val APP_BAR_SELECTION_MODE = 1
    }
}