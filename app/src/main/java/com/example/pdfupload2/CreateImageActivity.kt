package com.example.pdfupload2


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.pdfupload2.Models.Post
import com.example.pdfupload2.Models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_image.*

private val TAG:String = "CreateImageActivity"
class CreateImageActivity : AppCompatActivity() {
    private var  currentUserId:String = ""
    private lateinit var storageRef : StorageReference
    private lateinit var db:FirebaseFirestore
    private var currentUser: User= User()
    private var photoUri: Uri? = null
    private val REQUEST_CODE: Int = 123

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_image)
        supportActionBar?.title = "Post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        storageRef = FirebaseStorage.getInstance().reference
        db = FirebaseFirestore.getInstance()

        val auth = Firebase.auth
         currentUserId = auth.currentUser!!.uid
        val usersCollection = db.collection("users")


        usersCollection.document(currentUserId).get()
            .addOnSuccessListener {userSnapshot->
                currentUser = userSnapshot.toObject(User::class.java)!!
            }


            //call first
            selectImage()


        upload_post_btn.setOnClickListener {

            uploadPost()
        }

    }

    private fun uploadPost() {


        val title = image_title.text.toString()
        if(title == ""){

            Toast.makeText(this,"pls write description",Toast.LENGTH_LONG).show()
            return
        }
        if(photoUri == null){
            Toast.makeText(this,"pls select an image",Toast.LENGTH_LONG).show()
            return

        }


        val dialog = Dialog(this)
        dialog.setContentView(R.layout.progress_dialog)

//        if(dialog.window != null)
//            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        dialog.show()

    upload_post_btn.isEnabled = false

        val photoRef = storageRef.child("image/ ${System.currentTimeMillis()}-photo.jpg")
        val uploadPhotoUri = photoUri as Uri
        photoRef.putFile(uploadPhotoUri)
            .continueWithTask{photoUploadTask->

                Log.i(TAG,"uploading ${photoUploadTask.result.bytesTransferred}")
                photoRef.downloadUrl
            }.continueWithTask {downloadUrlTask->

            val post = Post(
                currentUserId,
                title,
                currentUser,
                downloadUrlTask.result.toString(),
                "",
                "",
                System.currentTimeMillis(),
                2,

            )
                db.collection("posts").add(post)

            }.addOnCompleteListener{postCompleteTask->

                if(postCompleteTask.isSuccessful == false){

                    Log.e(TAG,"exception during post")
                    Toast.makeText(this,"something went wrong in uploading..",Toast.LENGTH_LONG).show()
                }

                upload_post_btn.isEnabled = true
                dialog.dismiss()

                selected_img.setImageResource(0)
//                val intent = Intent(this,MainActivity::class.java)
//                startActivity(intent)
                finish()
            }

    }

    private fun selectImage() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK) {

            if (requestCode == REQUEST_CODE) {


                photoUri = data?.data
                if (photoUri != null) {

                    selected_img.setImageURI(photoUri)
                }
            }
        }else{
            finish()
        }
    }
}