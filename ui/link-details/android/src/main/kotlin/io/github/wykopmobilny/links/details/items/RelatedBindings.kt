package io.github.wykopmobilny.links.details.items

import io.github.aakira.napier.Napier
import io.github.wykopmobilny.links.details.RelatedLinkUi
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsRelatedBinding

internal fun LinkDetailsRelatedBinding.bindRelated(related: List<RelatedLinkUi>) {
    Napier.v("related ${related.hashCode()} ${hashCode()}")
}
