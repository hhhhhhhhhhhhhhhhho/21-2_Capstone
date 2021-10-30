package com.nanioi.capstoneproject.closet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nanioi.capstoneproject.databinding.ViewholderGalleryPhotoItemBinding

class iiiAdapter() : ListAdapter<ItemModel, iiiAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ViewholderGalleryPhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(itemModel: ItemModel) {

            Glide.with(binding.photoImageView)
                    .load(itemModel.imageUrl)
                    .into(binding.photoImageView)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ViewholderGalleryPhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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