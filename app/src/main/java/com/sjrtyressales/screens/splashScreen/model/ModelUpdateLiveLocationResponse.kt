package com.sjrtyressales.screens.splashScreen.model

data class ModelUpdateLiveLocationResponse(var status:String, var message:String, val data: UpdateLiveLocationData?)

data class UpdateLiveLocationData(var userActive:String)
