package com.nanioi.closetapplication.closet

import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.databinding.ViewholderItemBinding
import com.nanioi.closetapplication.extensions.loadCenterCrop
import java.lang.Boolean.getBoolean

class itemAdapter(
    private val checkPhotoListener: (ItemModel) -> Unit
) : RecyclerView.Adapter<itemAdapter.ViewHolder>() {

    private var itemList : List<ItemModel> = listOf()

    inner class ViewHolder(
        private val binding: ViewholderItemBinding
        ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(item: ItemModel) = with(binding) {
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
        }

        fun bindViews(data: ItemModel) = with(binding) {
            root.setOnClickListener {
                checkPhotoListener(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ViewholderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(itemList[position])
        holder.bindViews(itemList[position])
    }
    override fun getItemCount(): Int = itemList.size

    fun setPhotoList(itemList: List<ItemModel>) {
        Log.d("bb","setPhotoList")
        this.itemList = itemList
        notifyDataSetChanged()
    }
}