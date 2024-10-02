package com.axel.axelai.Api;

import com.axel.axelai.Request.ChatRequest;
import com.axel.axelai.Request.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApiService {
    @POST("v1beta/models/gemini-1.5-flash-latest:generateContent?key=")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
}
