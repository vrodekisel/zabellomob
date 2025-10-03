package com.example.zabello.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RemoteArticleService {
    @GET("webservices/rest/search")
    Call<EuropePmcResponse> search(
            @Query("query") String query,
            @Query("format") String format,
            @Query("pageSize") int pageSize,
            @Query("page") int page
    );
}
