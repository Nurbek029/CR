package com.example.kp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;

public class FavoritesActivity extends AppCompatActivity {
    private TeamsViewModel viewModel;
    private TeamsAdapter adapter;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ★ ИСПРАВЛЕН АДАПТЕР ★
        adapter = new TeamsAdapter(new TeamsAdapter.OnTeamClickListener() {
            @Override
            public void onTeamClick(Team team) {
                // ★ ВАЖНО: Передаем всю команду ★
                TeamDetailActivity.start(FavoritesActivity.this, team);
            }

            @Override
            public void onFavoriteClick(Team team, boolean isFavorite) {
                if (!isFavorite) {
                    viewModel.removeFromFavorites(team.idTeam);
                    Snackbar.make(recyclerView, "Удалено из избранного", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.setAdapter(adapter);

        // ★ ИСПРАВЛЕНА ИНИЦИАЛИЗАЦИЯ ViewModel ★
        viewModel = new ViewModelProvider(this).get(TeamsViewModel.class);

        // Наблюдатель за избранными командами
        viewModel.getFavoriteTeams().observe(this, teams -> {
            if (teams != null && !teams.isEmpty()) {
                adapter.setTeams(teams);
                recyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            } else {
                adapter.setTeams(new ArrayList<>());
                recyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Нет избранных команд");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении обновляем данные
        if (viewModel != null) {
            // Можно обновить список
        }
    }
}