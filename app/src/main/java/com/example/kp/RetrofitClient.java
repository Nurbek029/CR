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

    // Тестовые URL для разных лиг
    public static final String[] TEST_URLS = {
            BASE_URL + "search_all_teams.php?l=English%20Premier%20League",
            BASE_URL + "search_all_teams.php?l=Spanish%20La%20Liga",
            BASE_URL + "search_all_teams.php?l=German%20Bundesliga",
            BASE_URL + "search_all_teams.php?l=Italian%20Serie%20A",
            BASE_URL + "search_all_teams.php?l=French%20Ligue%201",
            BASE_URL + "search_all_teams.php?l=Dutch%20Eredivisie",
            BASE_URL + "search_all_teams.php?l=Portuguese%20Primeira%20Liga",
            BASE_URL + "search_all_teams.php?l=Brazilian%20Serie%20A",
            BASE_URL + "search_all_teams.php?l=Argentine%20Primera%20Division"
    };

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
            android.util.Log.d("RETROFIT", "Доступно эндпоинтов лиг: " + TEST_URLS.length);
        }
        return retrofit;
    }

    // Метод для тестирования всех эндпоинтов
    public static void testAllEndpoints() {
        android.util.Log.d("RETROFIT", "Тестирование всех эндпоинтов лиг:");
        for (String url : TEST_URLS) {
            android.util.Log.d("RETROFIT", "URL: " + url);
        }
    }
}