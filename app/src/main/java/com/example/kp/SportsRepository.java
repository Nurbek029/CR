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

    // Карта лиг и их отображаемых названий
    private final Map<String, String> leagueDisplayNames = new HashMap<String, String>() {{
        put("English Premier League", "Английская Премьер-лига");
        put("Spanish La Liga", "Испанская Ла Лига");
        put("German Bundesliga", "Немецкая Бундеслига");
        put("Italian Serie A", "Итальянская Серия А");
        put("French Ligue 1", "Французская Лига 1");
        put("Dutch Eredivisie", "Нидерландская Эредивизи");
        put("Portuguese Primeira Liga", "Португальская Примейра Лига");
        put("Brazilian Serie A", "Бразильская Серия А");
        put("Argentine Primera Division", "Аргентинская Примера Дивисьон");
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

    // --- Работа с избранным (остается без изменений) ---
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

    // Основной метод получения команд по лиге
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

        Call<TeamResponse> call = apiService.getTeams(leagueName);
        call.enqueue(new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                android.util.Log.d(TAG, "Ответ на запрос команд. Код: " + response.code());
                android.util.Log.d(TAG, "URL запроса: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    List<Team> teams = response.body().teams;
                    android.util.Log.d(TAG, "Получено команд: " + (teams != null ? teams.size() : 0));

                    // Детальный лог первых 3 команд (если есть)
                    if (teams != null && !teams.isEmpty()) {
                        for (int i = 0; i < Math.min(3, teams.size()); i++) {
                            Team team = teams.get(i);
                            android.util.Log.d(TAG, "Команда " + (i+1) + ": " +
                                    team.strTeam + " (ID: " + team.idTeam + ")");
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

    // Метод для получения команд из всех лиг
    public LiveData<Resource<Map<String, List<Team>>>> getAllTeamsFromAllLeagues() {
        MutableLiveData<Resource<Map<String, List<Team>>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        android.util.Log.d(TAG, "Запрос команд из всех лиг");

        // Создаем мапу для результатов
        Map<String, List<Team>> allTeams = new HashMap<>();
        final int[] completedRequests = {0};
        final int totalRequests = leagueDisplayNames.size();

        for (String league : leagueDisplayNames.keySet()) {
            Call<TeamResponse> call = apiService.getTeams(league);
            call.enqueue(new Callback<TeamResponse>() {
                @Override
                public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().teams != null) {
                        String leagueName = call.request().url().queryParameter("l");
                        allTeams.put(leagueName, response.body().teams);
                        android.util.Log.d(TAG, "Загружены команды для лиги: " + leagueName +
                                " (" + response.body().teams.size() + " команд)");
                    }

                    completedRequests[0]++;
                    if (completedRequests[0] >= totalRequests) {
                        if (!allTeams.isEmpty()) {
                            result.setValue(Resource.success(allTeams));
                        } else {
                            result.setValue(Resource.error("Не удалось загрузить команды из лиг", null));
                        }
                    }
                }

                @Override
                public void onFailure(Call<TeamResponse> call, Throwable t) {
                    android.util.Log.e(TAG, "Ошибка загрузки лиги: " + call.request().url(), t);
                    completedRequests[0]++;
                    if (completedRequests[0] >= totalRequests) {
                        if (!allTeams.isEmpty()) {
                            result.setValue(Resource.success(allTeams));
                        } else {
                            result.setValue(Resource.error("Ошибка загрузки команд", null));
                        }
                    }
                }
            });
        }

        return result;
    }

    // Метод для поиска команд по названию во всех лигах
    public LiveData<Resource<List<Team>>> searchTeamsGlobally(String query) {
        MutableLiveData<Resource<List<Team>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (query == null || query.trim().isEmpty()) {
            result.setValue(Resource.error("Пустой запрос поиска", null));
            return result;
        }

        android.util.Log.d(TAG, "Глобальный поиск команд: " + query);

        // Здесь можно реализовать поиск по всем лигам
        // Пока возвращаем пустой результат, можно расширить функционал
        result.setValue(Resource.success(new ArrayList<>()));

        return result;
    }

    // Старый метод получения лиг (если нужен)
    public LiveData<Resource<List<League>>> getLeagues() {
        MutableLiveData<Resource<List<League>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        android.util.Log.d(TAG, "Запрос всех лиг");

        // Создаем список лиг из нашей карты
        new Thread(() -> {
            try {
                List<League> leagues = new ArrayList<>();
                for (Map.Entry<String, String> entry : leagueDisplayNames.entrySet()) {
                    League league = new League();
                    // Генерируем ID на основе названия
                    league.idLeague = String.valueOf(entry.getKey().hashCode());
                    league.strLeague = entry.getKey();
                    league.strSport = "Soccer";
                    leagues.add(league);
                }

                Thread.sleep(500); // Имитация загрузки
                android.util.Log.d(TAG, "Создано лиг: " + leagues.size());
                result.postValue(Resource.success(leagues));

            } catch (Exception e) {
                android.util.Log.e(TAG, "Ошибка создания списка лиг: " + e.getMessage());
                result.postValue(Resource.error("Ошибка загрузки лиг: " + e.getMessage(), null));
            }
        }).start();

        return result;
    }

    // Метод для получения игроков команды (остается без изменений)
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
}