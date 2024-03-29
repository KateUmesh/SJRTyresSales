package com.sjrtyressales.screens.meetings.model

data class ModelCheckMeetingNotEndResponse(var status:String,var message:String,var data: MeetingData?)

data class MeetingData(var meeting: Meeting, var userActive:String)

data class Meeting(
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
    var meeting_end_longitude:String)