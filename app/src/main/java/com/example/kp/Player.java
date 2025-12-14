package com.example.kp;

import com.google.gson.annotations.SerializedName;

public class Player {
    @SerializedName("idPlayer")
    public String idPlayer;

    @SerializedName("strPlayer")
    public String strPlayer;

    @SerializedName("strPosition")
    public String strPosition;

    @SerializedName("strNumber")
    public String strNumber;

    @SerializedName("strThumb")
    public String strThumb;
}