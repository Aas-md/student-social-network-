package com.example.pdfupload2

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.pdfupload2.Models.Post
import com.example.pdfupload2.Models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_image.*
import kotlinx.android.synthetic.main.activity_create_pdf.*

import java.io.File

//import java.io.File


class CreatePdfActivity : AppCompatActivity() {
    private var currentUserId:String = ""
    private var currentUser:User= User()
    val TAG: String = "CreatePostActivity"
    lateinit var db: FirebaseFirestore
    lateinit var storageRef: StorageReference
    private var pdfUri: Uri? = null
    private var pdfName: String = ""
    private var title: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pdf)

        //page title
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

            pickPdf()


        post_btn.setOnClickListener {

            uploadPdf()
        }
    }

    private fun uploadPdf() {
        title = pdf_title.text.toString()
        if (title == "") {

            Toast.makeText(this, "please write something about your pdf", Toast.LENGTH_LONG).show()
            return
        }

        if (pdfUri == null) {

            Toast.makeText(this, "Please select a pdf", Toast.LENGTH_LONG).show()
            return
        }

        post_btn.isEnabled = false
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.progress_dialog)
        dialog.show()

        val pdfRef = storageRef.child("pdf/ $pdfName .${System.currentTimeMillis()}-.pdf")
        val uploadPdfUri = pdfUri as Uri //Type Casting
        pdfRef.putFile(uploadPdfUri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "uploading ${photoUploadTask.result.bytesTransferred}")
                pdfRef.downloadUrl

            }.continueWithTask { downloadUrlTask ->


                    val post = Post(
                        currentUserId,
                        title,
                        currentUser,
                        "",
                        downloadUrlTask.result.toString(),
                        pdfName,
                        System.currentTimeMillis(),
                        1


                    )
            Log.i(TAG,"current user = $currentUser")

                    db.collection("posts").add(post)

            }.addOnCompleteListener { postCompleteTask ->



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
                            Toast.makeText(this, "successfully posted", Toast.LENGTH_LONG).show()
//                     imageView.setImageResource(0)
//                            val intent = Intent(this, MainActivity::class.java)
//                            startActivity(intent)
                            finish()

                        }
                }


    private fun pickPdf() {

        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "application/pdf"
        startActivityForResult(intent,123)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 123) {
                // Get the url of the image from data
                pdfUri = data?.data

                if (null != pdfUri) {
                    // update the preview image in the layout

                    if (pdfUri.toString().startsWith("content://")) {

                        val cursor = contentResolver.query(pdfUri!!, null, null, null, null)

                        if(cursor != null && cursor.moveToFirst()){

                            pdfName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        }


                    } else if (pdfUri.toString().startsWith("file://"))
                        pdfName = File(pdfUri.toString()).name

                    }
                    pdf_name.text = pdfName
                }

            }else{
            finish()
        }
        }

    }
