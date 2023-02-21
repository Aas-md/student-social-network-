package com.example.pdfupload2


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pdfupload2.Daos.UserDao
import com.example.pdfupload2.Models.Post
import com.example.pdfupload2.Models.User
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception


const val post_type_text = 0
const val post_type_pdf = 1
const val post_type_image = 2
private const val TAG = "PostAdapter"
class PostAdapter(val context:Context,options: FirestoreRecyclerOptions<Post>, private val listener:OnItemClicked):FirestoreRecyclerAdapter<Post,RecyclerView.ViewHolder>(
    options
) {

    @SuppressLint("DiscouragedPrivateApi")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun Deletepost(view: View, holder:RecyclerView.ViewHolder) {

        val popupMenus = PopupMenu(context,view)
        popupMenus.inflate(R.menu.menu_delete_post)
        popupMenus.setOnMenuItemClickListener {


            when(it.itemId){

                R.id.item_delete_post->{
                    listener.onDeleteClicked(snapshots.getSnapshot(holder.adapterPosition).id)
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
    }catch (e:Exception){

        Log.e(TAG,"exception is $e")
    }finally {
        popupMenus.show()
    }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        //for text
        if(viewType == post_type_text){

            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_text,parent,false )
            val holder = TextViewHolder(context ,view)

            holder.mMenus.setOnClickListener { Deletepost(it,holder) }

            holder.likeButton.setOnClickListener{

                listener.onLikeClicked(snapshots.getSnapshot(holder.adapterPosition).id)
            }

            return holder

        }
        //for pdf
        else if(viewType == post_type_pdf) {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pdf, parent, false)
            val holder = PdfViewHolder(view)

            holder.mMenus.setOnClickListener { Deletepost(it,holder) }

            holder.pdfLikeButton.setOnClickListener {

                listener.onLikeClicked(snapshots.getSnapshot(holder.adapterPosition).id)
            }
            holder.pdfDownLoad.setOnClickListener {

                listener.onDownloadClicked(snapshots.getSnapshot(holder.adapterPosition).id)
            }

            return  holder
        }

//        for image
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image,parent,false)
        val holder = ImageViewHolder(view)

        holder.mMenus.setOnClickListener { Deletepost(it,holder) }


        holder.imageLikeButton.setOnClickListener {

            listener.onLikeClicked(snapshots.getSnapshot(holder.adapterPosition).id)
        }

        return holder

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Post) {

//        holder.pdfName.text = model.pdfName
        if(getItemViewType(position) == post_type_text){

            val holder = holder as TextViewHolder
          holder.bind(model,listener)

        }else if(getItemViewType(position) == post_type_pdf){

            val holder  = holder as PdfViewHolder
            holder.bind(model,listener)

        }else if(getItemViewType(position) == post_type_image){
            val holder = holder as ImageViewHolder
            holder.bind(model,listener)
        }


    }

    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
    if(getItem(position).post_type == 0L){
//        Log.e(TAG,"item post is ${getItem(position).post_title}")
        return post_type_text
    }else if(getItem(position).post_type == 1L)
        return post_type_pdf
        else if(getItem(position).post_type == 2L)
            return post_type_image

        return 100
    }

}


class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    val pdfName: TextView = itemView.findViewById(R.id.pdfName)
    val pdfDownLoad:ImageView = itemView.findViewById(R.id.post_pdf_download)
    val pdfUserImage:ImageView = itemView.findViewById(R.id.userImage)
    val pdfUserName :TextView = itemView.findViewById(R.id.userName)
    val pdfCreatedAt:TextView = itemView.findViewById(R.id.createdAt)
    val pdfPostTitle:TextView = itemView.findViewById(R.id.postTitle)
    val pdfLikeButton:ImageView = itemView.findViewById(R.id.likeButton)
    val pdfLikeCount:TextView = itemView.findViewById(R.id.likeCount)
    val mMenus:ImageView = itemView.findViewById(R.id.popup_menu)
    fun bind(model: Post,listener: OnItemClicked){

        GlobalScope.launch {

            val userDao = UserDao()
            val user = userDao.getUserById(model.uid).await().toObject(User::class.java)!!
            withContext(Main) {
                pdfUserName.text =user.displayName
                Glide.with(pdfUserImage.context).load(user.imageUrl).circleCrop().into(pdfUserImage)
            }
        }


        pdfPostTitle.text = model.post_title

        pdfLikeCount.text = model.likedBy.size.toString()
        pdfCreatedAt.text = Utils.getTimeAgo(model.createdAt)
        pdfName.text = model.pdfName
        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid
        val isLike = model.likedBy.contains(currentUserId)

        if(isLike){

            pdfLikeButton.setImageDrawable(ContextCompat.getDrawable(pdfLikeButton.context,R.drawable.ic_like))
        }else{
            pdfLikeButton.setImageDrawable(ContextCompat.getDrawable(pdfLikeButton.context,R.drawable.ic_unlike))
        }

        val uid = Firebase.auth.currentUser!!.uid

        if(model.uid == uid){

            mMenus.visibility = View.VISIBLE

        }else{

            mMenus.visibility =View.GONE
        }

        pdfUserName.setOnClickListener {

            listener.onProfileClicked(model.uid)
        }


        pdfUserImage.setOnClickListener {

            listener.onProfileClicked(model.uid)
        }

    }

}

