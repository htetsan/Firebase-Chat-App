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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.dev_hss.firebasechatapp.databinding.ActivitySignInBinding
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    // ActivityResultLauncher
    // TODO: Implement

    // Firebase instance variables
    private lateinit var auth: FirebaseAuth

    private val signIn: ActivityResultLauncher<Intent> =
        registerForActivityResult(FirebaseAuthUIActivityResultContract(), this::onSignInResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = Firebase.auth

        //disableAutofill()
    }


//    @TargetApi(Build.VERSION_CODES.O)
//    private fun disableAutofill() {
//        window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
//    }

    public override fun onStart() {
        super.onStart()
        // If there is no signed in user, launch FirebaseUI
        // Otherwise head to MainActivity
        val customLayout = AuthMethodPickerLayout.Builder(R.layout.activity_phone_auth)
            .setPhoneButtonId(R.id.phone_button)
            .build()

        if (Firebase.auth.currentUser == null) {
            // Sign in with FirebaseUI, see docs for more details:
            // https://firebase.google.com/docs/auth/android/firebaseui
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(customLayout)
                .setLogo(R.mipmap.ic_launcher)
                .setAvailableProviders(
                    listOf(
//                        AuthUI.IdpConfig.EmailBuilder().build(),
//                        AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.PhoneBuilder().build()
                    )
                )
                .build()

            signIn.launch(signInIntent)
        } else {
            //goToMainActivity()
            goToCreateConservationActivity()
        }
    }

    private fun signIn() {

    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "Sign in successful!")
            //goToMainActivity()
            //goToCreateConservationActivity()

            if (result.idpResponse is PhoneAuthCredential) {
                val credential = result.idpResponse as PhoneAuthCredential
                Log.d(TAG, credential.toString())
                startActivity(PhoneAuthFromCodeSnipActivity.newIntent(this))
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
        startActivity(CreateConservationActivity.newIntent(this))
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
