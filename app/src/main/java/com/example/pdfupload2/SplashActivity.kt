package com.example.pdfupload2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
//        requestWindowFeature()
//        this.getSupportActionBar()!!.hide()

        setContentView(R.layout.activity_splash)


        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)

    }
}