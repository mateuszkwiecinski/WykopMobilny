package io.github.wykopmobilny.ui.modules.input.entry.comment

import io.github.wykopmobilny.models.dataclass.Embed
import io.github.wykopmobilny.ui.modules.input.BaseInputView

interface EditEntryCommentView : BaseInputView {
    val entryId: Long
    val commentId: Long
    val embed: Embed?
}
