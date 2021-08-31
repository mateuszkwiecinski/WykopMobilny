package io.github.wykopmobilny.domain.search

import io.github.wykopmobilny.domain.search.di.SearchScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@SearchScope
internal class SearchViewStateStorage @Inject constructor() {

    val state = MutableStateFlow(value = SearchViewState())

    fun update(updater: (SearchViewState) -> SearchViewState) {
        state.update(updater)
    }
}

data class SearchViewState(
    val query: String? = null,
)
