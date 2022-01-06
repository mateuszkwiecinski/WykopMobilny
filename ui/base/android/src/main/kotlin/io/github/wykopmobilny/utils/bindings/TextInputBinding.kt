package io.github.wykopmobilny.utils.bindings

import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import io.github.wykopmobilny.ui.base.components.TextInputUi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

suspend fun Flow<TextInputUi>.collectUserInput(editText: EditText) = coroutineScope {
    val state = stateIn(this)
    editText.doAfterTextChanged { text ->
        state.value.onChanged(text?.toString().orEmpty())
    }
    state.map { it.text }
        .filterNot { it == editText.text.toString() }
        .collect(editText::setText)
}
