package com.example.boda.route;

import static com.example.boda.route.Config.APP_KEY_TOKEN;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitAPI {
    @Headers(
            {
                    "appKey: " + APP_KEY_TOKEN,
                    "Content-Type: application/json"
            }
    )
    @POST("/tmap/routes/pedestrian?version=1&format=json&callback=result")
    Call<ResponseInfo> walkingDirection(@Body RequestInfo requestInfo);
}
