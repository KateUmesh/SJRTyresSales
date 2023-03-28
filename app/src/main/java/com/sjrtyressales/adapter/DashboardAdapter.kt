package com.sjrtyressales.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sjrtyressales.databinding.ItemDashboardListBinding
import com.sjrtyressales.model.RecentMeetingsList
import com.sjrtyressales.model.TodaysMeetingsList
import com.sjrtyressales.utils.callMeetingDetailsActivity
import javax.inject.Inject

class DashboardAdapter @Inject constructor(): ListAdapter<RecentMeetingsList,DashboardAdapter.AboutUsViewHolder>(AboutUsDiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutUsViewHolder {
        return AboutUsViewHolder(ItemDashboardListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: AboutUsViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    class AboutUsViewHolder(val binding: ItemDashboardListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item:RecentMeetingsList){
            binding.listItem=item

            binding.lytParent.setOnClickListener {
                callMeetingDetailsActivity(it.context,item.id_meeting)
            }
        }

    }

    companion object {
        val AboutUsDiffCallback = object : DiffUtil.ItemCallback<RecentMeetingsList>() {
            override fun areItemsTheSame(oldItem: RecentMeetingsList, newItem: RecentMeetingsList): Boolean {
                return oldItem.id_meeting == newItem.id_meeting
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: RecentMeetingsList, newItem: RecentMeetingsList): Boolean {
                return newItem == oldItem
            }
        }
    }
}
