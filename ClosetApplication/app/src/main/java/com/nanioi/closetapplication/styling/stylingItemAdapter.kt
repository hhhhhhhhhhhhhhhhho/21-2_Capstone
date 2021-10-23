package com.nanioi.closetapplication.styling

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.closet.ItemModel
import com.nanioi.closetapplication.databinding.ViewholderItemStylingBinding
import com.nanioi.closetapplication.extensions.loadCenterCrop

class stylingItemAdapter(
    val onItemClicked: (ItemModel) -> Unit
) : ListAdapter<ItemModel, stylingItemAdapter.ViewHolder>(diffUtil) {


    inner class ViewHolder(private val binding: ViewholderItemStylingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemModel)= with(binding) {

            photoImageView.loadCenterCrop(item.imageUrl, 8f)
            checkButton.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    if (item.isSelected)
                        R.drawable.ic_check_circle
                    else
                        R.drawable.ic_before_check
                )
            )
            binding.root.setOnClickListener {
                onItemClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ViewholderItemStylingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ItemModel>() {
            override fun areItemsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
                return oldItem.itemId == newItem.itemId
            }

            override fun areContentsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}