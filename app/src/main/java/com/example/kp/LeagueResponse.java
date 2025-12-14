package com.example.kp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LeagueResponse {
    @SerializedName("leagues")
    public List<League> leagues;
}