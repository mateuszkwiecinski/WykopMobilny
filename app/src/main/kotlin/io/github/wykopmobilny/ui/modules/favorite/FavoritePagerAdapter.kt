package io.github.wykopmobilny.ui.modules.favorite

import android.content.res.Resources
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.github.wykopmobilny.R
import io.github.wykopmobilny.ui.modules.favorite.entry.EntryFavoriteFragment
import io.github.wykopmobilny.ui.modules.favorite.links.LinksFavoriteFragment

class FavoritePagerAdapter(
    private val resources: Resources,
    fragmentManager: FragmentManager,
) : androidx.fragment.app.FragmentPagerAdapter(fragmentManager) {

    val registeredFragments = SparseArray<Fragment>()

    override fun getItem(position: Int): Fragment = if (position == 0) {
        LinksFavoriteFragment.newInstance()
    } else {
        EntryFavoriteFragment.newInstance()
    }

    override fun getCount() = 2

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        registeredFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        registeredFragments.removeAt(position)
        super.destroyItem(container, position, `object`)
    }

    override fun getPageTitle(position: Int) = if (position == 0) {
        resources.getString(R.string.links)
    } else {
        resources.getString(R.string.entries)
    }
}