class TextViewHolder(val context:Context,itemView: View):RecyclerView.ViewHolder(itemView){

    private val userImage:ImageView = itemView.findViewById(R.id.userImage)
     val userName :TextView = itemView.findViewById(R.id.userName)
    private val createdAt:TextView = itemView.findViewById(R.id.createdAt)
    private val postTitle:TextView = itemView.findViewById(R.id.postTitle)
    val likeButton:ImageView = itemView.findViewById(R.id.likeButton)
    private val likeCount:TextView = itemView.findViewById(R.id.likeCount)
    val mMenus :ImageView = itemView.findViewById(R.id.popup_menu)

    fun bind(model:Post,listener: OnItemClicked) {

        GlobalScope.launch {

            val userDao = UserDao()
            val user = userDao.getUserById(model.uid).await().toObject(User::class.java)!!
            withContext(Main) {
                userName.text = user.displayName
                Glide.with(userImage.context).load(user.imageUrl).circleCrop().into(userImage)

            }
        }


        postTitle.text = model.post_title

        likeCount.text = model.likedBy.size.toString()
        createdAt.text = Utils.getTimeAgo(model.createdAt)

        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid
        val isLike = model.likedBy.contains(currentUserId)
//            val isLike = false
        if (isLike) {

            likeButton.setImageDrawable(
                ContextCompat.getDrawable(
                    likeButton.context,
                    R.drawable.ic_like
                )
            )
        } else {
            likeButton.setImageDrawable(ContextCompat.getDrawable(likeButton.context,R.drawable.ic_unlike))
        }

        val uid = Firebase.auth.currentUser!!.uid

        if (model.uid == uid) {

            mMenus.visibility = View.VISIBLE

        } else {

           mMenus.visibility = View.GONE
        }
       userName.setOnClickListener {

            listener.onProfileClicked(model.uid)
        }

        userImage.setOnClickListener {

            listener.onProfileClicked(model.uid)
        }

    }

}

class ImageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    private val imageUserImage:ImageView = itemView.findViewById(R.id.userImage)
            val imageUserName :TextView = itemView.findViewById(R.id.userName)
    private val imageCreatedAt:TextView = itemView.findViewById(R.id.createdAt)
    private val imagePostTitle:TextView = itemView.findViewById(R.id.postTitle)
    val imageLikeButton:ImageView = itemView.findViewById(R.id.likeButton)
    private val imageLikeCount:TextView = itemView.findViewById(R.id.likeCount)
     private val imagePost:ImageView = itemView.findViewById(R.id.post_image)
    val mMenus:ImageView = itemView.findViewById(R.id.popup_menu)

    fun bind(model : Post,listener: OnItemClicked){


        GlobalScope.launch {

            val userDao = UserDao()
            val user = userDao.getUserById(model.uid).await().toObject(User::class.java)!!
            withContext(Main) {
                imageUserName.text = user.displayName
                Glide.with(imageUserImage.context).load(user.imageUrl).circleCrop().into(imageUserImage)

            }
        }


        imagePostTitle.text = model.post_title

        imageLikeCount.text = model.likedBy.size.toString()
        imageCreatedAt.text = Utils.getTimeAgo(model.createdAt)
        Glide.with(imagePost.context).load(model.imaegUri).into(imagePost)
        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid


        val isLike = model.likedBy.contains(currentUserId)

        if(isLike){

            imageLikeButton.setImageDrawable(ContextCompat.getDrawable(imageLikeButton.context,R.drawable.ic_like))
        }else{
            imageLikeButton.setImageDrawable(ContextCompat.getDrawable(imageLikeButton.context,R.drawable.ic_unlike))
        }

        imagePost.setOnClickListener {

        listener.onImageItemCliced(model.imaegUri)
        }

        val uid = Firebase.auth.currentUser!!.uid

        if(model.uid == uid){

            mMenus.visibility = View.VISIBLE

        }else{

            mMenus.visibility =View.GONE
        }

        imageUserName.setOnClickListener {

            listener.onProfileClicked(model.uid)
        }

        imageUserImage.setOnClickListener {

            listener.onProfileClicked(model.uid)
        }

    }
}



interface OnItemClicked {

    fun onDeleteClicked(postId: String)
    fun onDownloadClicked(postId: String)
    fun onLikeClicked(postId: String)
    fun onImageItemCliced(uri: String)
    fun onProfileClicked(uid: String)

}