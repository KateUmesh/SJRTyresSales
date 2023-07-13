package com.sjrtyressales.screens.inOut.model

data class ModelAttendanceResponse(var status:String, var message:String,var data: AttendanceData?)

data class AttendanceData(
    var currentTime: String,
    var inTime: String,
    var outTime: String,
    var inTimeButton: Int,
    var outTimeButton: Int,
    var userActive:String
)
