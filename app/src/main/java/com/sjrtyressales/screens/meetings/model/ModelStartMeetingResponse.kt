package com.sjrtyressales.screens.meetings.model

data class ModelStartMeetingResponse(var status:String, var message:String,var data: StartMeetingData?)

class StartMeetingData(var userActive:String)
