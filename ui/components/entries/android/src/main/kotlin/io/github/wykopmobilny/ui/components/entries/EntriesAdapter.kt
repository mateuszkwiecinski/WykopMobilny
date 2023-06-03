package io.github.wykopmobilny.ui.components.entries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.github.wykopmobilny.kotlin.AppDispatchers
import io.github.wykopmobilny.ui.components.entries.android.databinding.ItemLinkBinding
import io.github.wykopmobilny.ui.components.widgets.ListElementUi
import io.github.wykopmobilny.ui.components.widgets.id
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

suspend fun Flow<PagingData<ListElementUi>>.bindEntries(recyclerView: RecyclerView, swipeRefreshLayout: SwipeRefreshLayout) =
    coroutineScope {
        val adapter = EntriesAdapter()
        launch {
            swipeRefreshLayout.setOnRefreshListener { adapter.refresh() }
            adapter.loadStateFlow.collect { swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading }
        }
        launch {
            recyclerView.adapter = adapter
            collectLatest { adapter.submitData(it) }
        }
    }

internal class EntriesAdapter : PagingDataAdapter<ListElementUi, EntriesAdapter.EntryViewHolder>(
    diffCallback = EntriesComparator,
    workerDispatcher = AppDispatchers.Default,
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder =
        EntryViewHolder(ItemLinkBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val item = getItem(position = position)
        holder.binding.text.text = "$position ${item?.let { it::class.java.simpleName }}, id=${item?.id}}"
    }

    data class EntryViewHolder(val binding: ItemLinkBinding) : RecyclerView.ViewHolder(binding.root)
}

internal object EntriesComparator : DiffUtil.ItemCallback<ListElementUi>() {

    override fun areItemsTheSame(oldItem: ListElementUi, newItem: ListElementUi) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ListElementUi, newItem: ListElementUi) =
        oldItem::class.java.simpleName == newItem::class.java.simpleName
}
