package com.example.projemanag

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.projemanag.activities.Common.Common
import com.example.projemanag.activities.Common.Common.currentResult
import com.example.projemanag.activities.ViewDirections
import com.example.projemanag.model.PlaceDetail
import com.example.projemanag.remote.IGoogleAPIService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_place.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPlace : AppCompatActivity() {

    internal lateinit var mService:IGoogleAPIService
    var mPlace:PlaceDetail?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_place)
//        Init service
        mService= Common.googleApiService
//        Set empty for all text view
        place_name.text=""
        place_address.text=""
        place_open_hour.text=""

        btn_show_map.setOnClickListener{
            val mapIntent=Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.result!!.url))
            startActivity(mapIntent)
        }

        btn_view_direction.setOnClickListener {
            val viewDirection = Intent(this@ViewPlace,ViewDirections::class.java)
            startActivity(viewDirection)
        }

        if(Common.currentResult!!.photos != null && currentResult!!.photos!!.size>0)
           Picasso.with(this)
               .load(getPhotoOfPlace(Common.currentResult!!.photos!![0].photo_reference!!,1000))
               .into(photo)
        if(Common.currentResult!!.rating != null)
            ratingbar.rating=Common.currentResult!!.rating.toFloat()
        else
           ratingbar.visibility= View.GONE
//        Load open hours
        if(Common.currentResult!!.opening_hours != null)
            place_open_hour.text="Open now: "+Common.currentResult!!.opening_hours!!.open_now
        else
            place_open_hour.visibility=View.GONE
//        Use Service to fetch Address and name
        mService.getDetailPlace(getPlaceDetailUrl(Common.currentResult!!.place_id))
            .enqueue(object: Callback<PlaceDetail>{
                override fun onResponse(call: Call<PlaceDetail>, response: Response<PlaceDetail>) {
                    mPlace=response!!.body()
                    place_address.text = mPlace!!.result!!.formatted_address
                    place_name.text=mPlace!!.result!!.name
                }

                override fun onFailure(call: Call<PlaceDetail>?, t: Throwable?) {
                  Toast.makeText(baseContext,""+t!!.message,Toast.LENGTH_SHORT).show()
                }


            })

    }

    private fun getPlaceDetailUrl(place_id: String?): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/details/json")
        url.append("?placeid=$place_id")
        url.append("&key=")
        return url.toString()
    }

    private fun getPhotoOfPlace(photo_reference: String, maxWidth: Int): String {
        val url=StringBuilder("https://maps.googleapis.com/maps/api/places/photo")
        url.append("?maxwidth=$maxWidth")
        url.append("&photoreference=$photo_reference")
        url.append("&key=")
        return url.toString()
    }
}