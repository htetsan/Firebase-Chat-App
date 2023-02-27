package com.dev_hss.firebasechatapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev_hss.firebasechatapp.databinding.ActivityMainBinding
import com.dev_hss.firebasechatapp.model.FriendlyMessage
import com.dev_hss.firebasechatapp.model.UserAccountVO
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var wrapContentLinearLayoutManager: WrapContentLinearLayoutManager
    private lateinit var binding: ActivityMainBinding

    //    private lateinit var manager: LinearLayoutManager
    private lateinit var db: FirebaseDatabase
    private lateinit var adapter: FriendlyMessageAdapter

    // Firebase instance variables
    private lateinit var auth: FirebaseAuth

    private val openDocument = registerForActivityResult(MyOpenDocumentContract()) { uri ->
        uri?.let { onImageSelected(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        if (BuildConfig.DEBUG) {
//            Firebase.database.useEmulator("10.0.2.2", 9000)
//            Firebase.auth.useEmulator("10.0.2.2", 9099)
//            Firebase.storage.useEmulator("10.0.2.2", 9199)
//        }

        // Initialize Firebase Auth and check if the user is signed in
        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(SignInActivity.newIntent(this))
            finish()
            return
        } else {
            App.userId = auth.currentUser?.email?.let { createUserIdWithEmail(it) }.toString()
        }

        db = Firebase.database
        val accountRef = getUserId()?.let {
            db.reference.child("users").child(it)
        }

        if (isCreateAccount) {
            val userAccount = UserAccountVO(
                getEmail(), getUserName(), getPhoneNumber(), getPhotoUrl()
            )
            getUserId()?.let {
                db.reference.child(USERS_CHILD).child(it).setValue(userAccount)
            }
        }

        // Initialize Realtime Database and FirebaseRecyclerAdapter
        //val messagesRef = db.reference.child("chats/chat_id_1/messages")
        val messagesRef = db.reference.child("chats").child("chat_id_1").child("messages")

        val options = FirebaseRecyclerOptions.Builder<FriendlyMessage>()
            .setQuery(messagesRef, FriendlyMessage::class.java).build()

        adapter = FriendlyMessageAdapter(options, getUserName())
        binding.progressBar.visibility = ProgressBar.INVISIBLE

        wrapContentLinearLayoutManager =
            WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        wrapContentLinearLayoutManager.stackFromEnd = true
        binding.messageRecyclerView.layoutManager = wrapContentLinearLayoutManager
        binding.messageRecyclerView.adapter = adapter

        // Scroll down when a new message arrives
        // See MyScrollToBottomObserver for details
        adapter.registerAdapterDataObserver(
            MyScrollToBottomObserver(
                binding.messageRecyclerView, adapter, wrapContentLinearLayoutManager
            )
        )


        // Disable the send button when there's no text in the input field
        // See MyButtonObserver for details
        binding.messageEditText.addTextChangedListener(MyButtonObserver(binding.sendButton))


        // When the send button is clicked, send a text message
        binding.sendButton.setOnClickListener {
            val friendlyMessage = FriendlyMessage(
                userId = getUserId(),
                binding.messageEditText.text.toString(),
                getUserName(),
                getPhotoUrl(),
                null, /* no image */
                Calendar.getInstance().timeInMillis
            )
            db.reference.child("chats").child("chat_id_1").child("messages")
                .child(generateMessageIdWithTime()).setValue(friendlyMessage)
            binding.messageEditText.setText("")
        }


        // When the image button is clicked, launch the image picker
        binding.addMessageImageView.setOnClickListener {
            openDocument.launch(arrayOf("image/*"))
        }


        // The test phone number and code should be whitelisted in the console.
        val phoneNumber = "+959984458969"
        val smsCode = "123456"

        val firebaseAuth = Firebase.auth
        val firebaseAuthSettings = firebaseAuth.firebaseAuthSettings

        // Configure faking the auto-retrieval with the whitelisted numbers.
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode)

        val PhoneOptions = PhoneAuthOptions.newBuilder(Firebase.auth).setPhoneNumber(phoneNumber)
            .setTimeout(10, TimeUnit.SECONDS).setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onCodeSent(
                    verificationId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    // Save the verification id somewhere
                    // ...

                    // The corresponding whitelisted code above should be used to complete sign-in.
//                    this@MainActivity.enableUserManuallyInputCode()\

                    Log.d(TAG, "onCodeSent: $verificationId")
                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    // Sign in with the credential
                    // ...
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // ...
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(PhoneOptions)


        //
//    val phoneNum = "+16505554567"
//    val testVerificationCode = "123456"
//
//    // Whenever verification is triggered with the whitelisted number,
//    // provided it is not set for auto-retrieval, onCodeSent will be triggered.
//    val options = PhoneAuthOptions.newBuilder(Firebase.auth).setPhoneNumber(phoneNum)
//        .setTimeout(30L, TimeUnit.SECONDS).setActivity(this)
//        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//            override fun onCodeSent(
//                verificationId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken
//            ) {
//                // Save the verification id somewhere
//                // ...
//
//                // The corresponding whitelisted code above should be used to complete sign-in.
//                this@MainActivity.enableUserManuallyInputCode()
//            }
//
//            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
//                // Sign in with the credential
//                // ...
//            }
//
//            override fun onVerificationFailed(e: FirebaseException) {
//                // ...
//            }
//        }).build()
//    PhoneAuthProvider.verifyPhoneNumber(options)
//
    }

    private fun generateMessageId(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateMessageIdWithTime(): String {
        return Calendar.getInstance().timeInMillis.toString()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in.
//        if (auth.currentUser == null) {
//            startActivity(SignInActivity.newIntent(this))
//            finish()
//            return
//        }
//        else {
//            App.userId = auth.currentUser?.email?.let { createUserIdWithEmail(it) }.toString()
//            Toast.makeText(this, App.userId, Toast.LENGTH_SHORT).show()
//        }
    }

    public override fun onPause() {
        adapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        if (auth.currentUser != null) {
            adapter.startListening()
        }
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onImageSelected(uri: Uri) {
        Log.d(TAG, "Uri: $uri")
        val user = auth.currentUser
        val tempMessage = FriendlyMessage(
            getUserId(),
            null,
            getUserName(),
            getPhotoUrl(),
            LOADING_IMAGE_URL,
            Calendar.getInstance().timeInMillis
        )
        db.reference.child(MESSAGES_CHILD).push().setValue(
            tempMessage, DatabaseReference.CompletionListener { databaseError, databaseReference ->
                if (databaseError != null) {
                    Log.w(
                        TAG, "Unable to write message to database.", databaseError.toException()
                    )
                    return@CompletionListener
                }

                // Build a StorageReference and then upload the file
                val key = databaseReference.key
                val storageReference = Firebase.storage.getReference(user!!.uid).child(key!!)
                    .child(uri.lastPathSegment!!)
                putImageInStorage(storageReference, uri, key)
            })
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, key: String?) {

        // Upload the image to Cloud Storage
        storageReference.putFile(uri).addOnSuccessListener(
            this
        ) { taskSnapshot -> // After the image loads, get a public downloadUrl for the image
            // and add it to the message.
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                val friendlyMessage = FriendlyMessage(
                    null,
                    null,
                    getUserName(),
                    getPhotoUrl(),
                    uri.toString(),
                    Calendar.getInstance().timeInMillis
                )
                db.reference.child(MESSAGES_CHILD).child(key!!).setValue(friendlyMessage)
            }
        }.addOnFailureListener(this) { e ->
            Log.w(
                TAG, "Image upload task was unsuccessful.", e
            )
        }
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        SignInActivity.newIntent(this)
        finish()
    }

    private fun getPhotoUrl(): String? {
        val user = auth.currentUser
        return user?.photoUrl?.toString()
    }

    private fun getUserName(): String? {
        val user = auth.currentUser
        return if (user != null) {
            user.displayName
        } else ANONYMOUS
    }

    private fun getEmail(): String? {
        val user = auth.currentUser
        return if (user != null) {
            user.email
        } else TEST_EMAIL
    }

    private fun getPhoneNumber(): String? {
        val user = auth.currentUser
        return if (user != null) {
            user.phoneNumber
        } else TEST_PHONE
    }

    fun generateChatId(userId1: String, userId2: String): String {
        val sortedUserIds = listOf(userId1, userId2).sorted()
        return sortedUserIds.joinToString("_")
    }

    private fun createUserIdWithEmail(email: String): String {
        return email.replace(".", "_")
    }


    private fun getUserId(): String? {
        return if (auth.currentUser != null) {
            val userId = auth.currentUser?.email?.let { createUserIdWithEmail(it) }
            userId
        } else ANONYMOUS
    }
//
//    val phoneNum = "+16505554567"
//    val testVerificationCode = "123456"
//
//    // Whenever verification is triggered with the whitelisted number,
//    // provided it is not set for auto-retrieval, onCodeSent will be triggered.
//    val options = PhoneAuthOptions.newBuilder(Firebase.auth).setPhoneNumber(phoneNum)
//        .setTimeout(30L, TimeUnit.SECONDS).setActivity(this)
//        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//            override fun onCodeSent(
//                verificationId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken
//            ) {
//                // Save the verification id somewhere
//                // ...
//
//                // The corresponding whitelisted code above should be used to complete sign-in.
//                this@MainActivity.enableUserManuallyInputCode()
//            }
//
//            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
//                // Sign in with the credential
//                // ...
//            }
//
//            override fun onVerificationFailed(e: FirebaseException) {
//                // ...
//            }
//        }).build()
//    PhoneAuthProvider.verifyPhoneNumber(options)
//


    companion object {
        const val userId1 = "user_id_1"
        val userId2 = "user_id_2"
        //val chatId = generateChatId(userId1, userId2)

        private const val TAG = "MainActivity"
        const val MESSAGES_CHILD = "messages"
        const val USERS_CHILD = "users"
        const val TEST_EMAIL = "htetsoesan8888@gmail.com"
        const val TEST_PHONE = "09984458969"
        const val ANONYMOUS = "anonymous"
        var isCreateAccount = false

        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"

        fun newIntent(context: Context, createAccount: Boolean): Intent {
            isCreateAccount = createAccount
            val intent = Intent(context, MainActivity::class.java)
            return intent
        }

    }
}