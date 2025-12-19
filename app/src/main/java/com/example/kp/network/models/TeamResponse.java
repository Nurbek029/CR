package com.example.kp.network.models;

import com.example.kp.entities.Team;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TeamResponse {
    @SerializedName("teams")
    public List<Team> teams;
}