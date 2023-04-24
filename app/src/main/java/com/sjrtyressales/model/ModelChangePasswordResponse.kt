package com.sjrtyressales.model

data class ModelChangePasswordResponse(var status:String, var message:String, val data:ChangePasswordData?)

data class ChangePasswordData(var userActive:String)
