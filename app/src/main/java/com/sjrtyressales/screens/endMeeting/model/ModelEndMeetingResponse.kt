package com.sjrtyressales.screens.endMeeting.model

data class ModelEndMeetingResponse(var status:String, var message:String,var data: EndMeetingData?)

data class EndMeetingData(var userActive:String)
