package com.heathkev.flickrbrowser

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

internal const val FLICKR_QUERY = "FLICKR_QUERY"
internal const val PHOTO_TRANSFER = "PHOTO_TRANSFER"
internal const val RECENT_SEARCHES = "RECENT_SEARCHES"

@SuppressLint("Registered")
open class BaseActivity: AppCompatActivity() {
    private val TAG = "BaseActivity"

    internal fun activateToolbar(enableHome: Boolean){
        Log.d(TAG,".activateToolbar")

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayShowTitleEnabled(enableHome)
        supportActionBar?.setDisplayHomeAsUpEnabled(enableHome)
    }
}