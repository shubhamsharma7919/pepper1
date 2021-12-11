package com.example.projemanag.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.projemanag.R
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setupActionBar()

        btn_sign_up.setOnClickListener {
            registerUser()
        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this," you have successfully registered", Toast.LENGTH_LONG).show()
        FirebaseAuth.getInstance().signOut()
        finish()

}

    private fun setupActionBar() {
        setSupportActionBar(toolbar_sign_up_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        toolbar_sign_up_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun registerUser(){
        val name:String=et_name.text.toString().trim{it<=' '}
        val email:String=et_email.text.toString().trim{it<=' '}
        val password:String=et_password.text.toString().trim{it<=' '}
        if(validateForm(name,email,password)){
            showProgressDialog("Please wait")
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
//                hideProgressDialog()
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user= User(firebaseUser.uid,name,registeredEmail)
                    FirestoreClass().registerUser(this,user)
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()


                }

            }

        }

    }


    private fun validateForm(name:String,email:String,password:String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnacksBar("Please enter the name")
                false
            }
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