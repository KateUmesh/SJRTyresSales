package com.sjrtyressales.model

data class MyProfileData(var sales_employee:SalesEmployee,var userActive:String)

data class SalesEmployee(
    var id_sales_employee:String,
var name:String,
var mobile:String,
var email:String,
var profile_image:String)
