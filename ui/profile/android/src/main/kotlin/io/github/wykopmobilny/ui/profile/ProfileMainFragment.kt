package io.github.wykopmobilny.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.tabs.TabLayoutMediator
import io.github.wykopmobilny.ui.profile.android.R
import io.github.wykopmobilny.ui.profile.android.databinding.FragmentProfileBinding
import io.github.wykopmobilny.utils.bindings.collectErrorDialog
import io.github.wykopmobilny.utils.bindings.collectMenuOptions
import io.github.wykopmobilny.utils.bindings.setOnClick
import io.github.wykopmobilny.utils.bindings.toColorInt
import io.github.wykopmobilny.utils.destroyKeyedDependency
import io.github.wykopmobilny.utils.requireKeyedDependency
import io.github.wykopmobilny.utils.stringArgument
import io.github.wykopmobilny.utils.viewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun profileMainFragment(userId: String): Fragment =
    ProfileMainFragment()
        .apply { this.userId = userId }

internal class ProfileMainFragment : Fragment(R.layout.fragment_profile) {

    var userId by stringArgument("userId")

    private lateinit var getProfileDetails: GetProfileDetails

    private val binding by viewBinding(FragmentProfileBinding::bind)

    override fun onAttach(context: Context) {
        getProfileDetails = context.requireKeyedDependency<ProfileDependencies>(userId).profileDetails()
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        val adapter = ProfilePagerAdapter(this)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setText(adapter.getTitle(position))
        }
            .attach()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val shared = getProfileDetails().stateIn(this)

            launch { shared.map { it.errorDialog }.collectErrorDialog(view.context) }
            launch { shared.map { it.contextMenuOptions }.collectMenuOptions(binding.toolbar, ::contextMenuMapping) }
            launch { shared.map { it.onAddEntryClicked }.setOnClick(binding.addEntry) }
            launch { shared.map { it.header }.bindHeader(binding) }
        }
    }

    private suspend fun Flow<ProfileHeaderUi>.bindHeader(binding: FragmentProfileBinding) {
        distinctUntilChanged().collect { header ->
            binding.signup.text = header.joinedAgo
            binding.nickname.text = header.userInfo?.name
            binding.nickname.setTextColor(header.userInfo?.color.toColorInt(requireContext()))
            binding.description.isVisible = header.description != null
            binding.description.text = header.description
            binding.followers.text = header.followersCount?.let { followers ->
                resources.getQuantityString(R.plurals.followers_count, followers, followers)
            }
            binding.rank.isVisible = header.userInfo?.avatar?.rank != null
            binding.rank.text = header.userInfo?.avatar?.rank?.let { "#$it" }
            binding.rank.setBackgroundColor(header.userInfo?.color.toColorInt(requireContext()).defaultColor)
            binding.genderStripImageView.isVisible = header.userInfo?.avatar?.genderStrip != null
            binding.genderStripImageView.setBackgroundColor(header.userInfo?.avatar?.genderStrip.toColorInt(requireContext()).defaultColor)
            binding.banTextView.isVisible = header.banReason != null
            binding.banTextView.text = header.banReason?.let { reason ->
                if (reason.reason == null) {
                    if (reason.endDate == null) {
                        getString(R.string.banned_no_info)
                    } else {
                        getString(R.string.banned_date_only, reason.endDate)
                    }
                } else if (reason.endDate == null) {
                    getString(R.string.banned_reason_only, reason.reason)
                } else {
                    getString(R.string.banned_date_and_reason, reason.endDate, reason.reason)
                }
            }
            Glide.with(binding.root).load(header.backgroundUrl).transition(withCrossFade()).into(binding.backgroundImg)
            Glide.with(binding.root).load(header.userInfo?.avatar?.avatarUrl).transition(withCrossFade()).into(binding.profilePicture)
        }
    }

    private fun contextMenuMapping(option: ProfileMenuOption) =
        when (option) {
            ProfileMenuOption.PrivateMessage -> R.string.private_message to R.drawable.ic_pw
            ProfileMenuOption.Unblock -> R.string.unblock_user to null
            ProfileMenuOption.Block -> R.string.block_user to null
            ProfileMenuOption.ObserveProfile -> R.string.observe_user to null
            ProfileMenuOption.UnobserveProfile -> R.string.unobserve_user to null
            ProfileMenuOption.Badges -> R.string.badges to null
            ProfileMenuOption.Report -> R.string.report to null
        }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().destroyKeyedDependency<ProfileDependencies>(userId)
    }
}
