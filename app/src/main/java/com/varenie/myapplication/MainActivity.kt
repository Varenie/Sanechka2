package com.varenie.myapplication

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File


class MainActivity : AppCompatActivity() {
    private val TAG = "MYLOG"

    private val PERMISSION_ID = 101

    private lateinit var imageViewBase: ImageView
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var imageView5: ImageView

    private lateinit var filePhoto: File

    private val REQUEST_CODE_CAMERA = 13
    private val REQUEST_CODE_GALLERY = 11

    var storage = Firebase.storage("gs://test-55397.appspot.com/")

    var currentTimeMillis = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentTimeMillis = System.currentTimeMillis()
        if (!checkPermission())
            requestPermission()

        val btnSend = findViewById<Button>(R.id.btn_send)
        val btnTakePicture = findViewById<Button>(R.id.btn_take_picture)

        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)
        imageView3 = findViewById(R.id.imageView3)
        imageView4 = findViewById(R.id.imageView4)
        imageView5 = findViewById(R.id.imageView5)

        imageView1.setOnClickListener {
            imageViewBase = imageView1
            openDialog()
        }
        imageView2.setOnClickListener {
            imageViewBase = imageView2
            openDialog()
        }
        imageView3.setOnClickListener {
            imageViewBase = imageView3
            openDialog()
        }
        imageView4.setOnClickListener {
            imageViewBase = imageView4
            openDialog()
        }
        imageView5.setOnClickListener {
            imageViewBase = imageView5
            openDialog()
        }

        btnSend.setOnClickListener {
            sendPhotoToServer()
        }

        btnTakePicture.setOnClickListener {
            val intent = Intent(this, PictureActivity::class.java)
            intent.putExtra("currentTime", currentTimeMillis)
            startActivity(intent)
        }

    }

    private fun openDialog() {
        val dialog = AlertDialog.Builder(this)

        val inflater = LayoutInflater.from(this)
        val myWindow = inflater.inflate(R.layout.photo_layout, null)
        dialog.setView(myWindow)

        val btnGallery = myWindow.findViewById<Button>(R.id.btn_gallery)
        val btnCamera = myWindow.findViewById<Button>(R.id.btn_camera)

        val ad = dialog.show()

        btnCamera.setOnClickListener {
            openCameraForImage()
            ad.dismiss()
        }

        btnGallery.setOnClickListener {
            openGalelleryForImage()
            ad.dismiss()
        }

        dialog.setNegativeButton("Отменить") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

    }

    private fun openCameraForImage() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA)
    }

    private fun sendPhotoToServer() {
        var storageRef = storage.reference.child("folder_$currentTimeMillis")
        val imageViews = arrayOf(imageView1, imageView2, imageView3, imageView4, imageView5)
        var hasNull = false

        for (view in imageViews) {
            if (hasNullOrEmptyDrawable(view)) {
                hasNull = true
            }
        }

        if (!hasNull) {
            for (view in imageViews) {
                val newRef = storageRef.child("photo_${System.currentTimeMillis()}")

                val bitmap = (view.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val uploadTask = newRef.putBytes(data)

                uploadTask.addOnFailureListener {
                    Log.e(TAG, "${it.message}")
                }.addOnSuccessListener {
                    Log.i(TAG, "${it.totalByteCount}")
                }
            }
            Toast.makeText(this, "Изображения отправлены", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Заполните поля изображениями", Toast.LENGTH_SHORT).show()
        }

    }

    private fun hasNullOrEmptyDrawable(iv: ImageView): Boolean {
        val drawable = iv.drawable
        val bitmapDrawable = if (drawable is BitmapDrawable) drawable else null
        return bitmapDrawable == null || bitmapDrawable.bitmap == null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when(requestCode) {
                REQUEST_CODE_CAMERA -> {
                    imageViewBase.setImageBitmap(data.extras?.get("data") as Bitmap)
                }
                REQUEST_CODE_GALLERY -> {
                    imageViewBase.setImageURI(data.data)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openGalelleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_ID
        )
    }
}