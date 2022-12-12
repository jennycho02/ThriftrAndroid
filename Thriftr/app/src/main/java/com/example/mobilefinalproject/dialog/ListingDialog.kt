package com.example.mobilefinalproject.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.example.mobilefinalproject.MapsActivity
import com.example.mobilefinalproject.MessagingActivity
import com.example.mobilefinalproject.R
import com.example.mobilefinalproject.databinding.ListingDialogBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URL


class ListingDialog : DialogFragment() {

    lateinit var imgUrl: String
    val db = Firebase.firestore

    val SELLER_NAME = "SELLER_NAME"

    //pass the image URL
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogBuilder = AlertDialog.Builder(requireContext())

        if (arguments != null && requireArguments()
                .containsKey(MapsActivity.KEY_LISTING_URL)
        ) {
            imgUrl = requireArguments().getSerializable(
                MapsActivity.KEY_LISTING_URL
            ) as String
            //dialogBuilder.setTitle(imgUrl)
        }

        val dialogViewBinding = ListingDialogBinding.inflate(requireActivity().layoutInflater)
        dialogBuilder.setView(dialogViewBinding.root)

        val policy = ThreadPolicy.Builder()
            .permitAll().build()
        StrictMode.setThreadPolicy(policy)

        db.collection("listings")
            .whereEqualTo("imgUrl", imgUrl)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val itmDesc = document.data["itemDesc"]
                    val itmPrice = document.data["itemPrice"]
                    val itmName = document.data["itemTitle"]

                    dialogViewBinding.tvListingTitle.text = itmName.toString()
                    dialogViewBinding.tvListingPrice.text = getString(
                        R.string.itemPriceString, itmPrice.toString().toFloat()
                    )
                    dialogViewBinding.tvListingDescription.text = itmDesc.toString()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        val url = URL(imgUrl)
        val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        dialogViewBinding.ivListingImage.setImageBitmap(image)

        dialogViewBinding.btnBuy.setOnClickListener {
            openMessenger()
        }

        dialogViewBinding.btnExit.setOnClickListener {
            dismiss()
        }

        return dialogBuilder.create()
    }

    fun openMessenger() {
        val infoIntent = Intent((context as MapsActivity), MessagingActivity::class.java)
        startActivity(infoIntent)
    }

}