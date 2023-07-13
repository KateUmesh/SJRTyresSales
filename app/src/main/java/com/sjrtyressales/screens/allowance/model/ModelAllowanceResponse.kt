package com.sjrtyressales.screens.allowance.model

class ModelAllowanceResponse(var status:String, var message:String,val data: AllowanceData?)

data class AllowanceData(var userActive:String)