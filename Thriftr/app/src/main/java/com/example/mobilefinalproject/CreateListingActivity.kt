package com.example.mobilefinalproject

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mobilefinalproject.data.Listing
import com.example.mobilefinalproject.databinding.ActivityCreateListingBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*

class CreateListingActivity : AppCompatActivity() {
    companion object {
        const val COLLECTION_LISTINGS = "listings"
        const val REQUEST_CAMERA_PERMISSION = 1001
    }

    private lateinit var binding: ActivityCreateListingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            if (uploadBitmap != null) {
                try {
                    uploadListingWithImage()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                uploadListing()
            }
            finish()

        }
        binding.btnAttach.setOnClickListener {
            openCamera()
        }
        requestNeededPermission()
    }

    var uploadBitmap: Bitmap? = null

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                uploadBitmap = data!!.extras!!.get("data") as Bitmap
                binding.imgAttach.setImageBitmap(uploadBitmap)
                binding.imgAttach.visibility = View.VISIBLE
            }
        }

    fun openCamera() {
        val intentPhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(intentPhoto)
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.CAMERA
                )
            ) {
                Toast.makeText(
                    this,
                    "I need it for camera", Toast.LENGTH_SHORT
                ).show()
            }

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )

        } else {
            // we already have permission
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "CAMERA perm granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "CAMERA perm NOT granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadListing(imgUrl: String = "") {
        val myListing = Listing(
            FirebaseAuth.getInstance().currentUser!!.uid,
            FirebaseAuth.getInstance().currentUser!!.email!!,
            binding.etSeller.text.toString(),
            binding.etTitle.text.toString(),
            binding.etDescription.text.toString(),
            binding.etPrice.text.toString().toFloat(),
            imgUrl
        )
        val listingsCollection = FirebaseFirestore.getInstance().collection(COLLECTION_LISTINGS)
        listingsCollection.add(myListing)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Listing saved", Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this, "" +
                            "Error: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @Throws(Exception::class)
    private fun uploadListingWithImage() {
        // Convert bitmap to JPEG and put it in a byte array
        val baos = ByteArrayOutputStream()
        uploadBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        // prepare the empty file in the cloud
        val storageRef = FirebaseStorage.getInstance().getReference()
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")

        // upload the jpeg byte array to the created empty file
        newImagesRef.putBytes(imageInBytes)
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@CreateListingActivity,
                    exception.message, Toast.LENGTH_SHORT
                ).show()
                exception.printStackTrace()
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                newImagesRef.downloadUrl.addOnCompleteListener(
                    object : OnCompleteListener<Uri> {
                        override fun onComplete(task: Task<Uri>) {
                            // the public URL of the image is: task.result.toString()
                            uploadListing(task.result.toString())
                        }
                    })
            }
    }

}
