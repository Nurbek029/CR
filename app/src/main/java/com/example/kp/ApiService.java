package com.example.kp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    // Существующие методы
    @GET("all_leagues.php")
    Call<LeagueResponse> getAllLeagues();

    @GET("searchteams.php")
    Call<TeamResponse> searchTeams(@Query("t") String query);

    @GET("lookup_all_players.php")
    Call<PlayerResponse> getTeamPlayers(@Query("id") String teamId);

    // НОВЫЕ МЕТОДЫ ДЛЯ РАЗНЫХ ЛИГ
    // Используем один эндпоинт с параметром лиги
    @GET("search_all_teams.php")
    Call<TeamResponse> getTeams(@Query("l") String league);
}