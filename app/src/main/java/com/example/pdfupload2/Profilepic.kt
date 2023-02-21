package com.example.pdfupload2

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.pdfupload2.Daos.UserDao
import com.example.pdfupload2.Models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile_pic.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const private val TAG = "Profilepic"
class Profilepic : AppCompatActivity() {
    private val REQUEST_CODE = 123
    private var mPhotoUri: Uri? = null
    private lateinit var mUserDao:UserDao
    private var prevUrl:String = ""
    private lateinit var mStorageRef : StorageReference
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_pic)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val auth = Firebase.auth
        val uid = auth.currentUser!!.uid
        db = FirebaseFirestore.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference
        mUserDao = UserDao()

        GlobalScope.launch {
          val  signinUser = mUserDao.getUserById(uid).await().toObject(User::class.java)!!
            withContext(Main){
                prevUrl = signinUser.imageUrl
                Log.i(TAG,"phone num = ${prevUrl}")
            }
        }





        pickImage()

        done_btn.setOnClickListener {

            changeProfilePick(uid)

        }

    }

    private fun deletePrevImage() {
        Toast.makeText(this,"Profile photo updated",Toast.LENGTH_SHORT).show()
        if(prevUrl.isEmpty()){

            finish()
            return


        }

            val firebaseStorage = FirebaseStorage.getInstance()
            val storageReference = firebaseStorage.getReferenceFromUrl(prevUrl)
            storageReference.delete().addOnSuccessListener {
                Log.i(TAG, "#deleted")
                finish()}
            .addOnFailureListener {

                Log.e(TAG,"exception during deletion")

                finish()

                }


    }

    private fun pickImage() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK) {

            if (requestCode == REQUEST_CODE) {


                mPhotoUri = data?.data

                if(mPhotoUri != null){

                    Glide.with(this).load(mPhotoUri).into(image_view)
                }

            }
        }
    }


    private fun changeProfilePick(uid: String) {

        if (mPhotoUri == null) {

            Toast.makeText(this, "Select an image for your profile", Toast.LENGTH_LONG).show()
            finish()
        }

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.progress_dialog)
        dialog.show()

        done_btn.isEnabled = false
        val photoRef = mStorageRef.child("profile Pic/ ${System.currentTimeMillis()}-photo.jpg")
        val uploadPhotoUri = mPhotoUri as Uri
        photoRef.putFile(uploadPhotoUri)
            .continueWithTask { photoUploadTask ->

                Log.i(TAG, "uploading ${photoUploadTask.result.bytesTransferred}")
                photoRef.downloadUrl
            }.continueWithTask { downloadUrlTask ->

                db.collection("users").document(uid).update("imageUrl", downloadUrlTask.result.toString())
            }.addOnCompleteListener { postCompleteTask ->



                if (postCompleteTask.isSuccessful == false) {

                    Log.e(TAG, "exception during post")
                    Toast.makeText(this, "check your internet connection..", Toast.LENGTH_LONG)
                        .show()
                    done_btn.isEnabled = true
                 dialog.dismiss()
                }else{


                dialog.dismiss()
                    finish()
                    deletePrevImage()

                }

            }
    }

}