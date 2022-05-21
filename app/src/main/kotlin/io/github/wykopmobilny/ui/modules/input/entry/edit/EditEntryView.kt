package io.github.wykopmobilny.ui.modules.input.entry.edit

import io.github.wykopmobilny.models.dataclass.Embed
import io.github.wykopmobilny.ui.modules.input.BaseInputView

interface EditEntryView : BaseInputView {
    val entryId: Long
    val embed: Embed?
}
