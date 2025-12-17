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
import java.util.List;

public class TeamDetailActivity extends AppCompatActivity {
    private TeamsViewModel viewModel;
    private String teamId;
    private Team currentTeam;
    private PlayersAdapter playersAdapter;
    private EditText commentEditText;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        // Получаем команду из Intent
        currentTeam = (Team) getIntent().getSerializableExtra("TEAM");
        if (currentTeam == null || currentTeam.idTeam == null || currentTeam.idTeam.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Ошибка: данные команды не получены", Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        teamId = currentTeam.idTeam;
        viewModel = new ViewModelProvider(this).get(TeamsViewModel.class);

        // Инициализация полей ввода
        commentEditText = findViewById(R.id.commentEditText);
        ratingBar = findViewById(R.id.ratingBar);

        // Отображаем данные команды
        displayTeamDetails(currentTeam);

        // Настройка RecyclerView для игроков
        setupPlayersRecyclerView();

        // Отображаем игроков
        displayTeamPlayers(currentTeam.keyPlayers);

        // Наблюдатель за данными команды из базы
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

        playersAdapter = new PlayersAdapter(new PlayersAdapter.OnPlayerClickListener() {
            @Override
            public void onPlayerClick(Player player) {
                showPlayerDialog(player);
            }
        });
        playersRecyclerView.setAdapter(playersAdapter);

        playersRecyclerView.setNestedScrollingEnabled(true);
    }

    private void showPlayerDialog(Player player) {
        PlayerDialogFragment dialog = PlayerDialogFragment.newInstance(player);
        dialog.show(getSupportFragmentManager(), "player_dialog");
    }

    private void setupDatabaseObserver() {
        viewModel.getTeamByIdFromDb(teamId).observe(this, teamFromDb -> {
            if (teamFromDb != null && currentTeam != null) {
                // Обновляем текущую команду данными из БД
                currentTeam.comment = teamFromDb.comment;
                currentTeam.rating = teamFromDb.rating;
                currentTeam.isFavorite = teamFromDb.isFavorite;

                // Обновляем поля ввода
                updateCommentFields();

                // Обновляем кнопку
                updateFavoriteButton();

                android.util.Log.d("TeamDetailActivity",
                        "Данные из БД: isFavorite=" + currentTeam.isFavorite +
                                ", rating=" + currentTeam.rating);
            }
        });
    }

    private void setupButtons() {
        Button favoriteButton = findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(v -> {
            if (currentTeam != null) {
                if (!currentTeam.isFavorite) {
                    // Получаем данные из полей ввода
                    String comment = commentEditText.getText().toString().trim();
                    float rating = ratingBar.getRating();

                    // Проверяем заполненность
                    if (comment.isEmpty()) {
                        Snackbar.make(v,
                                "Пожалуйста, напишите комментарий",
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    if (rating <= 0) {
                        Snackbar.make(v,
                                "Пожалуйста, поставьте оценку",
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    // Обновляем данные команды
                    currentTeam.comment = comment;
                    currentTeam.rating = rating;

                    // Добавляем в избранное
                    viewModel.addToFavorites(currentTeam);
                    Snackbar.make(v, "Добавлено в избранное ✓", Snackbar.LENGTH_SHORT).show();

                    // Обновляем локальный статус
                    currentTeam.isFavorite = true;

                } else {
                    // Удаляем из избранного
                    viewModel.removeFromFavorites(currentTeam.idTeam);
                    Snackbar.make(v, "Удалено из избранного", Snackbar.LENGTH_SHORT).show();

                    // Очищаем поля
                    currentTeam.comment = "";
                    currentTeam.rating = 0;
                    currentTeam.isFavorite = false;

                    // Обновляем поля ввода
                    updateCommentFields();
                }

                updateFavoriteButton();
            }
        });

        // ★ УДАЛЯЕМ кнопку "Сохранить комментарий" - теперь не нужна ★
        // Вместо нее можно добавить кнопку скрытия игроков

        Button togglePlayersButton = findViewById(R.id.togglePlayersButton);
        LinearLayout playersSection = findViewById(R.id.playersSection);

        if (togglePlayersButton != null) {
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
            // Преобразуем SVG URL в PNG URL для Wikimedia
            String imageUrl = convertSvgToPngUrl(team.strBadge);

            // Используем Glide для загрузки изображения
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_soccer)
                    .error(R.drawable.ic_soccer)
                    .into(badge);
        } else {
            badge.setImageResource(R.drawable.ic_soccer);
        }

        updateFavoriteButton();
        updateCommentFields();
    }

    private String convertSvgToPngUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }

        // Если это SVG из Wikimedia, конвертируем в PNG
        if (originalUrl.contains("wikimedia.org") && originalUrl.endsWith(".svg")) {
            // Пример: https://upload.wikimedia.org/wikipedia/commons/0/05/FC_Internazionale_Milano_2021.svg
            // Конвертируем в PNG версию
            String pngUrl = originalUrl.replace(".svg", ".png");

            // Альтернативный вариант для Wikimedia
            if (pngUrl.contains("/commons/")) {
                pngUrl = pngUrl.replace("/commons/", "/commons/thumb/");
                // Добавляем размер
                int lastSlash = pngUrl.lastIndexOf("/");
                if (lastSlash != -1) {
                    String fileName = pngUrl.substring(lastSlash + 1);
                    pngUrl = pngUrl.replace(fileName, "512px-" + fileName);
                }
            }

            android.util.Log.d("TeamDetailActivity", "Конвертирован URL: " + originalUrl + " -> " + pngUrl);
            return pngUrl;
        }

        return originalUrl;
    }

    private void displayTeamPlayers(List<Player> players) {
        LinearLayout playersSection = findViewById(R.id.playersSection);
        TextView playersTitle = findViewById(R.id.playersTitle);
        Button togglePlayersButton = findViewById(R.id.togglePlayersButton);

        if (players != null && !players.isEmpty()) {
            playersTitle.setText("Состав команды (" + players.size() + " игроков)");
            playersAdapter.setPlayers(players);
            if (togglePlayersButton != null) {
                togglePlayersButton.setVisibility(View.VISIBLE);
            }
        } else {
            playersTitle.setText("Информация об игроках отсутствует");
            if (togglePlayersButton != null) {
                togglePlayersButton.setVisibility(View.GONE);
            }
            playersSection.setVisibility(View.GONE);
        }
    }

    private void updateFavoriteButton() {
        Button favoriteButton = findViewById(R.id.favoriteButton);
        if (currentTeam != null) {
            if (currentTeam.isFavorite) {
                favoriteButton.setText("Удалить из избранного");
                favoriteButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            } else {
                favoriteButton.setText("Добавить в избранное");
                favoriteButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            }
        }
    }

    private void updateCommentFields() {
        if (currentTeam != null) {
            commentEditText.setText(currentTeam.comment != null ? currentTeam.comment : "");
            ratingBar.setRating(currentTeam.rating > 0 ? currentTeam.rating : 0);

            // Подсказки
            if (currentTeam.comment == null || currentTeam.comment.isEmpty()) {
                commentEditText.setHint("Напишите комментарий (обязательно для добавления в избранное)");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.team_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void start(AppCompatActivity activity, Team team) {
        Intent intent = new Intent(activity, TeamDetailActivity.class);
        intent.putExtra("TEAM", team);
        activity.startActivity(intent);
    }
}