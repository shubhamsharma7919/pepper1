package com.example.projemanag

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationRequestCompat
import com.example.projemanag.activities.Common.Common
import com.example.projemanag.databinding.ActivityMapsBinding
import com.example.projemanag.model.myplaces
import com.example.projemanag.remote.IGoogleAPIService
//import com.example.travelguider.Common.Common
//import com.example.travelguider.R
//import com.example.travelguider.Remote.IGoogleAPIService
//import com.example.travelguider.databinding.ActivityMapsBinding
//import com.example.travelguider.models.MyPlaces
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var latitude:Double=0.toDouble()
    private var longitude:Double=0.toDouble()


    private lateinit var mLastLocation:Location

    private var mMarker:Marker?=null

    //Location

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    lateinit var locationCallback: LocationCallback


    companion object{
        const val MY_PERMISSION_CODE:Int=1000
    }

    lateinit var mService: IGoogleAPIService

    internal lateinit var currentPlace:myplaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Init Service

        mService= Common.googleApiService

        //Request runtime permission

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkLocationPermission()){
                builLocationRequest()
                buildLocationCallBack();
                fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(com.google.android.gms.location.LocationRequest(),locationCallback, Looper.myLooper()!!)
            }

        }

        else{
            builLocationRequest()
            buildLocationCallBack();
            fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(com.google.android.gms.location.LocationRequest(),locationCallback, Looper.myLooper()!!)
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener { item->
            when(item.itemId){
                R.id.action_market->nearByPlace("market")
                R.id.action_restaurant->nearByPlace("restaurant")
                R.id.action_hotel->nearByPlace("hotel")
                R.id.action_travel->nearByPlace("travel")
            }
            true
        }

    }

    private fun nearByPlace(typePlace: String) {
        // Clear all marker on map

        mMap.clear()
        //build URL request base on Location

        var url =getUrl(latitude,longitude,typePlace)

        mService.getNearbyPlaces(url)
            .enqueue(object :Callback<myplaces>{
                override fun onResponse(call: Call<myplaces>, response: Response<myplaces>) {
                    currentPlace=response!!.body()!!

                    if(response!!.isSuccessful){
                        for(i in 0 until response!!.body()!!.results!!.size){
                            val markerOptions=MarkerOptions()
                            val googlePlace=response.body()!!.results!![i]
                            val lat=googlePlace.geometry!!.location!!.lat
                            val lng=googlePlace.geometry!!.location!!.lng
                            val placeName=googlePlace.name
                            val latLng=LatLng(lat,lng)
                            markerOptions.position(latLng)
                            markerOptions.title(placeName)

                            if(typePlace.equals("market"))
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            else if(typePlace.equals("movie_theater"))
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))

                            else if(typePlace.equals("restaurant"))
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                            else if (typePlace.equals("tourist_attraction"))
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                            else
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                            markerOptions.snippet(i.toString()) // Assign index for Market

                            //Add marker to map

                            mMap!!.addMarker(markerOptions)

                            //Move Camera
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(20f))



                        }


                    }
                }

                override fun onFailure(call: Call<myplaces>, t: Throwable) {
                    Toast.makeText(baseContext,""+t!!.message,Toast.LENGTH_SHORT).show()
                }

            })

    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {

        val googlePlaceUrl=StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=1000") //10km
        googlePlaceUrl.append("&types=$typePlace")
        googlePlaceUrl.append("&key=")

        Log.d("URL ", googlePlaceUrl.toString())
        return googlePlaceUrl.toString()

    }

    private fun buildLocationCallBack() {
        locationCallback=object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                mLastLocation=p0!!.locations.get(p0!!.locations.size-1) //Get Lat location
                if(mMarker!=null){
                    mMarker!!.remove()
                }
                latitude=mLastLocation.latitude
                longitude=mLastLocation.longitude

                val latLng=LatLng(latitude,longitude)
                val markerOptions=MarkerOptions()
                    .position(latLng)
                    .title("Your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker=mMap!!.addMarker(markerOptions)

                //Move Camera


                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }
    }

    private fun builLocationRequest()  {
        locationRequest=com.google.android.gms.location.LocationRequest()
        locationRequest.priority=com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval=5000
        locationRequest.fastestInterval=3000
        locationRequest.smallestDisplacement=10f

    }

    private fun checkLocationPermission():Boolean {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            return false
        }
        else
            return true
    }

    //Override OnRequestPermissionResult

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            MY_PERMISSION_CODE->{
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                        if (checkLocationPermission()){
                            builLocationRequest()
                            buildLocationCallBack();
                            fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(com.google.android.gms.location.LocationRequest(),locationCallback, Looper.myLooper()!!)
                            mMap!!.isMyLocationEnabled=true
                        }

                }else
                {
                    Toast.makeText(this,"Permission Denied ",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Init Google Play Service
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                mMap!!.isMyLocationEnabled=true
            }
        }
        else
            mMap!!.isMyLocationEnabled=true

        //Enable Zoom control

        mMap.uiSettings.isZoomControlsEnabled=true

      mMap!!.setOnMarkerClickListener { marker ->
          Common.currentResult=currentPlace!!.results!![Integer.parseInt(marker.snippet)]

          startActivity(Intent (this@MapsActivity,ViewPlace::class.java))
          true
      }
    }
}