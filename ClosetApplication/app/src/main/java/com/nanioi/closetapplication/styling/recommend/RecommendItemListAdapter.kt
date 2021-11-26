package com.nanioi.closetapplication.styling.recommend

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
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

class RecommendItemListAdapter(val itemClicked:(RecommendItemModel)->Unit) : ListAdapter<RecommendItemModel, RecommendItemListAdapter.ViewHolder>(differ) {

    inner class ViewHolder(
        private val binding: RecommendedItemListBinding
        ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: RecommendItemModel) {
            val productName = binding.recommendedProductName
            val productPrice = binding.recommendedProductPrice
            val productImage = binding.recommendedItem
            val productSeller = binding.recommendedProductSeller
            val productSalePrice = binding.recommendedProductSalePrice


            productName.text = item.ProductName
            productPrice.text = "${item.ProductPrice}"
            productPrice.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                //setTypeface(null, Typeface.ITALIC)
            }
            productSalePrice.text = "${item.SalePrice}"
            productSeller.text = "${item.Seller}"
            Glide
                .with(productImage.context)
                .load(item.ProductImage)
                .transform(CenterCrop(), RoundedCorners(dpToPx(productImage.context,8)))
                .into(productImage)

            binding.root.setOnClickListener {
                itemClicked(item)
            }
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
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: RecommendItemModel, newItem: RecommendItemModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}