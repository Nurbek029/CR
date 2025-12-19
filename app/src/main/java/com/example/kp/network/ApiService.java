package com.example.kp.network;

import com.example.kp.network.models.PlayerResponse;
import com.example.kp.network.models.TeamResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    // Существующие методы

    @GET("lookup_all_players.php")
    Call<PlayerResponse> getTeamPlayers(@Query("id") String teamId);

    // Команды по лиге
    @GET("search_all_teams.php")
    Call<TeamResponse> getTeams(@Query("l") String league);

    // ★ НОВЫЙ МЕТОД: Все команды ★
    @GET("search_all_teams.php")
    Call<TeamResponse> getAllTeams();
}