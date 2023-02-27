package com.dev_hss.firebasechatapp.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

object PhoneAuth {

    private val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
//    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//            // This callback will be invoked in two situations:
//            // 1 - Instant verification. In some cases the phone number can be instantly
//            //     verified without needing to send or enter a verification code.
//            // 2 - Auto-retrieval. On some devices Google Play services can automatically
//            //     detect the incoming verification SMS and perform verification without
//            //     user action.
//            signInWithPhoneAuthCredential(credential)
//        }
//
//        override fun onVerificationFailed(e: FirebaseException) {
//            // This callback is invoked in an invalid request for verification is made,
//            // for instance if the the phone number format is not valid.
//
//            if (e is FirebaseAuthInvalidCredentialsException) {
//                // Invalid request
//                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
//            } else if (e is FirebaseTooManyRequestsException) {
//                // The SMS quota for the project has been exceeded
//                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
//            }
//          //  mProgressBar.visibility = View.VISIBLE
//            // Show a message and update the UI
//        }
//
//        override fun onCodeSent(
//            verificationId: String,
//            token: PhoneAuthProvider.ForceResendingToken
//        ) {
//            // The SMS verification code has been sent to the provided phone number, we
//            // now need to ask the user to enter the code and then construct a credential
//            // by combining the code with a verification ID.
//            // Save verification ID and resending token so we can use them later
//          //  val intent = Intent(this@PhoneActivity , OTPActivity::class.java)
//         //   intent.putExtra("OTP" , verificationId)
//         //   intent.putExtra("resendToken" , token)
//          //  intent.putExtra("phoneNumber" , number)
//          //  startActivity(intent)
//         //   mProgressBar.visibility = View.INVISIBLE
//        }
//    }


    fun getOTP(
        context: Activity, phoneNumber: String, onSuccess: () -> Unit, onFailure: (String) -> Unit
    ) {

        val options = PhoneAuthOptions.newBuilder(mFirebaseAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(context)                 // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    //signInWithPhoneAuthCredential(credential)


//                    mFirebaseAuth.signInWithCredential(credential)
//                        .addOnCompleteListener(context) { task ->
//                            if (task.isSuccessful) {
//                                // Sign in success, update UI with the signed-in user's information
//                                Log.d("TAG", "onVerificationCompleted: Authenticate Successfully")
//                                //  sendToMain()
//                                onSuccess()
//                            } else {
//                                // Sign in failed, display a message and update the UI
//                                Log.d(
//                                    "TAG",
//                                    "signInWithPhoneAuthCredential: ${task.exception.toString()}"
//                                )
//                                if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                                    // The verification code entered was invalid
//                                    onFailure(task.exception?.message.toString())
//                                }
//                                // Update UI
//                            }
//                            //  mProgressBar.visibility = View.INVISIBLE
//                        }
                    onSuccess()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.

                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        Log.d("TAG", "onVerificationFailed: $e")
                        onFailure(e.toString())
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        Log.d("TAG", "onVerificationFailed: $e")
                        onFailure(e.toString())
                    }
                    //  mProgressBar.visibility = View.VISIBLE
                    // Show a message and update the UI
                }

                override fun onCodeSent(
                    verificationId: String, token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    // Save verification ID and resending token so we can use them later
                    //  val intent = Intent(this@PhoneActivity , OTPActivity::class.java)
                    //   intent.putExtra("OTP" , verificationId)
                    //   intent.putExtra("resendToken" , token)
                    //  intent.putExtra("phoneNumber" , number)
                    //  startActivity(intent)
                    //   mProgressBar.visibility = View.INVISIBLE
                    onFailure("$verificationId ,  $token ")
                }
            }) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    fun verifyOTP(
        context: Activity,
        phoneNumber: String,
        otpCode: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseAuth.createUserWithEmailAndPassword("${phoneNumber}User@gmail.com", "111111")
            .addOnCompleteListener {
                if (it.isSuccessful && it.isComplete) {
                    mFirebaseAuth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder().setDisplayName("${phoneNumber}User")
                            .build()
                    )
                    var userId: String = mFirebaseAuth.currentUser?.uid ?: ""
                    onSuccess(userId)

                } else {
                    onFailure(it.exception?.message ?: "Please check internet connection")
                }
            }
    }


    /* override fun verifyOTP(
         context: Activity,
         phoneNumber: String,
         otpCode: String,
         onSuccess: () -> Unit,
         onFailure: (String) -> Unit
     ) {
         val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
             "1111", otpCode
         )

         mFirebaseAuth.signInWithCredential(credential)
             .addOnCompleteListener(context) { task ->
                 if (task.isSuccessful) {
                     // Sign in success, update UI with the signed-in user's information

                    // Toast.makeText(this, "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                    // sendToMain()
                     onSuccess()
                 } else {
                     // Sign in failed, display a message and update the UI
                     Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                     if (task.exception is FirebaseAuthInvalidCredentialsException) {
                         // The verification code entered was invalid
                         onFailure(task.exception?.message.toString())

                     }
                     // Update UI
                 }
                // progressBar.visibility = View.VISIBLE
             }
     }*/

//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                  //  Toast.makeText(this , "Authenticate Successfully" , Toast.LENGTH_SHORT).show()
//                  //  sendToMain()
//
//                } else {
//                    // Sign in failed, display a message and update the UI
//                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
//                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        // The verification code entered was invalid
//                    }
//                    // Update UI
//                }
//              //  mProgressBar.visibility = View.INVISIBLE
//            }
//    }

    fun getCurrentUser(): FirebaseUser? {

        Log.d("PhoneAuth", "check current user = ${mFirebaseAuth.currentUser?.uid}")

        return mFirebaseAuth.currentUser
    }
}