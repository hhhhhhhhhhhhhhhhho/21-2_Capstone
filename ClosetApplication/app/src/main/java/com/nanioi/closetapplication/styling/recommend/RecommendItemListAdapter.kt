package com.nanioi.closetapplication.styling.recommend

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.nanioi.closetapplication.databinding.RecommendedItemListBinding

class RecommendItemListAdapter : ListAdapter<RecommendItemModel, RecommendItemListAdapter.ViewHolder>(
    differ
) {

    inner class ViewHolder(
        private val binding: RecommendedItemListBinding
        ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecommendItemModel) {
            val productName = binding.recommendedProductName
            val productPrice = binding.recommendedProductPrice
            val productImage = binding.recommendedItem
            val productSeller = binding.recommendedProductSeller

            productName.text = item.ProductName
            productPrice.text = "가격 : " + item.ProductPrice + "원"
            productSeller.text = "판매자 : " + item.Seller

            Glide
                .with(productImage.context)
                .load(item.ProductImage)
                .transform(CenterCrop(), RoundedCorners(dpToPx(productImage.context,12)))
                .into(productImage)
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
                return oldItem.ProductCode == newItem.ProductCode
            }

            override fun areContentsTheSame(oldItem: RecommendItemModel, newItem: RecommendItemModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}