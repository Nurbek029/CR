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

        currentTeam = (Team) getIntent().getSerializableExtra("TEAM");
        if (currentTeam == null || currentTeam.idTeam == null || currentTeam.idTeam.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Ошибка: данные команды не получены", Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        teamId = currentTeam.idTeam;
        viewModel = new ViewModelProvider(this).get(TeamsViewModel.class);

        commentEditText = findViewById(R.id.commentEditText);
        ratingBar = findViewById(R.id.ratingBar);

        displayTeamDetails(currentTeam);
        setupPlayersRecyclerView();
        displayTeamPlayers(currentTeam.keyPlayers);
        setupDatabaseObserver();
        setupButtons();
    }

    private void setupPlayersRecyclerView() {
        RecyclerView playersRecyclerView = findViewById(R.id.playersRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override public boolean canScrollVertically() { return true; }
        };
        playersRecyclerView.setLayoutManager(layoutManager);
        playersAdapter = new PlayersAdapter(new PlayersAdapter.OnPlayerClickListener() {
            @Override public void onPlayerClick(Player player) {
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
                currentTeam.comment = teamFromDb.comment;
                currentTeam.rating = teamFromDb.rating;
                currentTeam.isFavorite = teamFromDb.isFavorite;
                updateCommentFields();
                updateFavoriteButton();
            }
        });
    }

    private void setupButtons() {
        Button favoriteButton = findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(v -> {
            if (currentTeam != null) {
                if (!currentTeam.isFavorite) {
                    String comment = commentEditText.getText().toString().trim();
                    float rating = ratingBar.getRating();

                    if (comment.isEmpty()) {
                        Snackbar.make(v, "Пожалуйста, напишите комментарий", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    if (rating <= 0) {
                        Snackbar.make(v, "Пожалуйста, поставьте оценку", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    currentTeam.comment = comment;
                    currentTeam.rating = rating;
                    viewModel.addToFavorites(currentTeam);
                    Snackbar.make(v, "Добавлено в избранное ✓", Snackbar.LENGTH_SHORT).show();
                    currentTeam.isFavorite = true;
                } else {
                    viewModel.removeFromFavorites(currentTeam.idTeam);
                    Snackbar.make(v, "Удалено из избранного", Snackbar.LENGTH_SHORT).show();
                    currentTeam.comment = "";
                    currentTeam.rating = 0;
                    currentTeam.isFavorite = false;
                    updateCommentFields();
                }
                updateFavoriteButton();
            }
        });

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

    // ⭐ КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: загрузка логотипа из ресурсов
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

        // ⭐ ИСПОЛЬЗУЕМ ТОТ ЖЕ МЕТОД, ЧТО И В АДАПТЕРЕ
        int badgeResId = getTeamBadgeResource(team.idTeam);
        if (badgeResId != R.drawable.ic_soccer) {
            // Реальное изображение - загружаем БЕЗ placeholder
            Glide.with(this)
                    .load(badgeResId)
                    .error(R.drawable.ic_soccer) // Только для ошибок
                    .into(badge);
        } else {
            // Нет изображения - показываем заглушку
            badge.setImageResource(R.drawable.ic_soccer);
        }

        updateFavoriteButton();
        updateCommentFields();
    }

    // ⭐ ТОТ ЖЕ МЕТОД, ЧТО И В TeamsAdapter
    private int getTeamBadgeResource(String teamId) {
        if (teamId == null) return R.drawable.ic_soccer;

        switch (teamId) {
            // English Premier League
            case "EPL1": return R.drawable.manchesterunited;
            case "EPL2": return R.drawable.manchestercity;
            case "EPL3": return R.drawable.liverpoolfc;
            case "EPL4": return R.drawable.arsenalfc;
            case "EPL5": return R.drawable.chelseafc;

            // Italian Serie A
            case "SA1": return R.drawable.inter_milan;
            case "SA2": return R.drawable.ac_milan;
            case "SA3": return R.drawable.juventus;
            case "SA4": return R.drawable.as_roma;
            case "SA5": return R.drawable.napoli;

            // Spanish La Liga
            case "LL1": return R.drawable.real_madrid;
            case "LL2": return R.drawable.barcelona;
            case "LL3": return R.drawable.atletico_madrid;
            case "LL4": return R.drawable.sevilla;
            case "LL5": return R.drawable.valencia;

            // German Bundesliga
            case "BL1": return R.drawable.bayern_munich;
            case "BL2": return R.drawable.borussia_dortmund;
            case "BL3": return R.drawable.rb_leipzig;
            case "BL4": return R.drawable.bayer_leverkusen;
            case "BL5": return R.drawable.eintracht_frankfurt;

            // French Ligue 1
            case "FL1": return R.drawable.psg;
            case "FL2": return R.drawable.marseille;
            case "FL3": return R.drawable.lyon;
            case "FL4": return R.drawable.monaco;
            case "FL5": return R.drawable.lille;

            default: return R.drawable.ic_soccer;
        }
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
            startActivity(new Intent(this, FavoritesActivity.class));
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