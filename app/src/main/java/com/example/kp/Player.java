package com.example.kp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "players")
public class Player implements Serializable {
    @PrimaryKey
    @NonNull
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

    @SerializedName("strCutout")
    public String strCutout;

    @SerializedName("dateBorn")
    public String dateBorn;

    @SerializedName("strNationality")
    public String strNationality;

    @SerializedName("strHeight")
    public String strHeight;

    @SerializedName("strWeight")
    public String strWeight;

    @SerializedName("strDescriptionEN")
    public String strDescriptionEN;

    // Конструктор
    public Player() {
        this.idPlayer = "";
    }

    // Геттеры и сеттеры
    public String getIdPlayer() {
        return idPlayer;
    }

    public void setIdPlayer(String idPlayer) {
        this.idPlayer = idPlayer;
    }

    public String getStrPlayer() {
        return strPlayer;
    }

    public void setStrPlayer(String strPlayer) {
        this.strPlayer = strPlayer;
    }

    public String getStrPosition() {
        return strPosition;
    }

    public void setStrPosition(String strPosition) {
        this.strPosition = strPosition;
    }

    public String getStrNumber() {
        return strNumber;
    }

    public void setStrNumber(String strNumber) {
        this.strNumber = strNumber;
    }

    public String getStrThumb() {
        return strThumb;
    }

    public void setStrThumb(String strThumb) {
        this.strThumb = strThumb;
    }

    public String getStrCutout() {
        return strCutout;
    }

    public void setStrCutout(String strCutout) {
        this.strCutout = strCutout;
    }

    public String getDateBorn() {
        return dateBorn;
    }

    public void setDateBorn(String dateBorn) {
        this.dateBorn = dateBorn;
    }

    public String getStrNationality() {
        return strNationality;
    }

    public void setStrNationality(String strNationality) {
        this.strNationality = strNationality;
    }

    public String getStrHeight() {
        return strHeight;
    }

    public void setStrHeight(String strHeight) {
        this.strHeight = strHeight;
    }

    public String getStrWeight() {
        return strWeight;
    }

    public void setStrWeight(String strWeight) {
        this.strWeight = strWeight;
    }

    public String getStrDescriptionEN() {
        return strDescriptionEN;
    }

    public void setStrDescriptionEN(String strDescriptionEN) {
        this.strDescriptionEN = strDescriptionEN;
    }
}