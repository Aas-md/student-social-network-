package com.example.pdfupload2.Daos

import android.util.Log
import com.example.pdfupload2.Models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class UserDao(){
    val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")




    fun addUser(user: User?){

        if (user != null) {
            GlobalScope.launch(Dispatchers.IO) {
                val userDocument = usersCollection.document(user.uId)

                userDocument.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                     // do not need to add user again in the collection already exist
                    } else {
                        // User document does not exist, create a new one
                        userDocument.set(user)
                    }
                }.addOnFailureListener { e ->
                    // Handle failures, such as network errors or permission issues

                    Log.e("TAG", "Error checking user document existence", e)
                }
            }
        }



 }
    fun getUserById(uid:String): Task<DocumentSnapshot> {

        return usersCollection.document(uid).get()
    }

}
