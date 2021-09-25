package io.github.wykopmobilny.ui.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.wykopmobilny.ui.profile.android.R

internal class ProfilePagerAdapter(
    fragment: Fragment,
) : FragmentStateAdapter(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {

    override fun getItemCount() = 3

    fun getTitle(position: Int) =
        when (position) {
            0 -> R.string.profile_tab_title_actions
            1 -> R.string.profile_tab_title_links
            2 -> R.string.profile_tab_title_microblog
            else -> error("unsupported")
        }

    override fun createFragment(position: Int) =
        when (position) {
            0 -> ActionsFragment()
            1 -> LinksFragment()
            2 -> MicroblogFragment()
            else -> error("unsupported")
        }
}
