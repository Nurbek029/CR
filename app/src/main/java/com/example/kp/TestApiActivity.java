package com.example.kp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestApiActivity extends AppCompatActivity {

    private TextView statusTextView;
    private TextView resultTextView;
    private Button testButton;
    private Button backButton;

    private OkHttpClient client;
    private int endpointsToTest = 4;
    private int endpointsTested = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_api);

        statusTextView = findViewById(R.id.statusTextView);
        resultTextView = findViewById(R.id.resultTextView);
        testButton = findViewById(R.id.testButton);
        backButton = findViewById(R.id.backButton);

        // Создаем клиент с увеличенными таймаутами
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        testButton.setOnClickListener(v -> testAllEndpoints());
        backButton.setOnClickListener(v -> finish());
    }

    private void testAllEndpoints() {
        clearResults();
        endpointsTested = 0;
        updateStatus("Начало тестирования...");

        // Тестируем все эндпоинты
        testEndpoint("Получение всех лиг",
                "https://02721d2d-8318-4cfe-ab98-2bd86c41dd8b.mock.pstmn.io/all_leagues.php");

        testEndpoint("Команды Английской Премьер-лиги",
                "https://02721d2d-8318-4cfe-ab98-2bd86c41dd8b.mock.pstmn.io/search_all_teams.php?l=English%20Premier%20League");

        testEndpoint("Поиск команд по запросу 'Chelsea'",
                "https://02721d2d-8318-4cfe-ab98-2bd86c41dd8b.mock.pstmn.io/searchteams.php?t=Chelsea");

        testEndpoint("Игроки команды (пример ID)",
                "https://02721d2d-8318-4cfe-ab98-2bd86c41dd8b.mock.pstmn.io/lookup_all_players.php?id=133604");
    }

    private void testEndpoint(String endpointName, String url) {
        updateStatus("Тестируем: " + endpointName);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    String errorMessage = "✗ " + endpointName + ": ОШИБКА\n" +
                            "URL: " + url + "\n" +
                            "Ошибка: " + e.getMessage() + "\n" +
                            "Класс ошибки: " + e.getClass().getSimpleName() + "\n\n";
                    appendResult(errorMessage);
                    checkTestCompletion();
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    try {
                        String responseBody = response.body() != null ?
                                response.body().string() : "Пустое тело ответа";

                        String resultMessage = "✓ " + endpointName + "\n" +
                                "URL: " + url + "\n" +
                                "Код ответа: " + response.code() + "\n" +
                                "Сообщение: " + response.message() + "\n" +
                                "Размер ответа: " + responseBody.length() + " символов\n";

                        // Если ответ большой, показываем только начало
                        if (responseBody.length() > 500) {
                            resultMessage += "Первые 500 символов:\n" +
                                    responseBody.substring(0, Math.min(500, responseBody.length())) + "...\n";
                        } else {
                            resultMessage += "Тело ответа:\n" + responseBody + "\n";
                        }

                        resultMessage += "\n" + "=".repeat(50) + "\n\n";
                        appendResult(resultMessage);

                        // Проверяем JSON
                        if (responseBody.contains("\"teams\"") ||
                                responseBody.contains("\"leagues\"") ||
                                responseBody.contains("\"player\"")) {
                            updateStatus(endpointName + ": JSON найден ✓");
                        } else if (responseBody.contains("error") ||
                                responseBody.contains("Error") ||
                                responseBody.contains("ERROR")) {
                            updateStatus(endpointName + ": Возможная ошибка в ответе ⚠");
                        }
                    } catch (IOException e) {
                        String errorMessage = "✗ " + endpointName + ": ОШИБКА чтения ответа\n" +
                                "URL: " + url + "\n" +
                                "Ошибка: " + e.getMessage() + "\n\n";
                        appendResult(errorMessage);
                    } finally {
                        if (response.body() != null) {
                            response.body().close();
                        }
                        checkTestCompletion();
                    }
                });
            }
        });
    }

    private void checkTestCompletion() {
        endpointsTested++;
        if (endpointsTested >= endpointsToTest) {
            updateStatus("Тестирование завершено!");
        }
    }

    private void updateStatus(String status) {
        runOnUiThread(() -> {
            statusTextView.setText("Статус: " + status);
            if (status.contains("✗") || status.contains("ОШИБКА")) {
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (status.contains("✓")) {
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
        });
    }

    private void appendResult(String text) {
        runOnUiThread(() -> {
            String currentText = resultTextView.getText().toString();
            resultTextView.setText(currentText + text);
        });
    }

    private void clearResults() {
        runOnUiThread(() -> {
            resultTextView.setText("");
            statusTextView.setText("Статус: Тестирование запущено...");
            statusTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }
}