package com.sjrtyressales.utils

class Constant {

    companion object{
        const val appName = "SJR Tyres Sales"
        const val Bearer: String="Bearer "
        const val no_internet_connection: String="No Internet Connection"
        const val slow_internet_connection_detected: String="Slow internet connection detected"
        const val something_went_wrong: String = "Something went wrong"
        const val token: String = "token"
        const val meetingId: String = "meetingId"

        const val login="sjr_tyres_sales_app/app/user/login"
        const val dashboard="sjr_tyres_sales_app/app/user/dashboard"
        const val meetingHistory="sjr_tyres_sales_app/app/user/meetingHistory"
        const val startMeeting="sjr_tyres_sales_app/app/user/startMeeting"
        const val attendance="sjr_tyres_sales_app/app/user/attendance"
        const val submitInTime="sjr_tyres_sales_app/app/user/submitInTime/{latitude}/{longitude}"
        const val submitOutTime="sjr_tyres_sales_app/app/user/submitOutTime/{latitude}/{longitude}"
        const val updateProfilePhoto="sjr_tyres_sales_app/app/user/updateProfilePhoto"
        const val updateLiveLocation="sjr_tyres_sales_app/app/user/updateLiveLocation"
        const val allowance="sjr_tyres_sales_app/app/user/allowance"
        const val checkMeetingNotEnd="sjr_tyres_sales_app/app/user/checkMeetingNotEnd"
        const val endMeeting="sjr_tyres_sales_app/app/user/endMeeting"
        const val viewMeetingDetails="sjr_tyres_sales_app/app/user/viewMeetingDetails/{meeting_id}"
    }
}