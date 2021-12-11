package com.example.projemanag.firebase

import android.util.Log
import com.example.projemanag.activities.SignInActivity
import com.example.projemanag.activities.SignUpActivity
import com.example.projemanag.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity : SignUpActivity,userInfo : User) {
        mFireStore.collection("Users")
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge()).addOnCompleteListener{
                activity.userRegisteredSuccess()
            }.addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun signInUser(activity: SignInActivity){
        mFireStore.collection("Users")
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener{ document ->
               val loggedInUser =document.toObject(User::class.java)!!
                activity.signInSuccess(loggedInUser)
            }.addOnFailureListener {
                    e ->
                Log.e("Sign in user", "Error writing document",e)
            }
    }

    fun getCurrentUserId():String{
       var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID =""
            if (currentUser != null) {
                currentUserID=currentUser.uid
            }
        return currentUserID
    }

}