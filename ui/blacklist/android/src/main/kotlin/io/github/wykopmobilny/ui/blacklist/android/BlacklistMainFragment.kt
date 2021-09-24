package io.github.wykopmobilny.ui.blacklist.android

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import io.github.wykopmobilny.ui.blacklist.BlacklistDependencies
import io.github.wykopmobilny.ui.blacklist.BlacklistedDetailsUi
import io.github.wykopmobilny.ui.blacklist.GetBlacklistDetails
import io.github.wykopmobilny.ui.blacklist.android.databinding.FragmentBlacklistMainBinding
import io.github.wykopmobilny.utils.bindings.collectErrorDialog
import io.github.wykopmobilny.utils.bindings.collectSnackbar
import io.github.wykopmobilny.utils.destroyDependency
import io.github.wykopmobilny.utils.requireDependency
import io.github.wykopmobilny.utils.viewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun blacklistMainFragment(): Fragment = BlacklistMainFragment()

internal class BlacklistMainFragment : Fragment(R.layout.fragment_blacklist_main) {

    private val binding by viewBinding(FragmentBlacklistMainBinding::bind)

    lateinit var getBlacklistDetails: GetBlacklistDetails

    override fun onAttach(context: Context) {
        getBlacklistDetails = context.requireDependency<BlacklistDependencies>().blacklistDetails()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val shared = getBlacklistDetails().stateIn(this)

            val blacklistAdapter = BlacklistAdapter(this@BlacklistMainFragment)
            binding.viewPager.adapter = blacklistAdapter
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.setText(blacklistAdapter.getTitle(position))
            }
                .attach()
            launch { shared.map { it.errorDialog }.collectErrorDialog(requireContext()) }
            launch { shared.map { it.snackbar }.collectSnackbar(binding.root) }
            launch { shared.collectState() }
        }
    }

    private suspend fun Flow<BlacklistedDetailsUi>.collectState() {
        map { it.content }
            .collect { content ->
                when (content) {
                    is BlacklistedDetailsUi.Content.Empty -> {
                        binding.viewPager.isVisible = false
                        binding.tabLayout.isVisible = false
                        binding.emptyContainer.isVisible = true
                        binding.btnImport.isVisible = !content.isLoading
                        binding.btnProgress.isVisible = content.isLoading
                        binding.btnImport.setOnClickListener { content.loadAction() }
                    }
                    is BlacklistedDetailsUi.Content.WithData -> {
                        binding.emptyContainer.isVisible = false
                        binding.btnImport.setOnClickListener(null)
                        binding.viewPager.isVisible = true
                        binding.tabLayout.isVisible = true
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().destroyDependency<BlacklistDependencies>()
    }
}
