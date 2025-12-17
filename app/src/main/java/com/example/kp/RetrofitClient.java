package com.example.kp;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static Retrofit retrofit;

    // Убедитесь, что BASE_URL соответствует вашему mock-серверу
    private static final String BASE_URL = "https://02721d2d-8318-4cfe-ab98-2bd86c41dd8b.mock.pstmn.io/";

    public static Retrofit getInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            android.util.Log.d("RETROFIT", "Retrofit клиент создан для URL: " + BASE_URL);
        }
        return retrofit;
    }

    // Метод для проверки URL лиг
    public static String[] getLeagueUrls() {
        String[] leagues = {
                "English Premier League",
                "Spanish La Liga",
                "German Bundesliga",
                "Italian Serie A",
                "French Ligue 1"
        };

        String[] urls = new String[leagues.length];
        for (int i = 0; i < leagues.length; i++) {
            try {
                String encoded = java.net.URLEncoder.encode(leagues[i], "UTF-8");
                urls[i] = BASE_URL + "search_all_teams.php?l=" + encoded;
            } catch (Exception e) {
                urls[i] = BASE_URL + "search_all_teams.php?l=" + leagues[i].replace(" ", "%20");
            }
        }
        return urls;
    }
}