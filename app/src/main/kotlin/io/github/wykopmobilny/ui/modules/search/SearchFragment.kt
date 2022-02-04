package io.github.wykopmobilny.ui.modules.search

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import com.google.android.material.tabs.TabLayoutMediator
import io.github.wykopmobilny.R
import io.github.wykopmobilny.base.BaseActivity
import io.github.wykopmobilny.base.BaseFragment
import io.github.wykopmobilny.databinding.ActivitySearchBinding
import io.github.wykopmobilny.ui.search.GetSearchDetails
import io.github.wykopmobilny.ui.search.SearchDependencies
import io.github.wykopmobilny.utils.destroyDependency
import io.github.wykopmobilny.utils.hideKeyboard
import io.github.wykopmobilny.utils.requireDependency
import io.github.wykopmobilny.utils.viewBinding
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder

class SearchFragment : BaseFragment(R.layout.activity_search) {

    companion object {
        fun newInstance() = SearchFragment()
    }

    val querySubject by lazy { PublishSubject.create<String>() }

    private lateinit var getSearchDetails: GetSearchDetails
    private lateinit var suggestionsAdapter: ArrayAdapter<String>

    private val binding by viewBinding(ActivitySearchBinding::bind)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getSearchDetails = context.requireDependency<SearchDependencies>().searchDetails()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        suggestionsAdapter = ArrayAdapter<String>(requireContext(), R.layout.history_suggestion_item, R.id.historySuggestion)

        val adapter = SearchPagerAdapter(this)
        binding.pager.adapter = adapter
        binding.pager.offscreenPageLimit = adapter.itemCount
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.setText(adapter.getTitle(position))
        }.attach()

        (activity as BaseActivity).supportActionBar?.setTitle(R.string.search)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.action_search) ?: return
        val searchView = item.actionView as SearchView

        searchView.findViewById<SearchView.SearchAutoComplete>(R.id.search_src_text).apply {
            setAdapter(suggestionsAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val clicked = suggestionsAdapter.getItem(position) ?: return@OnItemClickListener

                runBlocking { getSearchDetails().first().searchResults.firstOrNull { it.text == clicked }?.onClick?.invoke() }
                searchView.setQuery(clicked, false)
            }
        }

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    val callback = runBlocking { getSearchDetails().first().onQuerySubmitted } ?: return false

                    callback()
                    searchView.clearFocus()
                    querySubject.onNext(URLEncoder.encode(query, "UTF-8"))
                    activity?.hideKeyboard()

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val results = runBlocking {
                        val searchDetails = getSearchDetails().first()
                        searchDetails.onQueryChanged(newText.orEmpty())
                        getSearchDetails().first().searchResults.map { it.text }
                    }
                    suggestionsAdapter.clear()
                    suggestionsAdapter.addAll(results)

                    return true
                }
            },
        )
        searchView.setIconifiedByDefault(false)
        item.expandActionView()
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.destroyDependency<SearchDependencies>()
    }
}
