package com.example.kp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

@Entity(tableName = "favorite_teams")
@TypeConverters(PlayerListConverter.class)
public class Team implements Serializable {
    @PrimaryKey
    @NonNull
    @SerializedName("idTeam")
    public String idTeam = "";

    @SerializedName("strTeam")
    public String strTeam = "";

    @SerializedName("strLeague")
    public String strLeague = "";

    @SerializedName("strSport")
    public String strSport = "";

    @SerializedName("strCountry")
    public String strCountry = "";

    @SerializedName("strStadium")
    public String strStadium = "";

    @SerializedName("strBadge")
    public String strBadge = "";

    @SerializedName("intFormedYear")
    public String intFormedYear = "";

    @SerializedName("strDescriptionEN")
    public String strDescriptionEN = "";

    // ★ ВАЖНО: Это поле должно правильно десериализоваться из JSON ★
    @SerializedName("keyPlayers")
    public List<Player> keyPlayers;

    // Поля для избранного (только в Room)
    public String comment = "";
    public float rating = 0f;
    public boolean isFavorite = false;

    // Конструктор для Room
    public Team() {}

    // Геттеры и сеттеры
    public String getIdTeam() {
        return idTeam;
    }

    public void setIdTeam(String idTeam) {
        this.idTeam = idTeam;
    }

    public String getStrTeam() {
        return strTeam;
    }

    public void setStrTeam(String strTeam) {
        this.strTeam = strTeam;
    }

    public String getStrLeague() {
        return strLeague;
    }

    public void setStrLeague(String strLeague) {
        this.strLeague = strLeague;
    }

    public List<Player> getKeyPlayers() {
        return keyPlayers;
    }

    public void setKeyPlayers(List<Player> keyPlayers) {
        this.keyPlayers = keyPlayers;
    }
}