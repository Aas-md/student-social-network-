package com.example.pdfupload2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch



//import android.R
import android.app.Dialog
import android.content.DialogInterface
//import android.icu.number.NumberRangeFormatter.with

import android.view.Menu
import android.view.MenuItem
import android.view.View

import android.widget.ImageView
import android.widget.PopupMenu
//import com.bumptech.glide.GenericTransitionOptions.with
import com.example.pdfupload2.Daos.PostDao
import com.example.pdfupload2.Models.Post
import com.example.pdfupload2.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.nio.file.attribute.AclEntry


open class MainActivity : AppCompatActivity(), OnItemClicked {

    companion object{

        const val EXTRA_USERID = "EXTRA_USERNAME"
    }

    private var mCurrentUser:User = User()
    private var mCurrentUserId:String =""
    private val TAG: String = "MainActivity"
    private var mPostdao :PostDao = PostDao()
    private lateinit var mAdapter: PostAdapter
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

     val db = FirebaseFirestore.getInstance()

         auth = Firebase.auth
         mCurrentUserId = auth.currentUser!!.uid
        val userCollection = db.collection("users")

        userCollection.document(mCurrentUserId).get()
            .addOnSuccessListener {userSnapShot->

                mCurrentUser = userSnapShot.toObject(User::class.java)!!

            }


          mPostdao = PostDao()
        val fab  :ImageView= findViewById(R.id.fab)
        fab.setOnClickListener {

            showDialog(it)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        setupRecyclerView()
    }


    private fun setupRecyclerView() {
        val db = FirebaseFirestore.getInstance()
        var query = db.collection("posts").orderBy("createdAt", Query.Direction.DESCENDING)
        val userId = intent.getStringExtra(EXTRA_USERID)

        if(userId != null){

                      query = db.collection("posts").whereEqualTo(
            "uid",userId
        ).orderBy("createdAt",Query.Direction.DESCENDING)
        }

        val recyclerViewOptions =
            FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
        mAdapter = PostAdapter(this,recyclerViewOptions, this)
        recyclerView.adapter = mAdapter
    }



    override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }

//    override fun onStop() {
//        super.onStop()
//        mAdapter.stopListening()
//    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


            when(item.itemId){

                R.id.item_my_post->{
                    val intent = Intent(this,MyPostActivity::class.java)
                    intent.putExtra(EXTRA_USERID,mCurrentUserId)
                    startActivity(intent)

                }
            R.id.item_profile->{

                val intent = Intent(this,ProfileActivity::class.java)
                intent.putExtra(ProfileActivity.EXTRA_UID,mCurrentUserId)
                startActivity(intent)

            }
                R.id.logout->{

                   logoutFun()
                }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun logoutFun() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure")
        builder.setMessage("Do you want to Logout")
        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int -> }
        builder.show()

    }

    override fun onDeleteClicked(postId: String) {

        mPostdao.deletePost(postId)
    }

    override fun onDownloadClicked(postId: String) {

        GlobalScope.launch {

            val post = mPostdao.getPostById(postId).await().toObject(Post::class.java)!!
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(post.pdfUri))

            val target = Intent.createChooser(intent, "Open File")

            startActivity(target)
            Log.i(TAG, "exception during post ${post.pdfName}")
        }
        Toast.makeText(this, "item clicked", Toast.LENGTH_LONG).show()

    }

    override fun onLikeClicked(postId: String) {

        mPostdao.updateLikes(postId)
    }

    override fun onImageItemCliced(uri:String) {

            val intent = Intent(this,FullImageActivity::class.java)
        intent.putExtra(FullImageActivity.uri_extra,uri)
        startActivity(intent)

    }

    override fun onProfileClicked(UserId: String) {

        val intent = Intent(this,ProfileActivity::class.java)
        intent.putExtra(ProfileActivity.EXTRA_UID,UserId)
        startActivity(intent)

    }



    @SuppressLint("DiscouragedPrivateApi")
    private fun showDialog(view:View) {

        val popupMenus = PopupMenu(this,view)
        popupMenus.inflate(R.menu.menu_item_select)
        popupMenus.setOnMenuItemClickListener {


            when(it.itemId){

                R.id.item_image->{
                   val intent = Intent(this,CreateImageActivity::class.java)
            startActivity(intent)

                    true
                }
                R.id.item_pdf->{
                    val intent = Intent(this,CreatePdfActivity::class.java)
                startActivity(intent)

                    true
                }
                R.id.item_text->{

                    val intent = Intent(this,CreateTextActivity::class.java)
                     startActivity(intent)

                    true
                }
                else->{
                    true
                }

            }

        }


        try {
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)

            menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
        }catch (e: Exception){

            Log.e(TAG,"exception is $e")
        }finally {
            popupMenus.show()
        }
    }



}