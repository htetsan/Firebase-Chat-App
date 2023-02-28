/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dev_hss.firebasechatapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import com.dev_hss.firebasechatapp.auth.PhoneAuth
import com.dev_hss.firebasechatapp.databinding.ActivitySignInBinding
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivitySignInBinding


    //private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mVerificationId: String


    var otpCode: String = ""

    // Firebase instance variables
    private lateinit var auth: FirebaseAuth

    private val signIn: ActivityResultLauncher<Intent> =
        registerForActivityResult(FirebaseAuthUIActivityResultContract(), this::onSignInResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Initialize FirebaseAuth
        auth = Firebase.auth
        auth.setLanguageCode("fr")

//        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                signInWithCredential(credential)
//            }
//
//            override fun onVerificationFailed(e: FirebaseException) {
//                // Handle verification failure
//            }
//
//            override fun onCodeSent(
//                verificationId: String,
//                token: PhoneAuthProvider.ForceResendingToken
//            ) {
//                mVerificationId = verificationId
//            }
//        }

        setUpUI()
        clickListener()
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Phone authentication successful
                    val user = task.result?.user
                    if (user != null) {
                        showMessage(this, user.phoneNumber.toString())
                    }
                    // Proceed with Firebase private chat
                } else {
                    // Handle authentication failure
                }
            }
    }


