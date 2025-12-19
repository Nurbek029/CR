package com.example.kp.network.models;

import com.example.kp.entities.Player;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlayerResponse {
    @SerializedName("player")
    public List<Player> player;
}