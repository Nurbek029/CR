package com.example.kp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TeamsViewModel viewModel;
    private TeamsAdapter adapter;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView emptyTextView;
    private LinearLayout stateContainer;
    private LinearLayout filtersContainer;
    private Spinner leagueSpinner;
    private BottomNavigationView bottomNavigation;

    // Списки для спиннера лиг
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

        // Настройка фильтра лиг
        setupLeagueFilter();

        // Настройка нижнего меню
        setupBottomNavigation();

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
        bottomNavigation = findViewById(R.id.bottomNavigation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new TeamsAdapter(new TeamsAdapter.OnTeamClickListener() {
            @Override
            public void onTeamClick(Team team) {
                TeamDetailActivity.start(MainActivity.this, team);
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

        // Наблюдатель за командами
        viewModel.getFilteredTeams().observe(this, teams -> {
            if (teams != null && !teams.isEmpty()) {
                showTeamsList();
                adapter.setTeams(teams);
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

    private void setupLeagueFilter() {
        // Настройка спиннера лиг
        leagueAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, availableLeaguesDisplayNames);
        leagueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leagueSpinner.setAdapter(leagueAdapter);

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
    }

    // ★ НАСТРОЙКА НИЖНЕГО МЕНЮ ★
    private void setupBottomNavigation() {
        // ★ Устанавливаем цвета программно ★
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked }, // Выбрано
                new int[] { -android.R.attr.state_checked } // Не выбрано
        };

        int[] colors = new int[] {
                getResources().getColor(R.color.bottom_nav_selected), // Выбрано - фиолетовый
                getResources().getColor(R.color.bottom_nav_unselected) // Не выбрано - серый
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);
        bottomNavigation.setItemIconTintList(colorStateList);
        bottomNavigation.setItemTextColor(colorStateList);

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_teams) {
                return true;
            }
            else if (id == R.id.nav_favorites) {
                Intent intent = new Intent(this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            }
            else if (id == R.id.nav_refresh) {
                viewModel.reloadTeams();
                Snackbar.make(findViewById(R.id.recyclerView),
                        "Обновление данных...", Snackbar.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });

        // Выделяем первый элемент по умолчанию
        bottomNavigation.setSelectedItemId(R.id.nav_teams);
    }

    private void loadData() {
        // Устанавливаем первую лигу по умолчанию
        if (!availableLeaguesKeys.isEmpty()) {
            leagueSpinner.setSelection(0);
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

    // ★ УБИРАЕМ ВЕРХНЕЕ МЕНЮ - теперь используем нижнее ★
    // Удаляем методы onCreateOptionsMenu и onOptionsItemSelected

    private void testAllEndpoints() {
        new Thread(() -> {
            try {
                StringBuilder results = new StringBuilder();
                results.append("=== ТЕСТИРОВАНИЕ API ЭНДПОЙНТОВ ===\n\n");

                String[] urls = RetrofitClient.getLeagueUrls();

                for (int i = 0; i < urls.length; i++) {
                    String url = urls[i];
                    results.append("Тест ").append(i + 1).append(": ").append(url).append("\n");

                    try {
                        java.net.URL testUrl = new java.net.URL(url);
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) testUrl.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);

                        int responseCode = conn.getResponseCode();
                        results.append("Код: ").append(responseCode).append("\n");

                        conn.disconnect();

                    } catch (Exception e) {
                        results.append("Ошибка: ").append(e.getMessage()).append("\n");
                    }

                    results.append("\n");
                }

                final String finalResults = results.toString();

                runOnUiThread(() -> {
                    android.util.Log.d("API_TEST", finalResults);
                    Toast.makeText(MainActivity.this,
                            "Тестирование завершено. Смотрите логи (API_TEST)",
                            Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Ошибка тестирования: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран выделяем первую вкладку
        bottomNavigation.setSelectedItemId(R.id.nav_teams);

        // Обновляем данные если список пуст
        if (adapter != null && adapter.getItemCount() == 0) {
            viewModel.reloadTeams();
        }
    }
}