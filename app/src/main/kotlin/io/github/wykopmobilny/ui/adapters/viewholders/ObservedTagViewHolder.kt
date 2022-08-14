package io.github.wykopmobilny.ui.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import io.github.wykopmobilny.api.responses.ObservedTagResponse
import io.github.wykopmobilny.databinding.ObservedTagListItemBinding
import io.github.wykopmobilny.ui.modules.NewNavigator

class ObservedTagViewHolder(
    private val binding: ObservedTagListItemBinding,
    private val navigator: NewNavigator,
) : RecyclerView.ViewHolder(binding.root) {

    fun bindView(tag: ObservedTagResponse) {
        binding.blockedTextView.text = tag.tag
        binding.root.setOnClickListener { navigator.openTagActivity(tag.tag.removePrefix("#")) }
    }
}
