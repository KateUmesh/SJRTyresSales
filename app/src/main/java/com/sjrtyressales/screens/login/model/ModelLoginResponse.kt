package com.sjrtyressales.screens.login.model

data class ModelLoginResponse(var status:String, var message:String, var data: LoginData?)

data class LoginData(var token:String,var inTimeDialog:String,var inTimeDialogMessage:String)
