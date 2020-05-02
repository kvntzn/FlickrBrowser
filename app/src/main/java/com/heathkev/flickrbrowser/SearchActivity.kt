package com.heathkev.flickrbrowser

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.TextView
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_search.*


class SearchActivity : BaseActivity() {
    private val TAG = "SearchActivity"
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, ".onCreate starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        activateToolbar(true)
        Log.d(TAG, ".onCreate ends")

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val recentSearchesResult = sharedPref.getStringSet(RECENT_SEARCHES, HashSet<String?>())

        list_view.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            recentSearchesResult?.toTypedArray()!!
        )
        list_view.setOnItemClickListener { _, view, _, _ ->
            val listViewText = view as TextView
            searchView?.setQuery(listViewText.text, true);
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, ".onCreateOptionsMenu: starts")
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        val searchableInfo = searchManager.getSearchableInfo(componentName)
        searchView?.setSearchableInfo(searchableInfo)

        searchView?.isIconified = false

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, ".onQueryTextSubmit: called")

                val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val recentSearchesResult = sharedPref.getStringSet(RECENT_SEARCHES, HashSet<String?>())
                recentSearchesResult?.add(query)

                sharedPref.edit().putString(FLICKR_QUERY, query).apply()
                sharedPref.edit().putStringSet(RECENT_SEARCHES, recentSearchesResult).apply()
                searchView?.clearFocus()

                finish()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        searchView?.setOnCloseListener() {
            finish()
            false
        }
        Log.d(TAG, ".onCreateOptionsMenu: returning")
        return true
    }
}
