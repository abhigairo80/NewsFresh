package com.complete.newsfresh.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.complete.newsfresh.R
import com.complete.newsfresh.adapter.NewsAdapter
import com.complete.newsfresh.utils.Constants.Companion.QUERY_SIZE
import com.complete.newsfresh.utils.Resources
import kotlinx.android.synthetic.main.fragment_breaking_news.*

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    lateinit var viewModel:NewsViewModel
    private lateinit var newsAdapter : NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as NewsActivity).newsViewModel
        setUpRecyclerView()

        newsAdapter.setOnClickListener {article->
            val bundle = Bundle().apply {
                putSerializable("article",article)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }
        Log.d("taget",findNavController().toString())

        viewModel.breakingNews.observe(viewLifecycleOwner, { response->
             when(response){
                 is Resources.Success ->{
                     hideProgressBar()
                     response.data?.let {newsResponse ->  
                         newsAdapter.differ.submitList(newsResponse.articles.toList())
                         val totalPages = newsResponse.totalResults/ QUERY_SIZE + 2
                         isLastPage = totalPages == viewModel.breakingNewsPage
                         if(isLastPage){
                             rvBreakingNews.setPadding(0,0,0,0)
                         }

                     }
                 }
                 is Resources.Error ->{
                     hideProgressBar()
                     response.data?.let{
                         Toast.makeText(activity,"An Error occured $it",Toast.LENGTH_SHORT).show()
                     }
                 }
                 is Resources.Loading ->{
                     showProgressBar()
                 }
             }
        })

    }
    private fun hideProgressBar(){
        paginationProgressBarBreaking.visibility = View.INVISIBLE
        isLoading = false
    }
    private fun showProgressBar(){
        paginationProgressBarBreaking.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true

            }
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotOnLastPage = !isLoading && !isLastPage
            val isAtLastItem = (firstVisibleItemPosition == totalItemCount-visibleItemCount)
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_SIZE
            val shouldPaginate = isNotLoadingAndNotOnLastPage &&
                    isNotAtBeginning && isTotalMoreThanVisible && !isAtLastItem && isScrolling
            if(shouldPaginate){
                viewModel.getBreakingNews("in")
                isScrolling = false
            }
            super.onScrolled(recyclerView, dx, dy)
        }
    }
    private fun setUpRecyclerView(){
        newsAdapter = NewsAdapter()
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)

        }

    }
}