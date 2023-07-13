package com.sjrtyressales.screens.dashboard.model

data class ModelDashboardResponse(var status:String,var message:String,var data: DashboardData?)

data class DashboardData(var todays_meetings:List<TodaysMeetingsList>,
                         var recent_meetings:List<RecentMeetingsList>,
                         var allowance:String,
                         var total_meetings:String,
                         var userActive:String
)


data class TodaysMeetingsList( var id_meeting :String,
                               var id_sales_employee:String,
                               var shop_name:String,
                               var distributor_name:String,
                               var distributor_mobile:String,
                               var meeting_start_time:String,
                               var meeting_start_latitude:String,
                               var meeting_start_longitude:String,
                               var meeting_end_time:String,
                               var meeting_conclusion:String,
                               var meeting_end_latitude:String,
                               var meeting_end_mongitude:String,
                               var photo:String)


data class RecentMeetingsList(
    var id_meeting: String,
    var shop_name: String,
    var distributor_name: String,
    var distributor_mobile: String,
    var meeting_conclusion: String,
    var meeting_start_date: String,
    var meeting_end_date: String,
    var meeting_start_time: String,
    var meeting_end_time: String,
    var meeting_start_latitude: String,
    var meeting_start_longitude: String,
    var meeting_end_latitude: String,
    var meeting_end_longitude: String
)