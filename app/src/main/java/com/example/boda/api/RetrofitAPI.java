package com.example.boda.api;

import static com.example.boda.Config.APP_KEY_TOKEN;
import static com.example.boda.Config.KAKAO_REST_API_KEY;

import com.example.boda.api.route.RequestInfo;
import com.example.boda.api.route.ResponseInfo;
import com.example.boda.api.search.SearchResponseInfo;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface RetrofitAPI {
    @Headers(
            {
                    "appKey: " + APP_KEY_TOKEN,
                    "Content-Type: application/json"
            }
    )
    @POST("/tmap/routes/pedestrian?version=1&format=json&callback=result")
    Call<ResponseInfo> walkingDirection(@Body RequestInfo requestInfo);

    @Headers(
            {
                    "Authorization: KakaoAK " + KAKAO_REST_API_KEY
            }
    )
    @GET("local/search/keyword.json")
    Call<SearchResponseInfo> findPlace(@QueryMap Map<String, String> querys);
}
