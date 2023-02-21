package com.example.pdfupload2

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.pdfupload2.Daos.PostDao
import com.example.pdfupload2.Daos.UserDao
import com.example.pdfupload2.Models.Post
import com.example.pdfupload2.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG:String = "ProfileActivity"
class ProfileActivity : AppCompatActivity() {
    companion object{

        const val EXTRA_UID = "Extra_uid"
    }
    private lateinit var db:FirebaseFirestore
    private val mUserDao  = UserDao()
    private var mDpUri:String = ""
    private lateinit var  signInUserId:String
    private  lateinit var auth:FirebaseAuth
    private var mUserId:String? = null
    private var mUser:User = User()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        profile_image.setOnClickListener {

            if(mDpUri != null) {
                val intent = Intent(this, FullImageActivity::class.java)
                intent.putExtra(FullImageActivity.uri_extra, mDpUri)
                startActivity(intent)
            }
        }

        select_dp.setOnClickListener {
            val intent = Intent(this,Profilepic::class.java)
            startActivity(intent)

        }


         mUserId  = intent.getStringExtra(EXTRA_UID)

        progress_bar.visibility = View.VISIBLE



   auth = Firebase.auth

        db = FirebaseFirestore.getInstance()

        val signInUserId = auth.currentUser!!.uid

        mUserId?.let {


            GlobalScope.launch {



//                 mPost = mPostDao.getPostById(mUserId!!).await().toObject(Post::class.java)!!
                mUser=mUserDao.getUserById(mUserId!!).await().toObject(User::class.java)!!

                withContext(Main){

                progress_bar.visibility = View.GONE
                    //action bar title
                    supportActionBar?.title = mUser.displayName
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                   upDateUi(mUser)

                    if(signInUserId== mUserId){
                        select_dp.visibility = View.VISIBLE

                        mDpUri = mUser.imageUrl
                       edit_profile_btn.visibility = View.VISIBLE

                        edit_profile_btn.setOnClickListener {

                            showDialogg(mUserId!!)

                        }
                    }

                }

            }

        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = MenuInflater(this)

       menuInflater.inflate(R.menu.menu_profile,menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.posts_profile->{

                    val intent = Intent(this,MyPostActivity::class.java)
                    intent.putExtra(MainActivity.EXTRA_USERID,mUserId)
                    startActivity(intent)

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
       i_progress_bar.visibility = View.VISIBLE

        GlobalScope.launch {


            val user =mUserDao.getUserById(mUserId!!).await().toObject(User::class.java)!!

            withContext(Main){

                upDateUi(user)
                mDpUri = user.imageUrl
            }
        }


    }



    private fun showDialogg(uid : String) {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.profile_edit)
         val nameEdit: EditText = dialog.findViewById(R.id.edit_name)
          val bioEdit: EditText = dialog.findViewById(R.id.edit_about)
         val courceEdit : EditText = dialog.findViewById(R.id.edit_cource)

        //auto fill information
        GlobalScope.launch {

            val user = mUserDao.getUserById(uid).await().toObject(User::class.java)!!
            withContext(Main){

                nameEdit.setText(user.displayName)
                bioEdit.setText(user.bio)
                courceEdit.setText(user.cource)

            }
        }


        val saveBtn:Button = dialog.findViewById(R.id.save_btn)

        saveBtn.setOnClickListener {

            val name = nameEdit.editableText.toString()
            val cource = courceEdit.editableText.toString()
            val bio = bioEdit.editableText.toString()

            if(name.isEmpty()){

                Toast.makeText(this,"pleas write your name",Toast.LENGTH_LONG).show()

            }else{

                GlobalScope.launch {
                val db = FirebaseFirestore.getInstance()
                    val map = mapOf("displayName" to name, "cource" to cource,"bio" to bio)

                db.collection("users").document(uid).update(map)
               dialog.dismiss()


                    val user = mUserDao.getUserById(uid).await().toObject(User::class.java)!!

                    withContext(Main){

                        upDateUi(user)
                    }

                }


            }
        }

        dialog.show()


    }

    private fun upDateUi(user:User) {


//
        Log.e(TAG,"update Ui = $user")
        profile_name.text = user.displayName
        profile_about.text = user.bio
        profile_cource.text = user.cource


        Glide.with(this).load(user.imageUrl)
            .listener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG,"exception during i,age loadingg")
                    i_progress_bar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                   i_progress_bar.visibility = View.GONE
                    return false
                }
            }) .circleCrop().into(profile_image)

    }
//
}



//db.collection("users").document(currentUid).update("displayName","buglu")
//
//               val query = db.collection("posts").whereEqualTo(
//                    "uid",currentUid)