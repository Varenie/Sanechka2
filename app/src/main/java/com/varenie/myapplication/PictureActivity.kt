package com.varenie.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PictureActivity : AppCompatActivity() {
    val storageRef = Firebase.storage("gs://test-55397.appspot.com/").reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        val imageView = findViewById<ImageView>(R.id.picture)

        val currentTimeMillis = intent.getLongExtra("currentTime", 0)

        storageRef.child("folder_$currentTimeMillis/photo_6.jpg").getBytes(Long.MAX_VALUE).addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            imageView.setImageBitmap(bmp)
        }.addOnFailureListener {
            Log.e("MYLOG", "${it.message}")
        }
    }
}