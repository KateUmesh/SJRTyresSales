package com.sjrtyressales.screens.inOut.model

data class ModelSubmitInOutTimeResponse(var status:String, var message:String,var data: SubmitInOutTimeData?)

data class SubmitInOutTimeData(var userActive:String)
