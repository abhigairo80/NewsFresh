package com.complete.newsfresh.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.complete.newsfresh.NewsApplication
import com.complete.newsfresh.database.NewsRepository
import com.complete.newsfresh.model.Article
import com.complete.newsfresh.model.NewsResponse
import com.complete.newsfresh.utils.Resources
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel (app: Application, private val repository: NewsRepository):AndroidViewModel(app){
    val breakingNews:MutableLiveData<Resources<NewsResponse>> = MutableLiveData()
    val searchedQuery:MutableLiveData<Resources<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var searchedPageNumber = 1
    private var breakingNewsResponse: NewsResponse? =null
    private var searchNewsResponse: NewsResponse? =null
    init{
        getBreakingNews("in")
    }

    fun getBreakingNews(countryCode:String) = viewModelScope.launch {
        breakingNews.postValue(Resources.Loading())
        safeBreakingCall(countryCode)
    }
    fun getSearchedNews(searchQuery:String) = viewModelScope.launch {
        searchedQuery.postValue(Resources.Loading())
        safeSearchCall(searchQuery)
    }
    fun getSavedNews() = repository.getAll()
    fun saveNews(article: Article) = viewModelScope.launch {
        repository.upsert(article)
    }
    fun deleteNews(article:Article) = viewModelScope.launch {
        repository.delete(article)
    }

    private fun handleBreakingNewsResponse(response:Response<NewsResponse>):Resources<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let{ resultResponse->
                breakingNewsPage++
                if(breakingNewsResponse == null){
                    breakingNewsResponse = resultResponse
                }else{
                    val oldList = breakingNewsResponse!!.articles
                    val newList = resultResponse.articles
                    oldList.addAll(newList)
                }

                // breakingNewsResponse ?: resultResponse = this indicates that if
                // breakingNewsRespponse is not null then it will be returned and
                // if its null then only resultResponse is returned
                return Resources.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resources.Error(response.message())
    }
    private fun handleSearchedNewsResponse(response:Response<NewsResponse>):Resources<NewsResponse>{
        if(response.isSuccessful){
            response.body()?.let{ resultResponse->
                searchedPageNumber++
                if(searchNewsResponse == null){
                    searchNewsResponse = resultResponse
                }else{
                    val oldList = searchNewsResponse!!.articles
                    val newList = resultResponse.articles
                    oldList.addAll(newList)
                }

                return Resources.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resources.Error(response.message())
    }
    private fun hasConnection():Boolean{
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        )as ConnectivityManager
        if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val cababilities = connectivityManager.getNetworkCapabilities(activeNetwork)?:return false
            return when{
                cababilities.hasTransport(TRANSPORT_WIFI)->true
                cababilities.hasTransport(TRANSPORT_CELLULAR)->true
                cababilities.hasTransport(TRANSPORT_ETHERNET)->true
                else -> false

            }
        }else{
            connectivityManager.activeNetworkInfo?.run {
                return when(type) {

                    TYPE_WIFI->true
                    TYPE_MOBILE->true
                    TYPE_ETHERNET->true
                    else->false
                }
            }
        }
        return false
    }
    private suspend fun safeBreakingCall(countryCode:String){
        breakingNews.postValue(Resources.Loading())
        try{
            if(hasConnection()){
                val response = repository.getBreakingNews(countryCode,breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }else{
                breakingNews.postValue(Resources.Error("No Internet Connection"))
            }
        }catch(t:Throwable){
            when(t){
                is IOException -> breakingNews.postValue(Resources.Error("Network Failed"))
                else -> breakingNews.postValue(Resources.Error("conversion error"))
            }
        }
    }
    private suspend fun safeSearchCall(searchQuery:String){
        breakingNews.postValue(Resources.Loading())
        try{
            if(hasConnection()){
                val response = repository.getSearchQuery(searchQuery,searchedPageNumber)
                searchedQuery.postValue(handleSearchedNewsResponse(response))
            }else{
                searchedQuery.postValue(Resources.Error("No Internet Connection"))
            }
        }catch(t:Throwable){
            when(t){
                is IOException -> searchedQuery.postValue(Resources.Error("Network Failed"))
                else -> searchedQuery.postValue(Resources.Error("conversion error"))
            }
        }
    }
}