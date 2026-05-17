package com.orgzly.android.ui.notes.query

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.AssistedFactory

@AssistedFactory
interface QueryViewModelFactory : ViewModelProvider.Factory {

    fun create(): QueryViewModel

    companion object {
        fun provideFactory(
            assistedFactory: QueryViewModelFactory,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return assistedFactory.create() as T
            }
        }
    }
}