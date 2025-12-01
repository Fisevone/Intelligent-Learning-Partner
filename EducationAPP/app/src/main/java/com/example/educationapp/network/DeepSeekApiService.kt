package com.example.educationapp.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * DeepSeek API服务接口
 */
interface DeepSeekApiService {
    
    @POST(ApiConstants.CHAT_ENDPOINT)
    suspend fun chatCompletion(
        @Header(ApiConstants.HEADER_AUTHORIZATION) authorization: String,
        @Header(ApiConstants.HEADER_CONTENT_TYPE) contentType: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}




