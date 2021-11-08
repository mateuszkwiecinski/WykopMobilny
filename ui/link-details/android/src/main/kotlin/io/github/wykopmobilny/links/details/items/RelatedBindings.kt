package io.github.wykopmobilny.links.details.items

import io.github.aakira.napier.Napier
import io.github.wykopmobilny.links.details.RelatedLinksSectionUi
import io.github.wykopmobilny.ui.link_details.android.databinding.LinkDetailsRelatedBinding

internal fun LinkDetailsRelatedBinding.bindRelated(related: RelatedLinksSectionUi) {
    Napier.v("related ${related.hashCode()} ${hashCode()}")
}
