package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.MainActivity
import com.example.projemanag.activities.MyProfileActivity
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
    fun updateUserProfileData(activity:MyProfileActivity,userHashMap: HashMap<String,Any>){
        mFireStore.collection("Users")
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile Data Updated successfully!")
                activity.profileUpdateSuccess()
            }.addOnFailureListener {
                e->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
                Toast.makeText(activity,"Error while updating the profile",Toast.LENGTH_SHORT).show()
            }
    }
    fun loadUserData(activity: Activity){
        mFireStore.collection("Users")
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener{ document ->
               val loggedInUser =document.toObject(User::class.java)!!
               when(activity){
                   is SignInActivity -> {
                       activity.signInSuccess(loggedInUser)
                   }
                   is MainActivity ->{
                       activity.updateNavigationUserDetails(loggedInUser)
                   }
                   is MyProfileActivity ->{
                       activity.setUserDataInUI(loggedInUser)
                   }
               }


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