package com.example.projemanag.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.toolbar_sign_up_activity

class SignInActivity : BaseActivity() {

    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth= FirebaseAuth.getInstance()

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }

        setupActionBar()
    }
    fun signInSuccess(user: User){
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

        private fun setupActionBar() {
            setSupportActionBar(toolbar_sign_in_activity)
            val actionBar = supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
            }
            toolbar_sign_in_activity.setNavigationOnClickListener { onBackPressed() }
    }
    private fun signInRegisteredUser(){
        val email:String = et_email_signin.text.toString().trim{it <= ' '}
        val password:String= et_password_signin.text.toString().trim{it <= ' '}
        if(validateForm(email , password)){
            showProgressDialog("Please wait")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
//                        Log.d("Sign in", "signInWithEmail:success")
//                        val user = auth.currentUser
//                      startActivity(Intent(this, MainActivity::class.java))
                        FirestoreClass().loadUserData(this)
                    } else {
                        // If sign in fails, display a message to the user.
//                        Log.w("Sign in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()

                    }
                }
        }

    }
    private fun validateForm(email:String,password:String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnacksBar("please enter an email")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnacksBar("please enter a name")
                false
            }else->{
                true
            }
        }
    }

}