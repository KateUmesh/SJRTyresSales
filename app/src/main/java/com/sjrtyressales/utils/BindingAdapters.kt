package com.sjrtyressales.utils

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sjrtyressales.R
import com.sjrtyressales.adapter.DashboardAdapter
import com.sjrtyressales.adapter.HistoryAdapter
import com.sjrtyressales.screens.history.model.MeetingHistoryList
import com.sjrtyressales.screens.dashboard.model.RecentMeetingsList

@BindingAdapter("imageFromUrl")
fun ImageView.imageFromUrl(url: String?) {
    if (!url.isNullOrEmpty()) {
        Glide.with(this.context)
            .load(url)
            .placeholder(R.color.grey_10)
            .into(this)
    }
}

@BindingAdapter("android:visibility")
fun View.bindVisibility(visible: Boolean?) {
    isVisible = visible == true
}

@BindingAdapter("dashboardList")
fun RecyclerView.bindRecyclerView1(list: List<RecentMeetingsList>?) {
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