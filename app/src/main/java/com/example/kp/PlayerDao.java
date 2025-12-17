package com.example.kp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface PlayerDao {
    @Insert
    void insert(Player player);

    @Update
    void update(Player player);

    @Query("SELECT * FROM players WHERE idPlayer = :id")
    Player getPlayerById(String id);

    @Query("SELECT * FROM players WHERE idPlayer IN (:ids)")
    List<Player> getPlayersByIds(List<String> ids);

    @Query("DELETE FROM players")
    void deleteAll();
}