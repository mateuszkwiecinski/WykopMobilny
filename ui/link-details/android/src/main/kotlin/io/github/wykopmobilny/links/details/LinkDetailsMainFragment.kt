package io.github.wykopmobilny.links.details

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.link_details.android.R
import io.github.wykopmobilny.ui.link_details.android.databinding.FragmentLinkDetailsBinding
import io.github.wykopmobilny.utils.bindings.collectErrorDialog
import io.github.wykopmobilny.utils.bindings.collectInfoDialog
import io.github.wykopmobilny.utils.bindings.collectMenuOptions
import io.github.wykopmobilny.utils.bindings.collectOptionPicker
import io.github.wykopmobilny.utils.bindings.collectSnackbar
import io.github.wykopmobilny.utils.bindings.collectSwipeRefresh
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt
import io.github.wykopmobilny.utils.destroyKeyedDependency
import io.github.wykopmobilny.utils.longArgument
import io.github.wykopmobilny.utils.longArgumentNullable
import io.github.wykopmobilny.utils.requireKeyedDependency
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    override fun onAttach(context: Context) {
        getLinkDetails = context.requireKeyedDependency<LinkDetailsDependencies>(linkId).getLinkDetails()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLinkDetailsBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        val adapter = LinkDetailsAdapter()
        binding.list.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val shared = getLinkDetails()
                .flowOn(AppDispatchers.Default)
                .stateIn(this)

            launch {
                shared.map { it.toAdapterList() }
                    .flowOn(AppDispatchers.Default)
                    .collect { adapter.submitList(it) }
            }
            launch { shared.map { it.errorDialog }.collectErrorDialog(view.context) }
            launch { shared.map { it.infoDialog }.collectInfoDialog(view.context) }
            launch { shared.map { it.swipeRefresh }.collectSwipeRefresh(binding.swipeRefresh) }
            launch { shared.map { it.contextMenuOptions }.collectMenuOptions(binding.toolbar) }
            launch { shared.map { it.picker }.collectOptionPicker(view.context) }
            launch { shared.map { it.snackbar }.collectSnackbar(view) }
            launch {
                shared.map { it.header }
                    .collect { header ->
                        when (header) {
                            LinkDetailsHeaderUi.Loading -> {
                                binding.parallaxContainer.isInvisible = true
                            }
                            is LinkDetailsHeaderUi.WithData -> {
                                if (header.previewImageUrl != null) {
                                    Glide.with(this@LinkDetailsMainFragment)
                                        .load(header.previewImageUrl)
                                        .transition(withCrossFade())
                                        .into(binding.imgPreview)
                                }
                                binding.imgPreview.setOnClick(header.viewLinkAction)
                                binding.txtDomain.text = header.domain
                                binding.hotBadgeStrip.isVisible = header.badge != null
                                binding.hotBadgeStrip.setBackgroundColor(header.badge.toColorInt(view.context).defaultColor)
                                binding.parallaxContainer.isVisible = header.previewImageUrl != null
                            }
                        }
                    }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().destroyKeyedDependency<LinkDetailsDependencies>(linkId)
    }
}