//    @TargetApi(Build.VERSION_CODES.O)
//    private fun disableAutofill() {
//        window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
//    }

    public override fun onStart() {
        super.onStart()
        // If there is no signed in user, launch FirebaseUI
        // Otherwise head to MainActivity
//        val customLayout = AuthMethodPickerLayout.Builder(R.layout.activity_phone_auth)
//            .setPhoneButtonId(R.id.phone_button)
//            .build()
//
//        if (Firebase.auth.currentUser == null) {
//            // Sign in with FirebaseUI, see docs for more details:
//            // https://firebase.google.com/docs/auth/android/firebaseui
//            val signInIntent = AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAuthMethodPickerLayout(customLayout)
//                .setLogo(R.mipmap.ic_launcher)
//                .setAvailableProviders(
//                    listOf(
////                        AuthUI.IdpConfig.EmailBuilder().build(),
////                        AuthUI.IdpConfig.GoogleBuilder().build(),
//                        AuthUI.IdpConfig.PhoneBuilder().build()
//                    )
//                )
//                .build()
//
//            signIn.launch(signInIntent)
//        } else {
//            //goToMainActivity()
//            goToCreateConservationActivity()
//        }
    }

    private fun signIn() {

    }

    private fun setUpUI() {

        mBinding.btnGetOTP.isEnabled = false
        mBinding.btnGetOTP.background =
            ContextCompat.getDrawable(this, R.drawable.login_btn_disable_bg)

        mBinding.btnVerify.isEnabled = false
        mBinding.btnVerify.background =
            ContextCompat.getDrawable(this, R.drawable.login_btn_disable_bg)

        mBinding.txtInputPhoneNumber.setEndIconOnClickListener {

            mBinding.edtPhoneNo.setText("")
            mBinding.btnGetOTP.isEnabled = false
            mBinding.btnGetOTP.background =
                ContextCompat.getDrawable(this, R.drawable.login_btn_disable_bg)

            mBinding.btnVerify.isEnabled = false
            mBinding.btnVerify.background =
                ContextCompat.getDrawable(this, R.drawable.login_btn_disable_bg)
        }

        mBinding.edtPhoneNo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isNotEmpty() && mBinding.otpView.isNotEmpty()) {

                    mBinding.btnGetOTP.isEnabled = true
                    mBinding.btnGetOTP.background = ContextCompat.getDrawable(
                        this@SignInActivity,
                        R.drawable.login_btn_bg
                    )

                    mBinding.btnVerify.isEnabled = true
                    mBinding.btnVerify.background = ContextCompat.getDrawable(
                        this@SignInActivity,
                        R.drawable.login_btn_bg
                    )


                }
            }
        })

    }

    private fun clickListener() {

        mBinding.btnGetOTP.setOnClickListener {
            // mBinding.mPresenter.onTapGetOTPCode(this, mBinding.edtPhoneNo.text.toString())
            sendVerificationCode(this, mBinding.edtPhoneNo.text.toString(),
                //PhoneAuth.sendVerificationCode(this, "+959984458969",
                onSuccess = {
                    showMessage(this, "Auth Success")
                }, onFailure = {
                    showMessage(this, it)
                    Log.d(TAG, "clickListener:onFailure:: $it")
                })
        }



        mBinding.btnVerify.setOnClickListener {

            if (!(otpCode == OTP_DEMO_ONE_DIGIT || otpCode == OTP_DEMO_TWO_DIGIT)) {
                val snackbar =
                    Snackbar.make(window.decorView, "The OTP Code is wrong.", Snackbar.LENGTH_LONG)
                        .setAction("Try again!") {
                            /*  val snackbar =
                                  Snackbar.make(window.decorView, "The OTP Code is wrong.", Snackbar.LENGTH_LONG)
                              snackbar.show()*/
                            mBinding.otpView.clearFocus()
                            mBinding.otpView.isFocusable = true
                        }
                // call show() method to
                // display the snackbar
                snackbar.show()
            } else {
                PhoneAuth.verifyOTP(this,
                    mBinding.edtPhoneNo.text.toString(),
                    otpCode,
                    onSuccess = {
                        startActivity(
                            CreateConservationActivity.newIntent(
                                this, mBinding.edtPhoneNo.text.toString(), it
                            )
                        )
                    },
                    onFailure = {
                        showMessage(this, it)
                    })
                // startActivity(SignUpActivity.newIntent(this, edtPhoneNo.text.toString()))
            }

        }
    }

    fun sendVerificationCode(
        context: Activity, phoneNumber: String, onSuccess: () -> Unit, onFailure: (String) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(context)                 // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential).addOnCompleteListener(context) { task ->
                        if (task.isSuccessful) {
                            // Phone authentication successful
                            val user = task.result?.user
                            Log.d(
                                "TAG",
                                "signInWithPhoneAuthCredential:success:: ${task.result.additionalUserInfo.toString()}"
                            )
                            onSuccess()
                            // Proceed with Firebase private chat
                        } else {
                            // Handle authentication failure
                            Log.d(
                                "TAG",
                                "signInWithPhoneAuthCredential:fail:: ${task.exception.toString()}"
                            )
                            onFailure(task.exception.toString())

                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Handle verification failure
                    Log.d(
                        "TAG", "onVerificationFailed:: $e"
                    )
                }

                override fun onCodeSent(
                    verificationId: String, token: PhoneAuthProvider.ForceResendingToken
                ) {

//                    val verificationCode = "1234"
//                    val credential =
//                        PhoneAuthProvider.getCredential(verificationId, verificationCode)
//
//                    // Sign in with the PhoneAuthCredential
//
//                    mFirebaseAuth.signInWithCredential(credential)
//                        .addOnCompleteListener { task: Task<AuthResult?> ->
//                            if (task.isSuccessful) {
//                                // Phone number verification succeeded
//                                onSuccess()
//                                //showMessage(context, "task is successful")
//                            } else {
//                                // Phone number verification failed
//                                // ...
//                            }
//                        }
                    val intent = Intent(this@SignInActivity, CreateConservationActivity::class.java)
                    intent.putExtra("OTP", verificationId)
                    intent.putExtra("resendToken", token)
                    intent.putExtra("phoneNumber", phoneNumber)
                    mVerificationId = verificationId
                    //onFailure("$verificationId ,  $token")

                }
            }) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "Sign in successful!")
            //goToMainActivity()
            //goToCreateConservationActivity()

            if (result.idpResponse is PhoneAuthCredential) {
                val credential = result.idpResponse as PhoneAuthCredential
                Log.d(TAG, credential.toString())
                //startActivity(PhoneAuthFromCodeSnipActivity.newIntent(this))
            }
        } else {
            Toast.makeText(
                this,
                "There was an error signing in",
                Toast.LENGTH_LONG
            ).show()

            val response = result.idpResponse
            if (response == null) {
                Log.w(TAG, "Sign in canceled")
            } else {
                Log.w(TAG, "Sign in error", response.error)
            }
        }
    }

    private fun goToMainActivity() {
        Toast.makeText(this, "Sign in Successful! goToMainActivity", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToCreateConservationActivity() {
        Toast.makeText(
            this,
            "Sign in Successful! goToCreateConservationActivity",
            Toast.LENGTH_LONG
        ).show()
        startActivity(CreateConservationActivity.newIntent(this, "", ""))
        finish()
    }

    companion object {
        private const val TAG = "SignInActivity"
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, SignInActivity::class.java)
            return intent
        }
    }

//    override fun onDestroy() {
//        auth.currentUser = null
//        super.onDestroy()
//    }
}
