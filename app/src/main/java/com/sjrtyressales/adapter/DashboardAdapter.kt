package com.sjrtyressales.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sjrtyressales.databinding.ItemDashboardListBinding
import com.sjrtyressales.model.TodaysMeetingsList
import javax.inject.Inject

class DashboardAdapter @Inject constructor(): ListAdapter<TodaysMeetingsList,DashboardAdapter.AboutUsViewHolder>(AboutUsDiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutUsViewHolder {
        return AboutUsViewHolder(ItemDashboardListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: AboutUsViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    class AboutUsViewHolder(val binding: ItemDashboardListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item:TodaysMeetingsList){
            binding.listItem=item
        }

    }

    companion object {
        val AboutUsDiffCallback = object : DiffUtil.ItemCallback<TodaysMeetingsList>() {
            override fun areItemsTheSame(oldItem: TodaysMeetingsList, newItem: TodaysMeetingsList): Boolean {
                return oldItem.id_meeting == newItem.id_meeting
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: TodaysMeetingsList, newItem: TodaysMeetingsList): Boolean {
                return newItem == oldItem
            }
        }
    }
}
