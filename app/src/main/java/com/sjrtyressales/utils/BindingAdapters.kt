package com.sjrtyressales.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sjrtyressales.R
import com.sjrtyressales.adapter.DashboardAdapter
import com.sjrtyressales.adapter.HistoryAdapter
import com.sjrtyressales.model.MeetingHistoryList
import com.sjrtyressales.model.TodaysMeetingsList

@BindingAdapter("imageFromUrl")
fun ImageView.imageFromUrl(url: String?) {
    if (!url.isNullOrEmpty()) {
        Glide.with(this.context)
            .load(url)
            .placeholder(R.color.grey_10)
            .into(this)
    }
}

@BindingAdapter("dashboardList")
fun RecyclerView.bindRecyclerView1(list: List<TodaysMeetingsList>?) {
    this.setHasFixedSize(true)
    val adapter = DashboardAdapter()
    this.adapter = adapter
    adapter.submitList(list)
}

@BindingAdapter("historyList")
fun RecyclerView.bindRecyclerView2(list: List<MeetingHistoryList>?) {
    this.setHasFixedSize(true)
    val adapter = HistoryAdapter()
    this.adapter = adapter
    adapter.submitList(list)
}