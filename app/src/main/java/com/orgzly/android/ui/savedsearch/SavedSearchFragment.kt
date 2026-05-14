package com.orgzly.android.ui.savedsearch

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import com.orgzly.android.App
import com.orgzly.android.db.entity.SavedSearch
import com.orgzly.android.ui.compose.base.ComposeFragment
import com.orgzly.android.ui.drawer.DrawerItem
import com.orgzly.android.ui.main.SharedMainActivityViewModel
import com.orgzly.android.ui.savedsearches.SavedSearchesFragment

class SavedSearchFragment: ComposeFragment(), DrawerItem {

    companion object {
        private val TAG: String = SavedSearchFragment::class.java.getName()

        private const val ARG_ID: String = "id"

        /** Name used for [android.app.FragmentManager].  */
        val FRAGMENT_TAG: String = SavedSearchFragment::class.java.getName()

        fun getInstance(): SavedSearchFragment {
            return SavedSearchFragment()
        }

        fun getInstance(id: Long): SavedSearchFragment {
            val fragment = SavedSearchFragment()
            val args = Bundle()

            args.putLong(ARG_ID, id)

            fragment.setArguments(args)

            return fragment
        }
    }

    private lateinit var sharedMainActivityViewModel: SharedMainActivityViewModel
    private var mListener: Listener? = null

    @Composable
    override fun Content() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedMainActivityViewModel = ViewModelProvider(
            requireActivity()
        )[SharedMainActivityViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        App.appComponent.inject(this)

        /* This makes sure that the container activity has implemented
         * the callback interface. If not, it throws an exception
         */
        try {
            mListener = activity as Listener?
        } catch (e: ClassCastException) {
            throw ClassCastException(requireActivity().toString() + " must implement " + Listener::class.java)
        }
    }

    override fun getCurrentDrawerItemId() = SavedSearchesFragment.getDrawerItemId()


    interface Listener {
        fun onSavedSearchCreateRequest(savedSearch: SavedSearch?)
        fun onSavedSearchUpdateRequest(savedSearch: SavedSearch?)
        fun onSavedSearchCancelRequest()
    }

}