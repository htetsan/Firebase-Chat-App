package com.dev_hss.firebasechatapp

import android.content.Context
import android.widget.Toast


//    fun createUserId(phoneNumber: String): String {
//        return phoneNumber.replace("+", "").replace("-", "")
//    }

val OTP_DEMO_ONE_DIGIT = "1111"
val OTP_DEMO_TWO_DIGIT = "1234"

fun showMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}
