package com.example.mystoryapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityDetailBinding
import com.example.mystoryapp.retrofit.ListStoryResponse

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story = intent.getParcelableExtra<ListStoryResponse>(EXTRA_STORY) as ListStoryResponse
        getDetailStory(story)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.detail_title, story.name)
    }

    private fun getDetailStory(story: ListStoryResponse) {
        binding.apply {
            tvDetailName.text = story.name
            tvDetailDescription.text = story.description
        }
        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivDetailPhoto)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }
}