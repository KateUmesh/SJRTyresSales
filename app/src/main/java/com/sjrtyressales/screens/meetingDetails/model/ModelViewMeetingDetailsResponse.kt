package com.sjrtyressales.screens.meetingDetails.model

data class ModelViewMeetingDetailsResponse(var status:String,var message:String,var data: MeetingDetailsData?)

data class MeetingDetailsData(var meeting_details: MeetingDetails, var userActive:String)

data class MeetingDetails(
    var id_meeting:String,
    var shop_name:String,
    var distributor_name:String,
    var distributor_mobile:String,
    var meeting_conclusion:String,
    var meeting_start_date:String,
    var meeting_end_date:String,
    var meeting_start_time:String,
    var meeting_end_time:String,
    var meeting_start_latitude:String,
    var meeting_start_longitude:String,
    var meeting_end_latitude:String,
    var meeting_end_longitude:String,
    var photo:String)