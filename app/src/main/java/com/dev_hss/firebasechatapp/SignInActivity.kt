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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import com.dev_hss.firebasechatapp.auth.PhoneAuth
import com.dev_hss.firebasechatapp.databinding.ActivitySignInBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import `in`.aabhasjindal.otptextview.OTPListener
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {
    private lateinit var storedVerificationId: String
    private lateinit var mToken: ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var mBinding: ActivitySignInBinding


    //private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mVerificationId: String


    var otpCode: String = ""

    // Firebase instance variables
    private lateinit var auth: FirebaseAuth

//    private val signIn: ActivityResultLauncher<Intent> =
//        registerForActivityResult(FirebaseAuthUIActivityResultContract(), this::onSignInResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Initialize FirebaseAuth
        auth = Firebase.auth
        auth.setLanguageCode("fr")
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String, token: ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                mToken = token
            }
        }
        initialize()
        setUpOTP()
        setUpUI()
        clickListener()
    }

    private fun initialize() {
        sendVerificationCode(this, mBinding.edtPhoneNo.text.toString())
    }

    private fun setUpOTP() {
        mBinding.otpView.requestFocusOTP()
        mBinding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                // Toast.makeText(this@OTPVerifyActivity, "The OTP is $otp", Toast.LENGTH_SHORT).show()
                otpCode = otp
                if (otp.isEmpty()) {
                    mBinding.btnVerify.isEnabled = true
                    mBinding.btnVerify.background = ContextCompat.getDrawable(
                        this@SignInActivity,
                        R.drawable.login_btn_bg
                    )
                }
//                if(otp == OTP_DEMO_ONE_DIGIT || otp == OTP_DEMO_TWO_DIGIT)
//                {
//                    btnVerify.isEnabled = true
//                    btnVerify.background = ContextCompat.getDrawable(
//                        this@OTPVerifyActivity,
//                        R.drawable.login_btn_bg
//                    )
//                }else{
//                    otpView?.showError()
//                    Toast.makeText(this@OTPVerifyActivity, "The OTP Code is wrong. ", Toast.LENGTH_SHORT).show()
//                }

            }
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnSuccessListener {
            val phone = auth.currentUser?.phoneNumber
            showMessage(this, "Logged In as $phone")
//            }(this) { task ->
//                if (task.isSuccessful) {
//                    // Phone authentication successful
//                    val user = task.result?.user
//                    if (user != null) {
//                        showMessage(this, user.phoneNumber.toString())
//                    }
//                    // Proceed with Firebase private chat
//                } else {
//                    // Handle authentication failure
//                }
        }.addOnFailureListener {
            showMessage(this, "Logged In as $it")

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

//        mBinding.btnGetOTP.setOnClickListener {
//            // mBinding.mPresenter.onTapGetOTPCode(this, mBinding.edtPhoneNo.text.toString())
//            sendVerificationCode(this, mBinding.edtPhoneNo.text.toString())
//        }

        mBinding.btnResendCode.setOnClickListener {
            Log.d(TAG, "phoneNo: ${mBinding.edtPhoneNo.text.toString()}")
            resendVerificationCode(mBinding.edtPhoneNo.text.toString(), mToken)
        }



        mBinding.btnVerify.setOnClickListener {

//            if (!(otpCode == OTP_DEMO_ONE_DIGIT || otpCode == OTP_DEMO_TWO_DIGIT)) {
//                val snackbar =
//                    Snackbar.make(window.decorView, "The OTP Code is wrong.", Snackbar.LENGTH_LONG)
//                        .setAction("Try again!") {
//                            /*  val snackbar =
//                                  Snackbar.make(window.decorView, "The OTP Code is wrong.", Snackbar.LENGTH_LONG)
//                              snackbar.show()*/
//                            mBinding.otpView.clearFocus()
//                            mBinding.otpView.isFocusable = true
//                        }
//                // call show() method to
//                // display the snackbar
//                snackbar.show()
//            } else {
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
            //}

        }
    }

    private fun sendVerificationCode(
        context: Activity, phoneNumber: String
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(context)                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(
        phoneNumber: String, token: ForceResendingToken?
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

//    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
//        if (result.resultCode == RESULT_OK) {
//            Log.d(TAG, "Sign in successful!")
//            //goToMainActivity()
//            //goToCreateConservationActivity()
//
//            if (result.idpResponse is PhoneAuthCredential) {
//                val credential = result.idpResponse as PhoneAuthCredential
//                Log.d(TAG, credential.toString())
//                //startActivity(PhoneAuthFromCodeSnipActivity.newIntent(this))
//            }
//        } else {
//            Toast.makeText(
//                this,
//                "There was an error signing in",
//                Toast.LENGTH_LONG
//            ).show()
//
//            val response = result.idpResponse
//            if (response == null) {
//                Log.w(TAG, "Sign in canceled")
//            } else {
//                Log.w(TAG, "Sign in error", response.error)
//            }
//        }
//    }

//    private fun goToMainActivity() {
//        Toast.makeText(this, "Sign in Successful! goToMainActivity", Toast.LENGTH_LONG).show()
//        startActivity(Intent(this, MainActivity::class.java))
//        finish()
//    }

//    private fun goToCreateConservationActivity() {
//        Toast.makeText(
//            this,
//            "Sign in Successful! goToCreateConservationActivity",
//            Toast.LENGTH_LONG
//        ).show()
//        startActivity(CreateConservationActivity.newIntent(this, "", ""))
//        finish()
//    }

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
