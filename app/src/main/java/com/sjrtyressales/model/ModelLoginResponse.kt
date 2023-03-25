package com.sjrtyressales.model

data class ModelLoginResponse(var status:String, var message:String, var data:LoginData?)

data class LoginData(var token:String)
