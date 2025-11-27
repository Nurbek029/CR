package com.example.kp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("searchteams.php")
    Call<TeamResponse> searchTeams(@Query("t") String team);

}
