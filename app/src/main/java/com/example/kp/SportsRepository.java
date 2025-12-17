package com.example.kp;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SportsRepository {
    private final TeamDao teamDao;
    private final LiveData<List<Team>> favoriteTeams;
    private final ApiService apiService;
    private static final String TAG = "SportsRepository";

    // Список лиг
    private final Map<String, String> leagueDisplayNames = new HashMap<String, String>() {{
        put("English Premier League", "Английская Премьер-лига");
        put("Spanish La Liga", "Испанская Ла Лига");
        put("German Bundesliga", "Немецкая Бундеслига");
        put("Italian Serie A", "Итальянская Серия А");
        put("French Ligue 1", "Французская Лига 1");
    }};

    public SportsRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        teamDao = db.teamDao();
        favoriteTeams = teamDao.getFavoriteTeams();

        // Инициализация Retrofit
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        android.util.Log.d(TAG, "Репозиторий инициализирован");
        android.util.Log.d(TAG, "Доступно лиг: " + leagueDisplayNames.size());
    }

    // --- Работа с избранным ---
    public void addToFavorites(Team team) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Team existing = teamDao.getTeamById(team.idTeam);
                if (existing == null) {
                    team.isFavorite = true;
                    teamDao.insert(team);
                    android.util.Log.d(TAG, "Команда добавлена: " + team.strTeam);
                } else {
                    existing.isFavorite = true;
                    teamDao.update(existing);
                    android.util.Log.d(TAG, "Команда обновлена: " + team.strTeam);
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Ошибка добавления в избранное: " + e.getMessage());
            }
        });
    }

    public void removeFromFavorites(String teamId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                teamDao.deleteById(teamId);
                android.util.Log.d(TAG, "Команда удалена: " + teamId);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Ошибка удаления из избранного: " + e.getMessage());
            }
        });
    }

    public void updateTeamComment(String teamId, String comment, float rating) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Team team = teamDao.getTeamById(teamId);
                if (team != null) {
                    team.comment = comment;
                    team.rating = rating;
                    teamDao.update(team);
                    android.util.Log.d(TAG, "Комментарий обновлен для: " + teamId);
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Ошибка обновления комментария: " + e.getMessage());
            }
        });
    }

    public LiveData<List<Team>> getFavoriteTeams() {
        return favoriteTeams;
    }

    public LiveData<Team> getTeamByIdFromDb(String teamId) {
        MutableLiveData<Team> liveData = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Team team = teamDao.getTeamById(teamId);
                liveData.postValue(team);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Ошибка получения команды из БД: " + e.getMessage());
                liveData.postValue(null);
            }
        });
        return liveData;
    }

    // --- Работа с API ---

    // Получить все доступные лиги
    public List<String> getAvailableLeagues() {
        return new ArrayList<>(leagueDisplayNames.keySet());
    }

    // Получить отображаемое название лиги
    public String getLeagueDisplayName(String leagueKey) {
        return leagueDisplayNames.getOrDefault(leagueKey, leagueKey);
    }

    // Основной метод получения команд по лиге - ТОЛЬКО API
    public LiveData<Resource<List<Team>>> getTeamsByLeague(String leagueName) {
        MutableLiveData<Resource<List<Team>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        android.util.Log.d(TAG, "Запрос команд для лиги: " + leagueName);

        if (leagueName == null || leagueName.isEmpty()) {
            result.setValue(Resource.error("Название лиги не указано", null));
            return result;
        }

        // Проверяем, поддерживается ли лига
        if (!leagueDisplayNames.containsKey(leagueName)) {
            android.util.Log.w(TAG, "Лига не найдена в списке: " + leagueName);
            result.setValue(Resource.error("Лига не поддерживается: " + leagueName, null));
            return result;
        }

        // ★ ВАЖНОЕ ИЗМЕНЕНИЕ: ДЛЯ ВСЕХ ЛИГ ИСПОЛЬЗУЕМ API ★
        Call<TeamResponse> call = apiService.getTeams(leagueName);
        call.enqueue(new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                android.util.Log.d(TAG, "Ответ на запрос команд. Код: " + response.code());
                android.util.Log.d(TAG, "URL запроса: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    List<Team> teams = response.body().teams;
                    android.util.Log.d(TAG, "Получено команд: " + (teams != null ? teams.size() : 0));

                    if (teams != null && !teams.isEmpty()) {
                        // Логируем полученные команды для отладки
                        for (int i = 0; i < Math.min(3, teams.size()); i++) {
                            Team team = teams.get(i);
                            android.util.Log.d(TAG, "Команда " + (i+1) + ": " +
                                    team.strTeam + " (игроков: " +
                                    (team.keyPlayers != null ? team.keyPlayers.size() : 0) + ")");
                        }

                        result.setValue(Resource.success(teams));
                    } else {
                        android.util.Log.w(TAG, "Пустой список команд для лиги: " + leagueName);
                        result.setValue(Resource.error("Нет команд в лиге: " + leagueName, null));
                    }
                } else {
                    String errorMsg = "Ошибка API: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg += " - не удалось прочитать ошибку";
                        }
                    }
                    android.util.Log.e(TAG, errorMsg);
                    result.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                String errorMsg = "Ошибка сети при запросе команд: " + t.getMessage();
                android.util.Log.e(TAG, errorMsg, t);
                result.setValue(Resource.error(errorMsg, null));
            }
        });

        return result;
    }

    // Метод для получения игроков команды (остается для совместимости)
    public LiveData<Resource<List<Player>>> getTeamPlayers(String teamId) {
        MutableLiveData<Resource<List<Player>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        android.util.Log.d(TAG, "Запрос игроков команды: " + teamId);

        if (teamId == null || teamId.isEmpty()) {
            result.setValue(Resource.error("ID команды не указан", null));
            return result;
        }

        Call<PlayerResponse> call = apiService.getTeamPlayers(teamId);
        call.enqueue(new Callback<PlayerResponse>() {
            @Override
            public void onResponse(Call<PlayerResponse> call, Response<PlayerResponse> response) {
                android.util.Log.d(TAG, "Ответ на запрос игроков. Код: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Player> players = response.body().player;
                    android.util.Log.d(TAG, "Получено игроков: " + (players != null ? players.size() : 0));

                    if (players != null && !players.isEmpty()) {
                        result.setValue(Resource.success(players));
                    } else {
                        android.util.Log.w(TAG, "Пустой список игроков");
                        result.setValue(Resource.error("Нет информации об игроках", null));
                    }
                } else {
                    String errorMsg = "Ошибка API: " + response.code() + " - " + response.message();
                    android.util.Log.e(TAG, errorMsg);
                    result.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<PlayerResponse> call, Throwable t) {
                String errorMsg = "Ошибка сети: " + t.getMessage();
                android.util.Log.e(TAG, errorMsg, t);
                result.setValue(Resource.error(errorMsg, null));
            }
        });

        return result;
    }

    // ★ УДАЛЯЕМ метод createItalianTeams() - он больше не нужен ★
    // ★ УДАЛЯЕМ поле private List<Team> italianTeams - оно больше не нужно ★
}