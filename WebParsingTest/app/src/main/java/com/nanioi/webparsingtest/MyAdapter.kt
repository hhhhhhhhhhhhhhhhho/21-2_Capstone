package com.nanioi.webparsingtest

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nanioi.webparsingtest.databinding.ItemLayoutBinding

class MyAdapter(private var itemList : List<Item>) : RecyclerView.Adapter<MyAdapter.ViewHolder>(){
    //private var itemList : List<Item> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.ViewHolder {
        return ViewHolder(ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    class ViewHolder( private val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : Item ){
            binding.tvTitle.text = item.title
            Glide
                .with(binding.root)
                .load(item.thumb)
                .centerCrop()
                .placeholder(android.R.drawable.stat_sys_upload)
                .into(binding.ivThumb)
        }
    }
}