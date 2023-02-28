package com.dev_hss.firebasechatapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dev_hss.firebasechatapp.databinding.ActivityCreateconversationBinding

class CreateConservationActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityCreateconversationBinding

    companion object {
        private const val TAG = "CreateConservationActivity"
        private const val BUNDLE_PHONE_NUMBER = "BUNDLE_PHONE_NUMBER"
        private const val BUNDLE_USER_ID = "BUNDLE_USER_ID"
        fun newIntent(context: Context, phoneNum: String, userIdParam: String): Intent {
            val intent = Intent(context, CreateConservationActivity::class.java)
            intent.putExtra(BUNDLE_PHONE_NUMBER, phoneNum)
            intent.putExtra(BUNDLE_USER_ID, userIdParam)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCreateconversationBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.btnCreateConservation.setOnClickListener {
            startActivity(MainActivity.newIntent(this, createAccount = true))
        }
    }
}