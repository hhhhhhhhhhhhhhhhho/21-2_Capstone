package com.nanioi.closetapplication.styling

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.nanioi.closetapplication.databinding.RecommendedItemListBinding
import com.nanioi.closetapplication.databinding.ViewholderItemBinding

class RecommendItemListAdapter : ListAdapter<RecommendItemModel, RecommendItemListAdapter.ViewHolder>(differ) {

    inner class ViewHolder(
        private val binding: RecommendedItemListBinding
        ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecommendItemModel) {
            val titleTextView = binding.recommendedTitle
            val descriptionTextView = binding.recommendedDescription
            val thumbnailImageView = binding.recommendedItem

            titleTextView.text = item.title
            descriptionTextView.text = item.description

            Glide
                .with(thumbnailImageView.context)
                .load(item.imgUrl)
                .transform(CenterCrop(), RoundedCorners(dpToPx(thumbnailImageView.context,12)))
                .into(thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RecommendedItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    private fun dpToPx(context: Context, dp:Int):Int{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp.toFloat(),context.resources.displayMetrics).toInt()
    }
    companion object {
        val differ = object : DiffUtil.ItemCallback<RecommendItemModel>() {
            override fun areItemsTheSame(oldItem: RecommendItemModel, newItem: RecommendItemModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: RecommendItemModel, newItem: RecommendItemModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}