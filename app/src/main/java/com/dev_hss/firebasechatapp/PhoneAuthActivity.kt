package com.dev_hss.firebasechatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

object PhoneAuthActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mVerificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        mAuth = FirebaseAuth.getInstance()

//        btn_send_code.setOnClickListener {
//            val phoneNumber = et_phone_number.text.toString()
//            sendVerificationCode(phoneNumber)
//        }
//
//        btn_verify_code.setOnClickListener {
//            val verificationCode = et_verification_code.text.toString()
//            verifyCode(verificationCode)
//        }

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Handle verification failure
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                mVerificationId = verificationId
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//            phoneNumber,
//            60,
//            TimeUnit.SECONDS,
//            this,
//            mCallbacks
//        )
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(mVerificationId, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Phone authentication successful
                    val user = task.result?.user
                    // Proceed with Firebase private chat
                } else {
                    // Handle authentication failure
                }
            }
    }
}