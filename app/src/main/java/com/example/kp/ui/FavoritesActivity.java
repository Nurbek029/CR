package com.example.kp.ui;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kp.R;
import com.example.kp.viewmodel.TeamsViewModel;
import com.example.kp.entities.Team;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private TeamsViewModel viewModel;
    private TeamsAdapter adapter;
    private TextView emptyTextView;
    private SearchView searchView;
    private List<Team> allFavoriteTeams = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
        searchView = findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Настройка адаптера
        adapter = new TeamsAdapter(new TeamsAdapter.OnTeamClickListener() {
            @Override
            public void onTeamClick(Team team) {
                TeamDetailActivity.start(FavoritesActivity.this, team);
            }

            @Override
            public void onFavoriteClick(Team team, boolean isFavorite) {
                if (!isFavorite) {
                    viewModel.removeFromFavorites(team.idTeam);
                    Snackbar.make(recyclerView, "Удалено из избранного", Snackbar.LENGTH_SHORT).show();

                    // Обновляем локальный список
                    allFavoriteTeams.removeIf(t -> t.idTeam.equals(team.idTeam));
                }
            }
        });
        recyclerView.setAdapter(adapter);

        // Инициализация ViewModel
        viewModel = new ViewModelProvider(this).get(TeamsViewModel.class);

        // Настройка поиска
        setupSearch();

        // Наблюдатель за избранными командами
        viewModel.getFavoriteTeams().observe(this, teams -> {
            if (teams != null && !teams.isEmpty()) {
                // Сохраняем все избранные команды для поиска
                allFavoriteTeams = new ArrayList<>(teams);

                adapter.setTeams(teams);
                recyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);

                // Применяем текущий поисковый запрос (если есть)
                String query = searchView.getQuery().toString();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
            } else {
                allFavoriteTeams.clear();
                adapter.setTeams(new ArrayList<>());
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Нет избранных команд");
            }
        });
    }

    // Метод для настройки поиска
    private void setupSearch() {
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Скрываем клавиатуру при нажатии Enter
                searchView.clearFocus();
                hideKeyboard();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        // Кнопка закрытия поиска
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Показываем все избранные команды при закрытии поиска
                adapter.setTeams(allFavoriteTeams);
                return false;
            }
        });
    }

    // Метод для выполнения поиска
    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Если поиск пустой - показываем все команды
            adapter.setTeams(allFavoriteTeams);
            return;
        }

        String searchQuery = query.toLowerCase().trim();
        List<Team> searchResults = new ArrayList<>();

        for (Team team : allFavoriteTeams) {
            if ((team.strTeam != null && team.strTeam.toLowerCase().contains(searchQuery)) ||
                    (team.strLeague != null && team.strLeague.toLowerCase().contains(searchQuery)) ||
                    (team.strCountry != null && team.strCountry.toLowerCase().contains(searchQuery))) {
                searchResults.add(team);
            }
        }

        adapter.setTeams(searchResults);

        // Если нет результатов - показываем сообщение
        if (searchResults.isEmpty() && !allFavoriteTeams.isEmpty()) {
            emptyTextView.setText("Ничего не найдено по запросу: " + query);
            emptyTextView.setVisibility(View.VISIBLE);
            findViewById(R.id.recyclerView).setVisibility(View.GONE);
        } else if (!searchResults.isEmpty()) {
            emptyTextView.setVisibility(View.GONE);
            findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
        }
    }

    // Метод для скрытия клавиатуры
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран обновляем данные
        if (viewModel != null) {
            viewModel.reloadTeams();
        }

    }
}