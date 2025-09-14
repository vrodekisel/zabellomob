package com.example.zabello.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** Внешний API: Europe PMC — поиск медицинских/биомед статей. */
public interface RemoteArticleService {
    /**
     * Пример:
     * https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=diabetes&format=json&pageSize=25&page=1
     */
    @GET("webservices/rest/search")
    Call<EuropePmcResponse> search(
            @Query("query") String query,
            @Query("format") String format,      // "json"
            @Query("pageSize") int pageSize,     // 1..1000, разумно 25..50
            @Query("page") int page              // 1..N
    );
}
