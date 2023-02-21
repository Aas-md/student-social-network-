package com.example.pdfupload2


import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pdfupload2.Models.Post
import com.example.pdfupload2.Models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_text.*

class CreateTextActivity : AppCompatActivity() {
    private val TAG = "CreateTextActivity"
    private var currentUserId:String = ""
    private lateinit var db:FirebaseFirestore
    private var currentUser:User = User()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_text)
        //page title
        supportActionBar?.title = "Post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()

        val auth = Firebase.auth
         currentUserId = auth.currentUser!!.uid
        val userCollection = db.collection("users")

        userCollection.document(currentUserId).get()
            .addOnSuccessListener {userSnapShot->

                currentUser = userSnapShot.toObject(User::class.java)!!

            }



        post_btn.setOnClickListener {

            uploadPost()
        }
    }

    private fun uploadPost() {

        val title = post_title.text.toString()
        if(title == ""){

            Toast.makeText(this,"write something",Toast.LENGTH_LONG).show()
            return
        }
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.progress_dialog)
        dialog.show()
        post_btn.isEnabled = false
        val post = Post(
            currentUserId,
            title,
            currentUser,
            "",
            "",
            "",
            System.currentTimeMillis(),
            0
        )

        db.collection("posts").add(post)
            .addOnCompleteListener{postCompleteTask->



                if (postCompleteTask.isSuccessful == false) {

                    Log.e(TAG, "exception during post ${postCompleteTask.exception}")
                    Toast.makeText(
                        this,
                        "something went wrong in uploading post",
                        Toast.LENGTH_LONG
                    ).show()
                }
                post_btn.isEnabled = true
            dialog.dismiss()
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
                finish()
            }
    }
}