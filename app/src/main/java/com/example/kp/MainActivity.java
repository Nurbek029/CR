package com.example.kp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private TeamsViewModel viewModel;
    private TeamsAdapter adapter;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView emptyTextView;
    private LinearLayout stateContainer;
    private LinearLayout filtersContainer;
    private Spinner leagueSpinner;
    private Spinner countrySpinner;
    private Button resetFilterButton;
    private Button debugApiButton;
    private List<String> countriesList = new ArrayList<>();
    private ArrayAdapter<String> countryAdapter;

    // Списки для спиннеров
    private List<String> availableLeaguesKeys = new ArrayList<>();
    private List<String> availableLeaguesDisplayNames = new ArrayList<>();
    private ArrayAdapter<String> leagueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация всех View
        initViews();

        // Настройка адаптера
        setupAdapter();

        // Настройка ViewModel
        setupViewModel();

        // Настройка поиска
        setupSearch();

        // Настройка фильтров
        setupFilters();

        // Настройка кнопки отладки
        setupDebugButton();

        // Загрузка данных
        loadData();
    }

    private void initViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        SearchView searchView = findViewById(R.id.searchView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        emptyTextView = findViewById(R.id.emptyTextView);
        stateContainer = findViewById(R.id.stateContainer);
        filtersContainer = findViewById(R.id.filtersContainer);
        leagueSpinner = findViewById(R.id.leagueSpinner);
        countrySpinner = findViewById(R.id.countrySpinner);
        resetFilterButton = findViewById(R.id.resetFilterButton);
        debugApiButton = findViewById(R.id.debugApiButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new TeamsAdapter(new TeamsAdapter.OnTeamClickListener() {
            @Override
            public void onTeamClick(Team team) {
                if (team != null && team.idTeam != null) {
                    TeamDetailActivity.start(MainActivity.this, team.idTeam);
                }
            }

            @Override
            public void onFavoriteClick(Team team, boolean isFavorite) {
                if (team != null) {
                    if (isFavorite) {
                        viewModel.addToFavorites(team);
                        Snackbar.make(findViewById(R.id.recyclerView),
                                "Добавлено в избранное", Snackbar.LENGTH_SHORT).show();
                    } else {
                        viewModel.removeFromFavorites(team.idTeam);
                        Snackbar.make(findViewById(R.id.recyclerView),
                                "Удалено из избранного", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TeamsViewModel.class);

        // Получаем список доступных лиг из ViewModel
        availableLeaguesKeys = viewModel.getAvailableLeagues();

        // Преобразуем ключи в отображаемые названия
        for (String leagueKey : availableLeaguesKeys) {
            String displayName = viewModel.getLeagueDisplayName(leagueKey);
            availableLeaguesDisplayNames.add(displayName);
        }

        android.util.Log.d("MainActivity", "Доступно лиг: " + availableLeaguesKeys.size());

        // Наблюдатель за отфильтрованными командами
        viewModel.getFilteredTeams().observe(this, teams -> {
            if (teams != null && !teams.isEmpty()) {
                showTeamsList();
                adapter.setTeams(teams);
                updateCountryFilters(teams);
            } else {
                showEmptyState("Команды не найдены");
            }
        });

        // Наблюдатель за состоянием загрузки
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            }
        });

        // Наблюдатель за ошибками
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showError(errorMessage);
                Snackbar.make(findViewById(R.id.recyclerView),
                        errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });

        // Наблюдатель за загрузкой всех лиг
        viewModel.getAllTeamsByLeague().observe(this, allTeams -> {
            if (allTeams != null && !allTeams.isEmpty()) {
                Snackbar.make(findViewById(R.id.recyclerView),
                        "Загружены команды из " + allTeams.size() + " лиг", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.searchTeams(newText);
                return true;
            }
        });
    }

    private void setupFilters() {
        // Настройка спиннера лиг
        leagueAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, availableLeaguesDisplayNames);
        leagueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leagueSpinner.setAdapter(leagueAdapter);

        // Настройка спиннера стран
        countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countriesList);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        // Выбор лиги
        leagueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < availableLeaguesKeys.size()) {
                    String selectedLeagueKey = availableLeaguesKeys.get(position);
                    String selectedDisplayName = availableLeaguesDisplayNames.get(position);

                    viewModel.setCurrentLeague(selectedLeagueKey);
                    Snackbar.make(findViewById(R.id.recyclerView),
                            "Загружаем: " + selectedDisplayName, Snackbar.LENGTH_SHORT).show();

                    android.util.Log.d("MainActivity", "Выбрана лига: " + selectedLeagueKey);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Выбор страны
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Пропускаем первый элемент "Все страны"
                    String selectedCountry = (String) parent.getItemAtPosition(position);
                    viewModel.filterByCountry(selectedCountry);
                    Snackbar.make(findViewById(R.id.recyclerView),
                            "Фильтр по стране: " + selectedCountry, Snackbar.LENGTH_SHORT).show();
                } else {
                    viewModel.resetFilter();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Кнопка сброса фильтров
        resetFilterButton.setOnClickListener(v -> {
            viewModel.resetFilter();
            leagueSpinner.setSelection(0);
            countrySpinner.setSelection(0);
            Snackbar.make(v, "Фильтры сброшены", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void setupDebugButton() {
        debugApiButton.setOnClickListener(v -> {
            debugApiButton.setEnabled(false);
            debugApiButton.setText("Тестируем API...");

            // Тестируем все эндпоинты лиг
            new Thread(() -> {
                try {
                    StringBuilder results = new StringBuilder();
                    results.append("=== ТЕСТИРОВАНИЕ 9 ЭНДПОЙНТОВ ЛИГ ===\n\n");

                    for (int i = 0; i < availableLeaguesKeys.size(); i++) {
                        String leagueKey = availableLeaguesKeys.get(i);
                        String displayName = availableLeaguesDisplayNames.get(i);

                        String encodedLeague = java.net.URLEncoder.encode(leagueKey, "UTF-8");
                        String url = "https://02721d2d-8318-4cfe-ab98-2bd86c41dd8b.mock.pstmn.io/search_all_teams.php?l=" + encodedLeague;

                        results.append("Тест ").append(i + 1).append(": ").append(displayName).append("\n");
                        results.append("URL: ").append(url).append("\n");

                        try {
                            java.net.URL testUrl = new java.net.URL(url);
                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) testUrl.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setConnectTimeout(10000);
                            conn.setReadTimeout(10000);

                            int responseCode = conn.getResponseCode();
                            String responseMessage = conn.getResponseMessage();

                            // Читаем ответ
                            StringBuilder response = new StringBuilder();
                            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                                    new java.io.InputStreamReader(conn.getInputStream()))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                            }

                            conn.disconnect();

                            results.append("✓ Код: ").append(responseCode)
                                    .append(", Сообщение: ").append(responseMessage)
                                    .append(", Длина: ").append(response.length()).append(" символов\n");

                            // Парсим JSON для подсчета команд
                            if (responseCode == 200 && response.length() > 0) {
                                try {
                                    org.json.JSONObject json = new org.json.JSONObject(response.toString());
                                    if (json.has("teams")) {
                                        org.json.JSONArray teams = json.getJSONArray("teams");
                                        results.append("Количество команд: ").append(teams.length()).append("\n");
                                    }
                                } catch (Exception e) {
                                    results.append("Ошибка парсинга JSON\n");
                                }
                            }

                        } catch (Exception e) {
                            results.append("✗ Ошибка: ").append(e.getClass().getSimpleName())
                                    .append(" - ").append(e.getMessage()).append("\n");
                        }

                        results.append("\n");
                    }

                    final String finalResults = results.toString();

                    runOnUiThread(() -> {
                        // Показываем результат в Logcat
                        android.util.Log.d("API_DEBUG", finalResults);

                        // Обновляем UI
                        debugApiButton.setEnabled(true);
                        debugApiButton.setText("DEBUG: Проверить API");

                        // Показываем краткий результат
                        long successCount = finalResults.chars().filter(ch -> ch == '✓').count();
                        String message = "Протестировано " + availableLeaguesKeys.size() + " лиг. Успешно: " + successCount;
                        Snackbar.make(debugApiButton, message, Snackbar.LENGTH_LONG).show();

                        // Показываем Toast с деталями
                        Toast.makeText(MainActivity.this,
                                "Тестирование завершено. Смотрите логи (API_DEBUG)",
                                Toast.LENGTH_LONG).show();
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "Ошибка тестирования: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        debugApiButton.setEnabled(true);
                        debugApiButton.setText("DEBUG: Проверить API");
                    });
                }
            }).start();
        });
    }

    private void loadData() {
        // Устанавливаем первую лигу по умолчанию
        if (!availableLeaguesKeys.isEmpty()) {
            leagueSpinner.setSelection(0);
        }
    }

    private void updateCountryFilters(List<Team> teams) {
        Set<String> uniqueCountries = new HashSet<>();
        for (Team team : teams) {
            if (team.strCountry != null && !team.strCountry.isEmpty()) {
                uniqueCountries.add(team.strCountry);
            }
        }

        countriesList.clear();
        countriesList.add("Все страны"); // Первый элемент
        countriesList.addAll(uniqueCountries);

        if (countryAdapter != null) {
            countryAdapter.notifyDataSetChanged();
        }
    }

    private void showLoading() {
        stateContainer.setVisibility(View.VISIBLE);
        filtersContainer.setVisibility(View.GONE);
        findViewById(R.id.recyclerView).setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        stateContainer.setVisibility(View.VISIBLE);
        filtersContainer.setVisibility(View.GONE);
        findViewById(R.id.recyclerView).setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
        errorTextView.setText(message);
    }

    private void showEmptyState(String message) {
        stateContainer.setVisibility(View.VISIBLE);
        filtersContainer.setVisibility(View.VISIBLE);
        findViewById(R.id.recyclerView).setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText(message);
    }

    private void showTeamsList() {
        stateContainer.setVisibility(View.GONE);
        filtersContainer.setVisibility(View.VISIBLE);
        findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Добавляем пункт для загрузки всех лиг
        MenuItem allLeaguesItem = menu.add("Все лиги мира");
        allLeaguesItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        allLeaguesItem.setOnMenuItemClickListener(item -> {
            viewModel.loadAllTeamsFromAllLeagues();
            Snackbar.make(findViewById(R.id.recyclerView),
                    "Загружаем команды из всех лиг...", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Добавляем кнопки навигации по лигам
        MenuItem prevLeague = menu.add("← Предыдущая лига");
        prevLeague.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        prevLeague.setIcon(android.R.drawable.ic_media_previous);
        prevLeague.setOnMenuItemClickListener(item -> {
            viewModel.previousLeague();
            int currentPosition = leagueSpinner.getSelectedItemPosition();
            int newPosition = (currentPosition - 1 + availableLeaguesKeys.size()) % availableLeaguesKeys.size();
            leagueSpinner.setSelection(newPosition);
            return true;
        });

        MenuItem nextLeague = menu.add("Следующая лига →");
        nextLeague.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        nextLeague.setIcon(android.R.drawable.ic_media_next);
        nextLeague.setOnMenuItemClickListener(item -> {
            viewModel.nextLeague();
            int currentPosition = leagueSpinner.getSelectedItemPosition();
            int newPosition = (currentPosition + 1) % availableLeaguesKeys.size();
            leagueSpinner.setSelection(newPosition);
            return true;
        });

        // Добавляем пункт для тестирования API
        MenuItem testApiItem = menu.add("Тест API (активити)");
        testApiItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        testApiItem.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(MainActivity.this, TestApiActivity.class);
            startActivity(intent);
            return true;
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_refresh) {
            viewModel.reloadTeams();
            Snackbar.make(findViewById(R.id.recyclerView), "Обновление...", Snackbar.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран можно обновить данные
        if (adapter != null && adapter.getItemCount() == 0) {
            viewModel.reloadTeams();
        }
    }
}