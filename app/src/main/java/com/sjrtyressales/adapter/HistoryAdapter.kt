package com.sjrtyressales.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sjrtyressales.databinding.ItemHistoryListBinding
import com.sjrtyressales.model.MeetingHistoryList
import com.sjrtyressales.utils.callMeetingDetailsActivity
import javax.inject.Inject

class HistoryAdapter @Inject constructor(): ListAdapter<MeetingHistoryList,HistoryAdapter.HistoryViewHolder>(AboutUsDiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(ItemHistoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    class HistoryViewHolder(val binding: ItemHistoryListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item:MeetingHistoryList){
            binding.listItem=item

            binding.lytParent.setOnClickListener {
                callMeetingDetailsActivity(it.context,item.id_meeting)
            }
        }

    }

    companion object {
        val AboutUsDiffCallback = object : DiffUtil.ItemCallback<MeetingHistoryList>() {
            override fun areItemsTheSame(oldItem: MeetingHistoryList, newItem: MeetingHistoryList): Boolean {
                return oldItem.id_meeting == newItem.id_meeting
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: MeetingHistoryList, newItem: MeetingHistoryList): Boolean {
                return newItem == oldItem
            }
        }
    }
}
