package com.nanioi.closetapplication.home

import android.content.Context
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nanioi.closetapplication.R
import com.nanioi.closetapplication.databinding.HomeImageSliderBinding
import com.nanioi.closetapplication.databinding.ViewholderItemBinding


class ImageSliderAdapter(private val context: Context
) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>() {

    var sliderImage = arrayOf(
        R.drawable.home_image1,
        R.drawable.home_image2,
        R.drawable.home_image3,
        R.drawable.home_image4
    )

    inner class ViewHolder(
        private val binding: HomeImageSliderBinding
    ):RecyclerView.ViewHolder(binding.root){
        val image = binding.imageSlider
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeImageSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(sliderImage[position]).into(holder.image)
    }


    override fun getItemCount(): Int = sliderImage.size


}