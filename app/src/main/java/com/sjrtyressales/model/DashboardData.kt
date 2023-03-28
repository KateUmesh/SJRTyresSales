package com.sjrtyressales.model

data class DashboardData(var todays_meetings:List<TodaysMeetingsList>,
                         var recent_meetings:List<RecentMeetingsList>,
                         var allowance:String,
                         var total_meetings:String)
