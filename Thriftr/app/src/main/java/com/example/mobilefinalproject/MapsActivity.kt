package com.example.mobilefinalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mobilefinalproject.data.Listing
import com.example.mobilefinalproject.databinding.ActivityMapsBinding
import com.example.mobilefinalproject.dialog.ListingDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.*
import java.net.URL
import kotlin.concurrent.thread


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    MyLocationManager.OnNewLocationAvailable {
    companion object {
        const val KEY_LISTING_URL = "KEY_LISTING_URL"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var myLocationManager: MyLocationManager
    private lateinit var curLocation: LatLng

    var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        curLocation = LatLng(0.toDouble(), 0.toDouble())

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myLocationManager = MyLocationManager(this, this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestNeededPermission()

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, CreateListingActivity::class.java))
            queryPosts()
            myLocationManager.stopLocationMonitoring()

        }

    }

    fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        } else {
            // we have the permission
            myLocationManager.startLocationMonitoring()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "ACCESS_FINE_LOCATION perm granted", Toast.LENGTH_SHORT)
                        .show()

                    myLocationManager.startLocationMonitoring()
                } else {
                    Toast.makeText(
                        this,
                        "ACCESS_FINE_LOCATION perm NOT granted", Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                val imgUrl = marker.tag as String
                val dialog = ListingDialog()

                val bundle = Bundle()
                bundle.putSerializable(KEY_LISTING_URL, imgUrl)
                dialog.arguments = bundle

                dialog.show(supportFragmentManager, "TAG_URL")
                return true
            }
        })
    }

    fun addMarker(curLocation: LatLng, imgUrl: String) {
        var bmp: Bitmap? = null
        thread {
            val url = URL(imgUrl)
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            if (bmp != null) {
                runOnUiThread {
                    val itemMarker = curLocation
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(itemMarker))

                    val marker = mMap.addMarker(
                        MarkerOptions().position(itemMarker)
                            .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    )
                    marker?.tag = imgUrl
                }
            }
        }
    }

    override fun onNewLocation(location: Location) {
        curLocation = LatLng(location.latitude, location.longitude)
    }

    fun queryPosts() {
        val queryPosts = FirebaseFirestore.getInstance().collection(
            CreateListingActivity.COLLECTION_LISTINGS
        )


        val eventListener = object : EventListener<QuerySnapshot> {
            override fun onEvent(
                querySnapshot: QuerySnapshot?,
                e: FirebaseFirestoreException?
            ) {
                if (e != null) {
                    Toast.makeText(
                        this@MapsActivity, "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                for (docChange in querySnapshot?.getDocumentChanges()!!) {
                    if (docChange.type == DocumentChange.Type.ADDED) {
                        val listing = docChange.document.toObject(Listing::class.java)
                        if (listing.imgUrl.contains("http")) {
                            addMarker(curLocation, listing.imgUrl)
                        }

                    } else if (docChange.type == DocumentChange.Type.REMOVED) {
                        //here we would do something if BOUGHT
                    } else if (docChange.type == DocumentChange.Type.MODIFIED) {

                    }
                }
            }
        }

        snapshotListener = queryPosts.addSnapshotListener(eventListener)
    }
}