package com.complete.newsfresh.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.complete.newsfresh.database.ArticleDatabase

import com.complete.newsfresh.database.NewsRepository
import com.complete.newsfresh.databinding.ActivityNewsBinding
import kotlinx.android.synthetic.main.activity_news.*

class NewsActivity : AppCompatActivity() {
    private var _binding:ActivityNewsBinding? = null
    val binding : ActivityNewsBinding get() = _binding!!

    lateinit var newsViewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository :NewsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelFactory = NewsViewModelProviderFactory(application,repository)
        newsViewModel = ViewModelProvider(this,viewModelFactory).get(NewsViewModel::class.java)


        bottomNavigationView.setupWithNavController(newsNavHostFragment.findNavController())
    }
}