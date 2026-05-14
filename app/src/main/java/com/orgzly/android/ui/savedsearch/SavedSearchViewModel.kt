package com.orgzly.android.ui.savedsearch

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.orgzly.android.data.DataRepository
import com.orgzly.android.query.SimpleFilter
import com.orgzly.android.query.user.SimpleFilterMapper
import com.orgzly.android.query.user.InternalQueryBuilder
import com.orgzly.android.query.user.InternalQueryParser
import com.orgzly.android.ui.CommonViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class SavedSearchModel(
    val mode: Mode = Mode.Simple(SimpleFilter())
) {

    sealed interface Mode {

        data object Advanced: Mode

        data class Simple(
            val filter: SimpleFilter
        ): Mode

    }

}

class SavedSearchViewModel @AssistedInject constructor(
    private val dataRepository: DataRepository,
    private val simpleFilterMapper: SimpleFilterMapper,
    private val queryParser: InternalQueryParser,
    private val queryBuilder: InternalQueryBuilder,
    @Assisted private val existingSearchId: Long?
): CommonViewModel() {

    companion object {
        val TAG = SavedSearchViewModel::class.java.name

        fun provideFactory(
            assistedFactory: Factory,
            existingSearchId: Long?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return assistedFactory.create(existingSearchId) as T
            }
        }
    }

    private val isSimpleSearch = MutableStateFlow(true)

    private var currentSimpleFilter = MutableStateFlow(SimpleFilter())

    val nameField = TextFieldState()
    val advancedQueryField = TextFieldState()
    val simpleSearchField = TextFieldState()

    val mode = combine(
        isSimpleSearch,
        currentSimpleFilter
    ) { isSimpleSearch, currentSimpleFilter ->
        SavedSearchModel(
            when (isSimpleSearch) {
                true -> SavedSearchModel.Mode.Simple(
                    currentSimpleFilter
                )
                else -> SavedSearchModel.Mode.Advanced
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SavedSearchModel()
    )


    init {
        existingSearchId?.let {
            viewModelScope.launch {
                val existing = withContext(Dispatchers.IO) {
                    dataRepository.getSavedSearch(existingSearchId)
                } ?: return@launch
                nameField.setTextAndPlaceCursorAtEnd(existing.name)

                try {
                    val parsed = simpleFilterMapper.fromQuery(
                        queryParser.parse(existing.query)
                    )
                    currentSimpleFilter.value = parsed.filter
                    simpleSearchField.setTextAndPlaceCursorAtEnd(parsed.search)
                } catch (e: Exception) {
                    advancedQueryField.setTextAndPlaceCursorAtEnd(existing.query)
                    isSimpleSearch.value = false
                }
            }
        }
    }

    fun switchSearchStyle() {
        val isSimpleSearch = isSimpleSearch.value
        when (isSimpleSearch) {
            true -> {
                advancedQueryField.setTextAndPlaceCursorAtEnd(
                    queryBuilder.build(simpleFilterMapper.toQuery(
                        simpleSearchField.text.toString(),
                        currentSimpleFilter.value
                    ))
                )
                this.isSimpleSearch.value = false
            }
            else -> {
                try {
                    val parsed = simpleFilterMapper.fromQuery(
                        queryParser.parse(advancedQueryField.text.toString())
                    )
                    currentSimpleFilter.value = parsed.filter
                    simpleSearchField.setTextAndPlaceCursorAtEnd(parsed.search)
                    this.isSimpleSearch.value = true
                } catch (e: Exception) {
                    Log.e(TAG, "Cannot swap to simple search", e)
                }
            }
        }
    }

    fun updateFilter(filter: SimpleFilter) {
        this.currentSimpleFilter.value = filter
    }

    @AssistedFactory
    interface Factory {
        fun create(existingSearchId: Long?): SavedSearchViewModel
    }

}