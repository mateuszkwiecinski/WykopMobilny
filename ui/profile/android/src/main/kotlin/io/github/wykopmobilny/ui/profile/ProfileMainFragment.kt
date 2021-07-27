package io.github.wykopmobilny.ui.profile

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import io.github.wykopmobilny.ui.profile.android.R
import io.github.wykopmobilny.ui.profile.android.databinding.FragmentProfileBinding
import io.github.wykopmobilny.utils.bindings.collectErrorDialog
import io.github.wykopmobilny.utils.destroyKeyedDependency
import io.github.wykopmobilny.utils.requireKeyedDependency
import io.github.wykopmobilny.utils.stringArgument
import io.github.wykopmobilny.utils.viewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
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
        lifecycleScope.launchWhenResumed {
            val shared = getProfileDetails().stateIn(this)

            launch { shared.map { it.errorDialog }.collectErrorDialog(view.context) }
            launch { shared.map { it.contextMenuOptions }.collectMenuOptions(binding.toolbar, ::contextMenuMapping) }
            launch { shared.map { it.onAddEntryClicked }.bindOnClick(binding.addEntry) }
            launch {
                shared.map { it.header }.distinctUntilChanged().collect { header ->
                    when (header) {
                        ProfileHeaderUi.Loading -> Unit
                        is ProfileHeaderUi.WithData -> {
                            binding.signup.text = header.joinedAgo
                            binding.nickname.text = header.nick.name
                            binding.nickname.setTextColor(header.nick.color.toColorInt())
                            binding.description.isVisible = header.description != null
                            binding.description.text = header.description
                            binding.followers.text = resources.getQuantityString(
                                R.plurals.followers_count,
                                header.followersCount,
                                header.followersCount,
                            )
                            binding.rank.isVisible = header.avatarUi.rank != null
                            binding.rank.text = header.avatarUi.rank?.number?.let { "#$it" }
                            binding.rank.setBackgroundColor(header.avatarUi.rank?.color.toColorInt())
                            binding.genderStripImageView.isVisible = header.avatarUi.genderStrip != null
                            binding.genderStripImageView.setBackgroundColor(header.avatarUi.genderStrip.toColorInt())
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
                            Glide.with(binding.root).load(header.backgroundUrl).into(binding.backgroundImg)
                            Glide.with(binding.root).load(header.avatarUi.avatarUrl).into(binding.profilePicture)
                        }
                    }
                }
            }
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

@ColorInt
private fun ColorHex?.toColorInt(): Int = this?.hexValue?.let(Color::parseColor) ?: 0

private suspend fun <T : Enum<T>> Flow<List<ContextMenuOptionUi<T>>>.collectMenuOptions(
    toolbar: MaterialToolbar,
    mapping: (T) -> Pair<Int, Int?>,
) {
    distinctUntilChangedBy { it.map(ContextMenuOptionUi<T>::option) }.collect { options ->
        toolbar.menu.clear()
        options.forEach { menuOption ->
            val (textRes, imageRes) = mapping(menuOption.option)
            toolbar.menu.add(textRes).apply {
                setOnMenuItemClickListener { menuOption.onClick(); true }
                imageRes?.let(::setIcon)?.let { setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM) }
            }
        }
    }
}

private suspend fun Flow<() -> Unit>.bindOnClick(view: View) {
    collect { onClick -> view.setOnClickListener { onClick() } }
}
