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

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.dev_hss.firebasechatapp.FriendlyMessageAdapter.Companion.VIEW_TYPE_OTHER_TEXT
import com.dev_hss.firebasechatapp.MainActivity.Companion.ANONYMOUS
import com.dev_hss.firebasechatapp.R
import com.dev_hss.firebasechatapp.databinding.ImageMessageBinding
import com.dev_hss.firebasechatapp.databinding.MessageBinding
import com.dev_hss.firebasechatapp.databinding.MyImageMessageBinding
import com.dev_hss.firebasechatapp.databinding.MyMessageBinding
import com.dev_hss.firebasechatapp.databinding.OtherMessageBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.dev_hss.firebasechatapp.model.FriendlyMessage
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

// The FirebaseRecyclerAdapter class and options come from the FirebaseUI library
// See: https://github.com/firebase/FirebaseUI-Android
class FriendlyMessageAdapter(
    private val options: FirebaseRecyclerOptions<FriendlyMessage>,
    private val currentUserName: String?
) : FirebaseRecyclerAdapter<FriendlyMessage, ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_OTHER_TEXT -> {
                val view = inflater.inflate(R.layout.other_message, parent, false)
                val binding = OtherMessageBinding.bind(view)
                MessageViewHolder(binding)
            }
            VIEW_TYPE_OTHER_IMAGE -> {
                val view = inflater.inflate(R.layout.image_message, parent, false)
                val binding = ImageMessageBinding.bind(view)
                ImageMessageViewHolder(binding)
            }
            VIEW_TYPE_MY_TEXT -> {
                val view = inflater.inflate(R.layout.my_message, parent, false)
                val binding = MyMessageBinding.bind(view)
                ImageMessageViewHolder(binding)
            }
            VIEW_TYPE_MY_IMAGE -> {
                val view = inflater.inflate(R.layout.my_image_message, parent, false)
                val binding = MyImageMessageBinding.bind(view)
                ImageMessageViewHolder(binding)
            }
            else -> {
                val view = inflater.inflate(R.layout.other_message, parent, false)
                val binding = MessageBinding.bind(view)
                MessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: FriendlyMessage) {
        if (options.snapshots.size > 0) {
            if (options.snapshots[position].text != null) {
                (holder as OtherMessageViewHolder).bind(model)
            } else {
                (holder as ImageMessageViewHolder).bind(model)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        val message = options.snapshots[position]
        return if (App.userId == message.userId) {
            if (message.text != null) VIEW_TYPE_MY_TEXT else VIEW_TYPE_MY_IMAGE
        } else {
            if (message.text != null) VIEW_TYPE_OTHER_TEXT else VIEW_TYPE_OTHER_IMAGE
        }

        //return if (options.snapshots[position].text != null) VIEW_TYPE_TEXT else VIEW_TYPE_IMAGE
    }

    inner class OtherMessageViewHolder(private val binding: OtherMessageBinding) : ViewHolder(binding.root) {
        fun bind(item: FriendlyMessage) {

            binding.txtOtherMessage.text = item.text
            setTextColor(item.name, binding.txtOtherMessage)

            binding.txtOtherUser.text = item.name ?: ANONYMOUS
            if (item.photoUrl != null) {
                loadImageIntoView(binding.messengerImageView, item.photoUrl)
            } else {
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp)
            }
        }


        private fun setTextColor(userName: String?, textView: TextView) {
            if (userName != ANONYMOUS && currentUserName == userName && userName != null) {
                textView.setBackgroundResource(R.drawable.rounded_message_blue)
                textView.setTextColor(Color.WHITE)
            } else {
                textView.setBackgroundResource(R.drawable.rounded_message_gray)
                textView.setTextColor(Color.BLACK)
            }
        }
    }

    inner class ImageMessageViewHolder(private val binding: ImageMessageBinding) :
        ViewHolder(binding.root) {
        fun bind(item: FriendlyMessage) {
            loadImageIntoView(binding.messageImageView, item.imageUrl!!, false)

            binding.messengerTextView.text = item.name ?: ANONYMOUS
            if (item.photoUrl != null) {
                loadImageIntoView(binding.messengerImageView, item.photoUrl)
            } else {
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp)
            }
        }
    }

    private fun loadImageIntoView(view: ImageView, url: String, isCircular: Boolean = true) {
        if (url.startsWith("gs://")) {
            val storageReference = Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    loadWithGlide(view, downloadUrl, isCircular)
                }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG,
                        "Getting download url was not successful.",
                        e
                    )
                }
        } else {
            loadWithGlide(view, url, isCircular)
        }
    }

    private fun loadWithGlide(view: ImageView, url: String, isCircular: Boolean = true) {
        Glide.with(view.context).load(url).into(view)
        var requestBuilder = Glide.with(view.context).load(url)
        if (isCircular) {
            requestBuilder = requestBuilder.transform(CircleCrop())
        }
        requestBuilder.into(view)
    }

    companion object {
        const val TAG = "MessageAdapter"
        const val VIEW_TYPE_MY_TEXT = 1
        const val VIEW_TYPE_MY_IMAGE = 2
        const val VIEW_TYPE_OTHER_TEXT = 3
        const val VIEW_TYPE_OTHER_IMAGE = 4
    }
}
