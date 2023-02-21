package com.example.pdfupload2.Models

data class Post(
    val uid:String = "",
    val post_title:String = "",
    var createdBy :User = User(),
    val imaegUri:String = "",
    val pdfUri:String = "",
    val pdfName:String = "",
    val createdAt:Long = 0,
    val post_type:Long =0,
    val likedBy :ArrayList<String> = ArrayList()

)
