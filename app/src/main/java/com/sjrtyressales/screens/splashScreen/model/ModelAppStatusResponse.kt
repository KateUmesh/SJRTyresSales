package com.sjrtyressales.screens.splashScreen.model

data class ModelAppStatusResponse(var status:String,var message:String,var data: AppStatusData?)

data class AppStatusData( var appStatus:String,
                          var  appStatusMessage:String,
                          var inTimeDialog:String,
                          var inTimeDialogMessage:String)