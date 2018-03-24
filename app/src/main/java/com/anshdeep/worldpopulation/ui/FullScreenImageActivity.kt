package com.anshdeep.worldpopulation.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.anshdeep.worldpopulation.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_full_screen_image.*

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* create a full screen window */
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()

        setContentView(R.layout.activity_full_screen_image)


        val intent = intent
        if (intent.hasExtra("IMAGE_URL")) {
            val imageUrl = intent.getStringExtra("IMAGE_URL")


            Glide.with(this)
                    .load(imageUrl)
                    .into(country_full_image)
        }
    }
}
