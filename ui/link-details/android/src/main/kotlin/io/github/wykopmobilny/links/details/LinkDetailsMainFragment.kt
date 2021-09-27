package io.github.wykopmobilny.links.details

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.link_details.android.R
import io.github.wykopmobilny.ui.link_details.android.databinding.FragmentLinkDetailsBinding
import io.github.wykopmobilny.utils.bindings.collectErrorDialog
import io.github.wykopmobilny.utils.destroyKeyedDependency
import io.github.wykopmobilny.utils.longArgument
import io.github.wykopmobilny.utils.longArgumentNullable
import io.github.wykopmobilny.utils.requireKeyedDependency
import io.github.wykopmobilny.utils.viewBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

fun linkDetailsFragment(linkId: Long, commentId: Long?): Fragment =
    LinkDetailsMainFragment()
        .apply {
            this.linkId = linkId
            this.commentId = commentId
        }

internal class LinkDetailsMainFragment : Fragment(R.layout.fragment_link_details) {

    var linkId by longArgument("userId")
    var commentId by longArgumentNullable("commentId")

    private lateinit var getLinkDetails: GetLinkDetails

    private val binding by viewBinding(FragmentLinkDetailsBinding::bind)

    override fun onAttach(context: Context) {
        getLinkDetails = context.requireKeyedDependency<LinkDetailsDependencies>(linkId).getLinkDetails()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        val adapter = LinkDetailsAdapter()
        binding.list.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val shared = getLinkDetails().stateIn(this)

            shared.map { it.toAdapterList() }
                .flowOn(AppDispatchers.Default)
                .collect { adapter.submitList(it) }
            shared.map { it.errorDialog }
                .collectErrorDialog(view.context)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().destroyKeyedDependency<LinkDetailsDependencies>(linkId)
    }
}
