package com.example.kp.data;

import androidx.room.TypeConverter;

import com.example.kp.entities.Player;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class PlayerListConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<Player> fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        Type listType = new TypeToken<List<Player>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toString(List<Player> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return gson.toJson(list);
    }
}