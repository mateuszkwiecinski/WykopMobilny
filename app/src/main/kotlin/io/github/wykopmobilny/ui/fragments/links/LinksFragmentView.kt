package io.github.wykopmobilny.ui.fragments.links

import io.github.wykopmobilny.base.BaseView
import io.github.wykopmobilny.models.dataclass.Link

interface LinksFragmentView : BaseView {
    fun updateLink(link: Link)
    fun disableLoading()
    fun addItems(items: List<Link>, shouldRefresh: Boolean = false)
}
