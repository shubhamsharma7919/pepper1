package com.example.projemanag.activities

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projemanag.MapsActivity
import com.example.projemanag.R
import com.example.projemanag.activities.Common.Common

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.projemanag.databinding.ActivityViewDirectionsBinding
import com.example.projemanag.remote.IGoogleAPIService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import dmax.dialog.SpotsDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewDirections : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityViewDirectionsBinding
    lateinit var mService:IGoogleAPIService
    lateinit var mCurrentMarker: Marker
    lateinit var polyLine:Polyline

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    lateinit var locationCallback: LocationCallback
    lateinit var mLastLocation:Location


    private fun checkLocationPermission():Boolean {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MapsActivity.MY_PERMISSION_CODE
                )
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MapsActivity.MY_PERMISSION_CODE
                )
            return false
        }
        else
            return true
    }
    private fun builLocationRequest()  {
        locationRequest=com.google.android.gms.location.LocationRequest()
        locationRequest.priority=com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval=5000
        locationRequest.fastestInterval=3000
        locationRequest.smallestDisplacement=10f

    }
    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }
    private fun buildLocationCallBack() {
        locationCallback=object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                mLastLocation=p0!!.lastLocation

//                add your location to your map
                val markerOptions=MarkerOptions()
                    .position(LatLng(mLastLocation.latitude,mLastLocation.longitude))
                    .title("Your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                mCurrentMarker= mMap!!.addMarker(markerOptions)!!
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(mLastLocation.latitude,mLastLocation.longitude)))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12.0f))
//                create marker for destination location
                val destinationLatLng= LatLng(Common.currentResult!!.geometry.location.lat.toDouble(),
                    Common.currentResult!!.geometry.location.lng.toDouble())
                mMap!!.addMarker(MarkerOptions().position(destinationLatLng)
                    .title(Common.currentResult!!.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
//                get direction
                drawPath(mLastLocation,Common.currentResult!!.geometry.location)
            }
        }
    }

    private fun drawPath(mLastLocation: Location?, location: com.example.projemanag.model.Location) {
          if(polyLine!=null)
              polyLine.remove()

        val origin:String=StringBuilder(mLastLocation!!.latitude.toString())
            .append(",")
            .append(mLastLocation!!.longitude.toString())
            .toString()

        val destination =StringBuilder(location.lat.toString()).append(location.lng.toString())
        mService.getDirections(origin,destination = )
            .enqueue(object: Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                  ParserTask().execute(response.body()!!.toString())
                }

                override fun onFailure(call: Call<String>?, t: Throwable?) {
                    Log.d("EDMTDEV",t.message)
                }

            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewDirectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mService= Common.googleApiServiceScalars

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(checkLocationPermission()){
                builLocationRequest()
                buildLocationCallBack();
                fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(com.google.android.gms.location.LocationRequest(),locationCallback, Looper.myLooper()!!)
            }

        }

        else{
            builLocationRequest()
            buildLocationCallBack();
            fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(com.google.android.gms.location.LocationRequest(),locationCallback, Looper.myLooper()!!)
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

    }
}

class ParserTask: AsyncTask<String, Int, List<List<HashMap<String, String>>>>(){
    internal val waitingDialog: AlertDialog = SpotsDialog(this@ViewDirections)
    override fun doInBackground(vararg params: String?): List<List<HashMap<String, String>>> {

    }

}
