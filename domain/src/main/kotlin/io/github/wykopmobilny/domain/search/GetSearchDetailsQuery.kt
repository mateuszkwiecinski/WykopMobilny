package io.github.wykopmobilny.domain.search

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.wykopmobilny.data.storage.api.AppStorage
import io.github.wykopmobilny.domain.search.di.SearchScope
import io.github.wykopmobilny.domain.utils.safe
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.search.GetSearchDetails
import io.github.wykopmobilny.ui.search.SearchDetailsUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class GetSearchDetailsQuery @Inject constructor(
    private val appStorage: AppStorage,
    private val searchViewState: SearchViewStateStorage,
    private val appScopes: AppScopes,
) : GetSearchDetails {

    override fun invoke(): Flow<SearchDetailsUi> =
        combine(
            searchViewState.state,
            searchResultsFlow(),
        ) { viewState, searchResults ->
            SearchDetailsUi(
                query = viewState.query.orEmpty(),
                searchResults = searchResults,
                onQueryChanged = { value -> searchViewState.update { it.copy(query = value) } },
                onQuerySubmitted = viewState.query?.let { query ->
                    {
                        appScopes.applicationScope.launch {
                            appStorage.suggestionsQueries.insertOrReplace(query)
                        }
                    }
                },
            )
        }

    private fun searchResultsFlow() =
        searchViewState.state.flatMapLatest { viewState ->
            val query = viewState.query.orEmpty()
            if (query.length >= 2) {
                appStorage.suggestionsQueries.searchByText(query)
                    .asFlow()
                    .mapToList(AppDispatchers.Default)
            } else {
                flowOf(emptyList())
            }
        }
            .map { results ->
                results.map { result ->
                    SearchDetailsUi.Suggestion(
                        text = result,
                        onClick = {
                            appScopes.safe<SearchScope> { searchViewState.update { SearchViewState(query = result) } }
                        },
                    )
                }
            }
}
