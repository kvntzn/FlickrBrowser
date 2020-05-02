package com.heathkev.flickrbrowser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val TAG = "MainActivity"

class MainActivity : BaseActivity(), GetRawData.OnDownloadComplete
    , GetFlickrJsonData.OnDataAvailable, RecyclerItemClickListener.OnRecyclerClickListener,
    OnOffsetChangedListener {

    private val flickrRecyclerViewAdapter = FlickrRecyclerViewAdapter(ArrayList())
    private lateinit var mainMenu: Menu
    private var isShow = false
    private var scrollRange = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        app_bar.addOnOffsetChangedListener(this)

        activateToolbar(false)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addOnItemTouchListener(RecyclerItemClickListener(this, recycler_view, this))
        recycler_view.adapter = flickrRecyclerViewAdapter

        val url = createUri(
            "https://api.flickr.com/services/feeds/photos_public.gne",
            "horror",
            "en-us",
            true
        )
        val getRawData = GetRawData(this)
        getRawData.execute(url)
    }

    private fun changeOptionVisibility(id: Int, visible: Boolean) {
        val item: MenuItem = mainMenu.findItem(id)
        item.isVisible = visible
    }

    override fun onItemClick(view: View, position: Int) {
        Log.d(TAG, ".onItemClick: starts")
        val photo = flickrRecyclerViewAdapter.getPhoto(position)
        if (photo != null) {
            val intent = Intent(this, PhotoDetailsActivity::class.java)
            intent.putExtra(PHOTO_TRANSFER, photo)
            startActivity(intent)
        }
    }

    override fun onItemLongClick(view: View, position: Int) {
        Log.d(TAG, ".onItemLongClick: starts")
        Toast.makeText(this, ":Long tap at position $position", Toast.LENGTH_SHORT).show()
    }

    private fun createUri(
        baseUrl: String,
        searchCriteria: String,
        lang: String,
        matchAll: Boolean
    ): String {
        Log.d(TAG, ".createUri starts")

        return Uri.parse(baseUrl).buildUpon().appendQueryParameter("tags", searchCriteria)
            .appendQueryParameter("tagmode", if (matchAll) "ALL" else "ANY")
            .appendQueryParameter("lang", lang).appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1").build().toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mainMenu = menu
        menuInflater.inflate(R.menu.menu_main, menu)
        changeOptionVisibility(R.id.action_search, false)
        return true
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        if (scrollRange == -1) {
            scrollRange = appBarLayout.totalScrollRange
        }
        if (scrollRange + verticalOffset == 0) {
            isShow = true
            changeOptionVisibility(R.id.action_search, isShow)
        } else if (isShow) {
            isShow = false
            changeOptionVisibility(R.id.action_search, isShow)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDownloadComplete(data: String, status: DownloadStatus) {
        if (status == DownloadStatus.OK) {
            Log.d(TAG, "onDownloadComplete called")
            loader.visibility = View.VISIBLE
            recycler_view.visibility = View.GONE
            val getFlickrJsonData = GetFlickrJsonData(this)
            getFlickrJsonData.execute(data)

        } else {
            Log.d(TAG, "onDownloadCompleted failed with status $status, Error message is: $data")
        }
    }

    override fun onDataAvailable(data: List<Photo>) {
        flickrRecyclerViewAdapter.loadNewData(data);

        val toolbarPhoto = flickrRecyclerViewAdapter.getPhoto(0)
        Picasso.get().load(toolbarPhoto?.link)
            .error(R.drawable.placeholder)
            .into(expandedImage)

        loader.visibility = View.GONE
        recycler_view.visibility = View.VISIBLE
    }

    override fun onError(exception: Exception) {
        Log.e(TAG, "onError called with ${exception.message}")
    }

    override fun onResume() {
        Log.d(TAG, ".onResume starts")

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val queryResult = sharedPref.getString(FLICKR_QUERY, "")

        if (queryResult != null && queryResult.isNotEmpty()) {
            val url = createUri(
                "https://api.flickr.com/services/feeds/photos_public.gne",
                queryResult,
                "en-us",
                true
            )
            val getRawData = GetRawData(this)
            getRawData.execute(url)
        }
        super.onResume()
    }
}

