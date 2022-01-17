package com.complete.newsfresh.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.complete.newsfresh.database.ArticleDatabase
import com.complete.newsfresh.database.NewsRepository
import com.complete.newsfresh.databinding.ActivityNewsBinding
import kotlinx.android.synthetic.main.activity_news.*

class NewsActivity : AppCompatActivity() {
    private var _binding:ActivityNewsBinding? = null
    private val binding : ActivityNewsBinding get() = _binding!!

    lateinit var newsViewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository :NewsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelFactory = NewsViewModelProviderFactory(application,repository)
        newsViewModel = ViewModelProvider(this,viewModelFactory)[NewsViewModel::class.java]


        bottomNavigationView.setupWithNavController(newsNavHostFragment.findNavController())
    }
}