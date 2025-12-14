// TeamDetailActivity.java
package com.example.kp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class TeamDetailActivity extends AppCompatActivity {
    private TeamsViewModel viewModel;
    private String teamId;
    private Team currentTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        teamId = getIntent().getStringExtra("TEAM_ID");
        if (teamId == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(TeamsViewModel.class);

        // Наблюдатель за загруженными командами
        viewModel.getTeams().observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        if (resource.data != null) {
                            for (Team team : resource.data) {
                                if (team.idTeam.equals(teamId)) {
                                    currentTeam = team;
                                    displayTeamDetails(team);
                                    break;
                                }
                            }
                        }
                        break;
                    case ERROR:
                        showError(resource.message);
                        break;
                    case LOADING:
                        // Показать индикатор загрузки
                        break;
                }
            }
        });

        // Наблюдатель за игроками
        viewModel.getPlayers().observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        if (resource.data != null && !resource.data.isEmpty()) {
                            displayPlayers(resource.data);
                        }
                        break;
                    case ERROR:
                        showError(resource.message);
                        break;
                    case LOADING:
                        // Показать индикатор загрузки
                        break;
                }
            }
        });

        // Наблюдатель за данными команды из базы
        viewModel.getTeamByIdFromDb(teamId).observe(this, team -> {
            if (team != null && currentTeam != null) {
                // Обновляем текущую команду данными из базы
                currentTeam.comment = team.comment;
                currentTeam.rating = team.rating;
                currentTeam.isFavorite = team.isFavorite;

                // Обновляем UI
                updateFavoriteButton();
                updateCommentFields();
            }
        });

        // Загрузить данные
        viewModel.setCurrentLeague("English Premier League");
        viewModel.loadTeamPlayers(teamId);

        setupButtons();
    }

    private void setupButtons() {
        // Кнопка добавления в избранное
        Button favoriteButton = findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(v -> {
            if (currentTeam != null) {
                if (!currentTeam.isFavorite) {
                    viewModel.addToFavorites(currentTeam);
                    Snackbar.make(v, "Добавлено в избранное", Snackbar.LENGTH_SHORT).show();
                } else {
                    viewModel.removeFromFavorites(currentTeam.idTeam);
                    Snackbar.make(v, "Удалено из избранного", Snackbar.LENGTH_SHORT).show();
                }
                currentTeam.isFavorite = !currentTeam.isFavorite;
                updateFavoriteButton();
            }
        });

        // Сохранение комментария и оценки
        Button saveCommentButton = findViewById(R.id.saveCommentButton);
        saveCommentButton.setOnClickListener(v -> {
            EditText commentEditText = findViewById(R.id.commentEditText);
            RatingBar ratingBar = findViewById(R.id.ratingBar);

            String comment = commentEditText.getText().toString();
            float rating = ratingBar.getRating();

            viewModel.updateTeamComment(teamId, comment, rating);
            Snackbar.make(v, "Комментарий сохранен", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void displayTeamDetails(Team team) {
        TextView name = findViewById(R.id.teamName);
        TextView league = findViewById(R.id.teamLeague);
        TextView country = findViewById(R.id.teamCountry);
        TextView stadium = findViewById(R.id.teamStadium);
        TextView year = findViewById(R.id.teamYear);
        TextView description = findViewById(R.id.teamDescription);
        ImageView badge = findViewById(R.id.teamBadge);

        name.setText(team.strTeam);
        league.setText(team.strLeague);
        country.setText(team.strCountry);
        stadium.setText(team.strStadium);
        year.setText(team.intFormedYear);
        description.setText(team.strDescriptionEN);

        if (team.strBadge != null && !team.strBadge.isEmpty()) {
            Glide.with(this).load(team.strBadge).into(badge);
        }

        updateFavoriteButton();
        updateCommentFields();
    }

    private void updateFavoriteButton() {
        Button favoriteButton = findViewById(R.id.favoriteButton);
        if (currentTeam != null) {
            favoriteButton.setText(currentTeam.isFavorite ?
                    "Удалить из избранного" : "В избранное");
        }
    }

    private void updateCommentFields() {
        if (currentTeam != null && currentTeam.comment != null) {
            EditText commentEditText = findViewById(R.id.commentEditText);
            RatingBar ratingBar = findViewById(R.id.ratingBar);
            commentEditText.setText(currentTeam.comment);
            ratingBar.setRating(currentTeam.rating);
        }
    }

    private void displayPlayers(List<Player> players) {
        RecyclerView playersRecyclerView = findViewById(R.id.playersRecyclerView);
        playersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        PlayersAdapter adapter = new PlayersAdapter(players);
        playersRecyclerView.setAdapter(adapter);
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG).show();
    }

    public static void start(AppCompatActivity activity, String teamId) {
        Intent intent = new Intent(activity, TeamDetailActivity.class);
        intent.putExtra("TEAM_ID", teamId);
        activity.startActivity(intent);
    }
}