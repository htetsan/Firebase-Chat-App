package com.dev_hss.firebasechatapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dev_hss.firebasechatapp.databinding.ActivityCreateconversationBinding

class CreateConservationActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityCreateconversationBinding

    companion object {
        private const val TAG = "CreateConservationActivity"
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, CreateConservationActivity::class.java)
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