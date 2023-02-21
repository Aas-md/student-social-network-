package com.example.pdfupload2.Daos

import android.util.Log
import com.example.pdfupload2.Models.Post
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import com.google.firebase.storage.FirebaseStorage




class PostDao {

    private val dp = FirebaseFirestore.getInstance()
    private val postCollection = dp.collection("posts")
    lateinit var post : Post
    val auth = Firebase.auth


    fun getPostById(postId: String): Task<DocumentSnapshot> {
        return postCollection.document(postId).get()

    }

    fun updateLikes(postId : String){

        GlobalScope.launch {

            val post = getPostById(postId).await().toObject(Post::class.java)!!
            val currentUserId = auth.currentUser!!.uid
            val isLike = post.likedBy.contains(currentUserId)

            if(isLike){

                post.likedBy.remove(currentUserId)
            }else{

                post.likedBy.add(currentUserId)
            }
            postCollection.document(postId).set(post)
        }
    }

    fun deletePost(postId : String){

       GlobalScope.launch {


           val post = getPostById(postId).await().toObject(Post::class.java)!!

               val query = postCollection.whereEqualTo(
                   "createdAt", post.createdAt
               ).get().await()

               if (post.pdfUri != "") {

                   val firebaseStorage = FirebaseStorage.getInstance()
                   val storageReference = firebaseStorage.getReferenceFromUrl(post.pdfUri)
                   storageReference.delete()
                       .addOnSuccessListener { Log.i("PostDao", "#pdf deleted") }
               }

               if (post.imaegUri.toString() != "") {

                   val firebaseStorage = FirebaseStorage.getInstance()
                   val storageReference = firebaseStorage.getReferenceFromUrl(post.imaegUri)
                   storageReference.delete().addOnSuccessListener { Log.i("PostDao", "#deleted") }
               }

               if (!query.documents.isEmpty()) {

                   for (document in query) {

                       postCollection.document(document.id).delete().await()
                   }
               }
           }

    }

}

