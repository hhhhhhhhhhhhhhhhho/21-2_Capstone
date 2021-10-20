package com.nanioi.closetapplication.closet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.databinding.ViewholderGalleryPhotoItemBinding
import com.nanioi.closetapplication.extensions.loadCenterCrop

class itemAdapter(
    val onItemClicked: (ItemModel) -> Unit
) : ListAdapter<ItemModel, itemAdapter.ViewHolder>(diffUtil) {

    //private var itemList: List<ItemModel> = listOf()

    inner class ViewHolder(private val binding: ViewholderGalleryPhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemModel)= with(binding) {
//            Glide.with(photoImageView)
//                .load(item.imageUrl)
//                .into(photoImageView)

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
//        fun bindViews(item: ItemModel) = with(binding) {
//            root.setOnClickListener {
//                onItemClicked(item)
//            }
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ViewholderGalleryPhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

//    fun setPhotoList(itemList: List<ItemModel>) {
//        this.itemList = itemList
//        notifyDataSetChanged()
//    }
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