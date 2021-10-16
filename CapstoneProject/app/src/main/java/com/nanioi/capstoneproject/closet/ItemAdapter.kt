package com.nanioi.capstoneproject.closet

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nanioi.capstoneproject.R
import com.nanioi.capstoneproject.databinding.ViewholderGalleryPhotoItemBinding
import com.nanioi.capstoneproject.extensions.loadCenterCrop
import com.nanioi.capstoneproject.gallery.GalleryPhoto

class ItemAdapter (
    private val checkPhotoListener: (ItemModel) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private var itemList: List<ItemModel> = listOf()

    inner class ViewHolder(private val binding: ViewholderGalleryPhotoItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ItemModel) = with(binding) {
            photoImageView.loadCenterCrop(data.imageUrl.toString(), 8f)
            checkButton.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    if (data.isSelected)
                        R.drawable.ic_check_circle
                    else
                        R.drawable.ic_before_check
                )
            )
        }
        fun bindViews(data: ItemModel) = with(binding) {
            root.setOnClickListener {
                checkPhotoListener(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ViewholderGalleryPhotoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
        holder.bindViews(itemList[position])
    }
    override fun getItemCount(): Int = itemList.size

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ItemModel>() {
            override fun areItemsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
                return oldItem == newItem
            }


        }
    }
    fun setPhotoList(itemList: List<ItemModel>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

}