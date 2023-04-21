package io.github.wykopmobilny.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.wykopmobilny.ui.components.entries.bindEntries
import io.github.wykopmobilny.ui.profile.android.R
import io.github.wykopmobilny.ui.profile.android.databinding.FragmentLinksBinding
import io.github.wykopmobilny.utils.requireKeyedDependency
import io.github.wykopmobilny.utils.viewBinding
import kotlinx.coroutines.launch

internal class ActionsFragment : Fragment(R.layout.fragment_links) {

    private lateinit var getProfileActions: GetProfileActions
    private val binding by viewBinding(FragmentLinksBinding::bind)

    override fun onAttach(context: Context) {
        val userId = (parentFragment as ProfileMainFragment).userId
        getProfileActions = context.requireKeyedDependency<ProfileDependencies>(userId).profileLinks()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                getProfileActions().bindEntries(
                    recyclerView = binding.recyclerView,
                    swipeRefreshLayout = binding.swipeRefreshLayout,
                )
            }
        }
    }
}

internal class LinksFragment : Fragment()

internal class MicroblogFragment : Fragment()
