package com.example.pdfupload2.Daos

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


        val metadata = auth.currentUser!!.metadata
        if (metadata!!.creationTimestamp == metadata!!.lastSignInTimestamp) {
            // The user is new, show them a fancy intro screen!

            if(user != null)
            {

                GlobalScope.launch(Dispatchers.IO) {

                    usersCollection.document(user.uId).set(user)
                }
            }

        }



 }
    fun getUserById(uid:String): Task<DocumentSnapshot> {

        return usersCollection.document(uid).get()
    }

}
