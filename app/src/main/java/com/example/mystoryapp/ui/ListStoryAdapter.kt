package com.example.mystoryapp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ItemListStoryBinding
import com.example.mystoryapp.retrofit.ListStoryPagingResponse
import java.util.*

class ListStoryAdapter(val context: Context) :
    PagingDataAdapter<ListStoryPagingResponse, ListStoryAdapter.ListViewHolder>(DIFF_CALLBACK) {
    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    class ListViewHolder(private var binding: ItemListStoryBinding, val context: Context) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: ListStoryPagingResponse) {
            binding.tvItemName.text = data.name
            Glide.with(itemView.context)
                .load(data.photoUrl)
                .into(binding.image)
            if (data.lat != null && data.lon != null) {
                val geocoder = Geocoder(itemView.context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(data.lat, data.lon, 1)
                val address = addresses!![0].getAddressLine(0)

                binding.tvLocation.text = address
            } else {
                binding.tvLocation.text = context.getString(R.string.addresss)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding =
            ItemListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)

            holder.itemView.setOnClickListener {
                onItemClickCallback.onItemClicked(data)
            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ListStoryPagingResponse)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryPagingResponse>() {
            override fun areItemsTheSame(
                oldItem: ListStoryPagingResponse,
                newItem: ListStoryPagingResponse
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryPagingResponse,
                newItem: ListStoryPagingResponse
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}