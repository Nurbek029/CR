package com.example.kp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TeamDao {
    @Insert
    void insert(Team team);

    @Update
    void update(Team team);

    @Delete
    void delete(Team team);

    @Query("SELECT * FROM favorite_teams WHERE isFavorite = 1")
    LiveData<List<Team>> getFavoriteTeams();

    @Query("SELECT * FROM favorite_teams WHERE idTeam = :id")
    Team getTeamById(String id);

    @Query("DELETE FROM favorite_teams WHERE idTeam = :id")
    void deleteById(String id);

    @Query("DELETE FROM favorite_teams")
    void deleteAll();

    // ★ ДОБАВЬТЕ ЭТОТ МЕТОД ЕСЛИ ЕГО НЕТ ★
    @Query("SELECT * FROM favorite_teams")
    LiveData<List<Team>> getAllTeams();
}