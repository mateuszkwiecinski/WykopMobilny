package io.github.wykopmobilny.utils.bindings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.android.databinding.ItemOptionBinding
import io.github.wykopmobilny.ui.base.android.databinding.OptionPickerBinding
import io.github.wykopmobilny.ui.base.components.OptionPickerUi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy

suspend fun Flow<OptionPickerUi?>.collectOptionPicker(
    context: Context,
) {
    var dialog: BottomSheetDialog? = null
    distinctUntilChangedBy { it?.reasons?.map(OptionPickerUi.Option::label) }.collect { picker ->
        dialog?.dismiss()
        if (picker != null) {
            dialog = OptionPickerBottomSheet(picker, context).apply {
                setOnDismissListener { picker.dismissAction() }
                show()
            }
        }
    }
}

private class OptionPickerBottomSheet(
    private val picker: OptionPickerUi,
    context: Context,
) : BottomSheetDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = OptionPickerBinding.inflate(layoutInflater)
        binding.list.adapter = PickerListAdapter().apply {
            submitList(
                picker.reasons.map { reason ->
                    reason.copy(
                        clickAction = {
                            picker.dismissAction()
                            reason.clickAction()
                        },
                    )
                },
            )
        }
        binding.txtTitle.text = picker.title
        setContentView(binding.root)
    }
}

private class PickerListAdapter : ListAdapter<OptionPickerUi.Option, PickerListAdapter.DefaultViewHolder>(
    AsyncDifferConfig.Builder(Diff)
        .setBackgroundThreadExecutor(AppDispatchers.Default.asExecutor())
        .build(),
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DefaultViewHolder(binding = ItemOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        val item = getItem(position)

        val icon = item.icon
        if (icon == null) {
            holder.binding.imgIcon.isVisible = false
        } else {
            holder.binding.imgIcon.isVisible = true
            holder.binding.imgIcon.setImageResource(icon.drawableRes)
        }
        holder.binding.txtTitle.text = item.label
        holder.binding.root.setOnClick(item.clickAction)
    }

    class DefaultViewHolder(val binding: ItemOptionBinding) : RecyclerView.ViewHolder(binding.root)

    private object Diff : DiffUtil.ItemCallback<OptionPickerUi.Option>() {
        override fun areItemsTheSame(oldItem: OptionPickerUi.Option, newItem: OptionPickerUi.Option) =
            oldItem.label == newItem.label

        override fun areContentsTheSame(oldItem: OptionPickerUi.Option, newItem: OptionPickerUi.Option) =
            oldItem.label == newItem.label && oldItem.icon == newItem.icon

    }
}
