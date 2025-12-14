package com.example.kp;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Team.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TeamDao teamDao();

    private static volatile AppDatabase INSTANCE;
    // Executor для работы с БД в фоновом потоке
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
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}