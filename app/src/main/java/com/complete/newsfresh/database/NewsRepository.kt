package com.complete.newsfresh.database

import com.complete.newsfresh.api.RetrofitInstance
import com.complete.newsfresh.model.Article


class NewsRepository (private val db :ArticleDatabase) {
    suspend fun getBreakingNews(countryCode:String, pageNumber:Int) = RetrofitInstance.api.getBreakingNews(countryCode,pageNumber)
    suspend fun getSearchQuery(searchedQuery:String,pageNumber: Int) = RetrofitInstance.api.searchForNews(searchedQuery,pageNumber)

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)
    suspend fun delete(article: Article) = db.getArticleDao().delete(article)
    fun getAll() = db.getArticleDao().getAllArticles()
}