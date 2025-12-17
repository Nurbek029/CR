package com.example.kp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import java.io.Serializable;
import java.util.List;

public class TeamDetailActivity extends AppCompatActivity {
    private TeamsViewModel viewModel;
    private String teamId;
    private Team currentTeam;
    private PlayersAdapter playersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        // Получаем ВСЮ команду из Intent
        currentTeam = (Team) getIntent().getSerializableExtra("TEAM");
        if (currentTeam == null || currentTeam.idTeam == null || currentTeam.idTeam.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Ошибка: данные команды не получены", Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        teamId = currentTeam.idTeam;
        viewModel = new ViewModelProvider(this).get(TeamsViewModel.class);

        // Сразу отображаем данные команды
        displayTeamDetails(currentTeam);

        // Настройка RecyclerView для игроков
        setupPlayersRecyclerView();

        // Отображаем игроков (если они есть в объекте команды)
        displayTeamPlayers(currentTeam.keyPlayers);

        // Наблюдатель за данными команды из базы (для избранного)
        setupDatabaseObserver();

        setupButtons();
    }

    private void setupPlayersRecyclerView() {
        RecyclerView playersRecyclerView = findViewById(R.id.playersRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };

        playersRecyclerView.setLayoutManager(layoutManager);

        // Обработка кликов на игроков - показываем диалог
        playersAdapter = new PlayersAdapter(new PlayersAdapter.OnPlayerClickListener() {
            @Override
            public void onPlayerClick(Player player) {
                // Показываем диалог с деталями игрока
                showPlayerDialog(player);
            }
        });
        playersRecyclerView.setAdapter(playersAdapter);

        playersRecyclerView.setNestedScrollingEnabled(true);
    }

    // Метод для показа диалога с игроком
    private void showPlayerDialog(Player player) {
        PlayerDialogFragment dialog = PlayerDialogFragment.newInstance(player);
        dialog.show(getSupportFragmentManager(), "player_dialog");
    }

    private void setupDatabaseObserver() {
        // Наблюдаем за изменениями избранного в базе данных
        viewModel.getTeamByIdFromDb(teamId).observe(this, teamFromDb -> {
            if (teamFromDb != null && currentTeam != null) {
                // Обновляем данные из базы
                currentTeam.comment = teamFromDb.comment;
                currentTeam.rating = teamFromDb.rating;
                currentTeam.isFavorite = teamFromDb.isFavorite;
                updateFavoriteButton();
                updateCommentFields();
            }
        });
    }

    private void setupButtons() {
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

        Button saveCommentButton = findViewById(R.id.saveCommentButton);
        saveCommentButton.setOnClickListener(v -> {
            EditText commentEditText = findViewById(R.id.commentEditText);
            RatingBar ratingBar = findViewById(R.id.ratingBar);

            String comment = commentEditText.getText().toString();
            float rating = ratingBar.getRating();

            viewModel.updateTeamComment(teamId, comment, rating);
            Snackbar.make(v, "Комментарий сохранен", Snackbar.LENGTH_SHORT).show();
        });

        // Кнопка показать/скрыть игроков
        Button togglePlayersButton = findViewById(R.id.togglePlayersButton);
        LinearLayout playersSection = findViewById(R.id.playersSection);

        togglePlayersButton.setOnClickListener(v -> {
            if (playersSection.getVisibility() == View.VISIBLE) {
                playersSection.setVisibility(View.GONE);
                togglePlayersButton.setText("Показать состав");
            } else {
                playersSection.setVisibility(View.VISIBLE);
                togglePlayersButton.setText("Скрыть состав");
            }
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

        name.setText(team.strTeam != null ? team.strTeam : "Нет названия");
        league.setText(team.strLeague != null ? team.strLeague : "Лига не указана");
        country.setText(team.strCountry != null ? "Страна: " + team.strCountry : "");
        stadium.setText(team.strStadium != null ? "Стадион: " + team.strStadium : "");
        year.setText(team.intFormedYear != null ? "Основан: " + team.intFormedYear : "");
        description.setText(team.strDescriptionEN != null ? team.strDescriptionEN : "Описание отсутствует");

        if (team.strBadge != null && !team.strBadge.isEmpty()) {
            Glide.with(this)
                    .load(team.strBadge)
                    .placeholder(R.drawable.ic_soccer)
                    .into(badge);
        } else {
            badge.setImageResource(R.drawable.ic_soccer);
        }

        updateFavoriteButton();
        updateCommentFields();
    }

    private void displayTeamPlayers(List<Player> players) {
        LinearLayout playersSection = findViewById(R.id.playersSection);
        TextView playersTitle = findViewById(R.id.playersTitle);
        Button togglePlayersButton = findViewById(R.id.togglePlayersButton);

        if (players != null && !players.isEmpty()) {
            playersTitle.setText("Состав команды (" + players.size() + " игроков)");
            playersAdapter.setPlayers(players);
            togglePlayersButton.setVisibility(View.VISIBLE);
        } else {
            playersTitle.setText("Информация об игроках отсутствует");
            togglePlayersButton.setVisibility(View.GONE);
            playersSection.setVisibility(View.GONE);
        }
    }

    private void updateFavoriteButton() {
        Button favoriteButton = findViewById(R.id.favoriteButton);
        if (currentTeam != null) {
            favoriteButton.setText(currentTeam.isFavorite ?
                    "Удалить из избранного" : "В избранное");
        }
    }

    private void updateCommentFields() {
        if (currentTeam != null) {
            EditText commentEditText = findViewById(R.id.commentEditText);
            RatingBar ratingBar = findViewById(R.id.ratingBar);
            commentEditText.setText(currentTeam.comment != null ? currentTeam.comment : "");
            ratingBar.setRating(currentTeam.rating);
        }
    }

    // ★ ДОБАВЛЯЕМ МЕНЮ ДЛЯ ПЕРЕХОДА В ИЗБРАННЫЕ ★
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.team_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorites) {
            // Переход в избранные
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Метод для старта активити с передачей команды
    public static void start(AppCompatActivity activity, Team team) {
        Intent intent = new Intent(activity, TeamDetailActivity.class);
        intent.putExtra("TEAM", team);
        activity.startActivity(intent);
    }
}