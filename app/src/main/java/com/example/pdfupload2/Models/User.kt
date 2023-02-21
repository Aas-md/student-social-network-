package com.example.pdfupload2.Models

import com.google.firebase.firestore.Exclude

data class User(
        @get:Exclude var uId: String = "",
             var displayName:String = "",
                var imageUrl : String = "",
                var cource:String = "",
                var bio:String = "")
