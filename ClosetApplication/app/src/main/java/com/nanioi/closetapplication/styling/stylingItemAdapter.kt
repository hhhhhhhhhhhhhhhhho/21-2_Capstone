package com.nanioi.closetapplication.styling

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.closet.ItemModel
import com.nanioi.closetapplication.databinding.ViewholderItemStylingBinding
import com.nanioi.closetapplication.extensions.loadCenterCrop

class stylingItemAdapter(
    private val onItemClicked: (ItemModel) -> Unit
) : RecyclerView.Adapter<stylingItemAdapter.ViewHolder>() {

    private var itemList : List<ItemModel> = listOf()

    inner class ViewHolder(
        private val binding: ViewholderItemStylingBinding
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
                onItemClicked(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ViewholderItemStylingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: stylingItemAdapter.ViewHolder, position: Int) {
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