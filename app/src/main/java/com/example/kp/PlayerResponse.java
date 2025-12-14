package com.example.kp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlayerResponse {
    @SerializedName("player")
    public List<Player> player;
}