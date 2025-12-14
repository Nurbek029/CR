package com.example.kp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "favorite_teams")
public class Team {
    @PrimaryKey
    @NonNull
    @SerializedName("idTeam")
    public String idTeam = ""; // Инициализация по умолчанию

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

    // Поля для избранного (только в Room)
    public String comment = "";
    public float rating = 0f;
    public boolean isFavorite = false;
}