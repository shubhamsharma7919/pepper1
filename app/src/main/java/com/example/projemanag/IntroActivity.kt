package com.example.projemanag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

//        window.setFlags(
//            windowManager.LayoutParams.FLAG_FULLSCREEN,
//                    windowManager.LayoutParams.FLAG_FULLSCREEN
//        )

        btn_sign_up_intro.setOnClickListener{
            startActivity(Intent(this,SignUpActivity::class.java))
        }
    }

}