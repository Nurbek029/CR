package com.example.kp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.okhttp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RetrofitMock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testMockApi();
    }

    private void testMockApi() {

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);

        api.searchTeams("Arsenal").enqueue(new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {

                if (response.isSuccessful()) {

                    TeamResponse result = response.body();

                    if (result != null && result.teams != null && !result.teams.isEmpty()) {

                        String teamName = result.teams.get(0).strTeam;
                        Log.i(TAG, "Название команды: " + teamName);

                    } else {
                        Log.e(TAG, "Ответ пустой или нет teams[]");
                    }

                } else {
                    Log.e(TAG, "HTTP ошибка: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                Log.e(TAG, "Ошибка запроса: " + t.getMessage());
            }
        });
    }
}
