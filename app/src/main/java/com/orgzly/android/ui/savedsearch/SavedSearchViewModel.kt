package com.orgzly.android.ui.savedsearch

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.orgzly.android.data.DataRepository
import com.orgzly.android.query.QueryParser
import com.orgzly.android.query.SimpleFilter
import com.orgzly.android.query.SimpleFilterParser
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
    val state: State = State.Simple(SimpleFilter())
) {

    sealed interface State {

        data object Advanced: State

        data class Simple(
            val filter: SimpleFilter
        ): State

    }

}

class SavedSearchViewModel @AssistedInject constructor(
    private val dataRepository: DataRepository,
    private val simpleFilterParser: SimpleFilterParser,
    private val queryParser: QueryParser = InternalQueryParser(),
    @Assisted private val existingSearchId: Long?
): CommonViewModel() {

    companion object {
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

    val state = combine(
        isSimpleSearch,
        currentSimpleFilter
    ) { isSimpleSearch, currentSimpleFilter ->
        SavedSearchModel(
            when (isSimpleSearch) {
                true -> SavedSearchModel.State.Simple(
                    currentSimpleFilter
                )
                else -> SavedSearchModel.State.Advanced
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
                    val parsed = simpleFilterParser.fromQuery(
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
        if (isSimpleSearch.value) {

        } else {

        }
    }

    @AssistedFactory
    interface Factory {
        fun create(existingSearchId: Long?): SavedSearchViewModel
    }

}