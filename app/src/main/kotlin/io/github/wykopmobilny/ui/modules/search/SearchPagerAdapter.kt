package io.github.wykopmobilny.ui.modules.search

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.wykopmobilny.R
import io.github.wykopmobilny.ui.modules.search.entry.EntrySearchFragment
import io.github.wykopmobilny.ui.modules.search.links.LinkSearchFragment
import io.github.wykopmobilny.ui.modules.search.users.UsersSearchFragment

internal class SearchPagerAdapter(
    fragment: Fragment,
) : FragmentStateAdapter(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> LinkSearchFragment.newInstance()
        1 -> EntrySearchFragment.newInstance()
        2 -> UsersSearchFragment.newInstance()
        else -> error("Unsupported $position")
    }

    fun getTitle(position: Int) = when (position) {
        0 -> R.string.links
        1 -> R.string.entries
        2 -> R.string.profiles
        else -> error("Unsupported $position")
    }
}
