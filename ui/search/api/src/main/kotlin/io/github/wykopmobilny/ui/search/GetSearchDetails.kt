package io.github.wykopmobilny.ui.search

import io.github.wykopmobilny.ui.base.Query

interface GetSearchDetails : Query<SearchDetailsUi>

data class SearchDetailsUi(
    val query: String,
    val searchResults: List<Suggestion>,
    val onQueryChanged: (String) -> Unit,
    val onQuerySubmitted: (() -> Unit)?,
) {

    data class Suggestion(
        val text: String,
        val onClick: () -> Unit,
    )
}
