package io.github.wykopmobilny.links.details

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.github.wykopmobilny.ui.components.utils.dpToPx
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.link_details.android.R
import io.github.wykopmobilny.ui.link_details.android.databinding.FragmentLinkDetailsBinding
import io.github.wykopmobilny.utils.InjectableViewModel
import io.github.wykopmobilny.utils.bindings.collectErrorDialog
import io.github.wykopmobilny.utils.bindings.collectInfoDialog
import io.github.wykopmobilny.utils.bindings.collectMenuOptions
import io.github.wykopmobilny.utils.bindings.collectOptionPicker
import io.github.wykopmobilny.utils.bindings.collectSnackbar
import io.github.wykopmobilny.utils.bindings.collectSwipeRefresh
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt
import io.github.wykopmobilny.utils.longArgument
import io.github.wykopmobilny.utils.longArgumentNullable
import io.github.wykopmobilny.utils.viewModelWrapperFactoryKeyed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

fun linkDetailsFragment(linkId: Long, commentId: Long?): Fragment =
    LinkDetailsMainFragment()
        .apply {
            this.linkId = linkId
            this.commentId = commentId
        }

internal class LinkDetailsMainFragment : Fragment(R.layout.fragment_link_details) {

    var linkId by longArgument("userId")
    var commentId by longArgumentNullable("commentId")
    private val key
        get() = LinkDetailsKey(
            linkId = linkId,
            initialCommentId = commentId,
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel by viewModels<InjectableViewModel<LinkDetailsDependencies>> {
            viewModelWrapperFactoryKeyed<LinkDetailsKey, LinkDetailsDependencies>(key = key)
        }
        val getLinkDetails = viewModel.dependency.getLinkDetails()
        val binding = FragmentLinkDetailsBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        val adapter = LinkDetailsAdapter()
        adapter.stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.list.adapter = adapter
        (binding.list.itemAnimator as DefaultItemAnimator).apply {
            moveDuration = (resources.getInteger(android.R.integer.config_shortAnimTime) / 1.5).toLong()
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val shared = getLinkDetails()
                .flowOn(AppDispatchers.Default)
                .stateIn(this)

            launch {
                val adapterList = shared.map { it.toAdapterList() }
                    .flowOn(AppDispatchers.Default)

                val commentId = commentId
                if (savedInstanceState == null && commentId != null) {
                    runCatching {
                        withTimeout(3000) {
                            val state = adapterList.stateIn(this)
                            val targetElement = state.mapNotNull { list ->
                                list.indexOfFirst { item ->
                                    when (item) {
                                        is ListItem.Header,
                                        is ListItem.RelatedSection,
                                        -> false
                                        is ListItem.ParentComment -> item.id == commentId
                                        is ListItem.ReplyComment -> item.id == commentId
                                    }
                                }.takeIf { it >= 0 }
                            }.first()
                            adapter.submitList(state.value) {
                                binding.appBarLayout.setExpanded(false, false)
                                val linearLayoutManager = binding.list.layoutManager as LinearLayoutManager
                                linearLayoutManager.scrollToPositionWithOffset(targetElement, 8.dpToPx(resources))
                            }
                        }
                    }
                        .onFailure { Napier.w("Couldn't find target comment key=$key") }
                }
                adapterList.collect { adapter.submitList(it) }
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
                                binding.parallaxContainer.isVisible = header.previewImageUrl != null
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
                            }
                        }
                    }
            }
        }
    }
}
