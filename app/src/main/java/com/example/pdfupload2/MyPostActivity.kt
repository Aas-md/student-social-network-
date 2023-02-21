package com.example.pdfupload2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.pdfupload2.Daos.UserDao
import com.example.pdfupload2.Models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MyPostActivity : MainActivity() {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        return true
    }


    override fun onStart() {
        super.onStart()
        supportActionBar?.title = "user Posts"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fab.visibility = View.GONE
    }


}