package com.example.mystoryapp.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mystoryapp.AppPreferences
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityStoryBinding
import com.example.mystoryapp.databinding.ItemListStoryBinding
import com.example.mystoryapp.retrofit.ListStoryPagingResponse

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryBinding
    private lateinit var binding1: ItemListStoryBinding
    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding1 = ItemListStoryBinding.inflate(layoutInflater)

        val layoutManager = LinearLayoutManager(this)
        binding.rvStory.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvStory.addItemDecoration(itemDecoration)

        storyViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        val appPreferences = AppPreferences(this)
        val token = appPreferences.authToken

        if (token != null) {
            getStoryUser(token)
        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout_menu -> {
                val appPreferences = AppPreferences(this)
                appPreferences.isLoggedIn = false
                appPreferences.authToken = null
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.languange_menu -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.maps_menu -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun getStoryUser(token: String) {
        val listUserAdapter = ListStoryAdapter(this)
        binding.rvStory.adapter = listUserAdapter

        storyViewModel.getStory(token).observe(this) {
            listUserAdapter.submitData(lifecycle, it)
        }

        listUserAdapter.setOnItemClickCallback(object : ListStoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ListStoryPagingResponse) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@StoryActivity,
                    androidx.core.util.Pair.create(
                        binding1.tvItemName,
                        ViewCompat.getTransitionName(binding1.tvItemName)
                    ),
                    androidx.core.util.Pair.create(
                        binding1.image,
                        ViewCompat.getTransitionName(binding1.image)
                    )
                )
                val intent = Intent(this@StoryActivity, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_STORY, data)
                startActivity(intent, options.toBundle())
            }
        })
    }
}