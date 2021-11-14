package io.github.wykopmobilny.domain.linkdetails

import io.github.wykopmobilny.domain.linkdetails.di.LinkDetailsScope
import io.github.wykopmobilny.domain.utils.commentparsing.ExpandedSpoiler
import io.github.wykopmobilny.ui.base.Resource
import io.github.wykopmobilny.ui.base.components.OptionPickerUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@LinkDetailsScope
internal class LinkDetailsViewStateStorage @Inject constructor() {

    val state = MutableStateFlow(value = LinkDetailsViewState())

    fun update(updater: (LinkDetailsViewState) -> LinkDetailsViewState) {
        state.update(updater)
    }
}

data class LinkDetailsViewState(
    val generalResource: Resource = Resource.idle(),
    val relatedResource: Resource = Resource.idle(),
    val spoilerDialog: CharSequence? = null,
    val expandedSpoilers: Map<Long, Set<ExpandedSpoiler>> = emptyMap(),
    val expandedComments: Set<Long> = emptySet(),
    val collapsedIds: Set<Long> = emptySet(),
    val optionsVisibleIds: Set<Long> = emptySet(),
    val picker: OptionPickerUi? = null,
    val snackbar: String? = null,
    val forciblyShownBlockedComments: Set<Long> = emptySet(),
    val allowedNsfwImages: Set<String> = emptySet(),
)
