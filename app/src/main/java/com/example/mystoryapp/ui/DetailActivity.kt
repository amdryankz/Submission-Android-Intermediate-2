package com.example.mystoryapp.ui

import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityDetailBinding
import com.example.mystoryapp.retrofit.ListStoryPagingResponse
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story =
            intent.getParcelableExtra<ListStoryPagingResponse>(EXTRA_STORY) as ListStoryPagingResponse
        getDetailStory(story)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.detail_title, story.name)
    }

    private fun getDetailStory(story: ListStoryPagingResponse) {
        binding.apply {
            tvDetailName.text = story.name
            tvDetailDescription.text = story.description
        }
        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivDetailPhoto)
        if (story.lat != null && story.lon != null) {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(story.lat, story.lon, 1)
            val address = addresses!![0].getAddressLine(0)

            binding.tvLocation.text = address
        } else {
            binding.tvLocation.text = getString(R.string.addresss)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}