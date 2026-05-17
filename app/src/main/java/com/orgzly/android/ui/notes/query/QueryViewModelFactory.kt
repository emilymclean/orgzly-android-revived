package com.orgzly.android.ui.notes.query

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.AssistedFactory

@AssistedFactory
interface QueryViewModelFactory : ViewModelProvider.Factory {

    fun create(
        owner: QueryViewModelOwner
    ): QueryViewModel

    companion object {
        fun provideFactory(
            assistedFactory: QueryViewModelFactory,
            owner: QueryViewModelOwner,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return assistedFactory.create(owner) as T
            }
        }
    }
}