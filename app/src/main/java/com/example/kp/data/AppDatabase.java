package com.example.kp.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.example.kp.entities.Player;
import com.example.kp.entities.Team;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Team.class, Player.class}, version = 2, exportSchema = false)
@TypeConverters({PlayerListConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TeamDao teamDao();
    public abstract PlayerDao playerDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "sports_db"
                            ).fallbackToDestructiveMigration() // Удаляем старую БД при обновлении
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}