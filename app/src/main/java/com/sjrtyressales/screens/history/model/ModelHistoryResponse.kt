package com.sjrtyressales.screens.history.model

data class ModelHistoryResponse(var status:String, var message:String, var data: HistoryData?)

data class HistoryData(var meeting_history:List<MeetingHistoryList>, var userActive:String)

data class MeetingHistoryList(var id_meeting :String,
                              var id_sales_employee:String,
                              var shop_name:String,
                              var distributor_name:String,
                              var distributor_mobile:String,
                              var meeting_start_date:String,
                              var meeting_start_time:String,
                              var meeting_end_date:String,
                              var meeting_start_latitude:String,
                              var meeting_start_longitude:String,
                              var meeting_end_time:String,
                              var meeting_conclusion:String,
                              var meeting_end_latitude:String,
                              var meeting_end_mongitude:String,
                              var photo:String)