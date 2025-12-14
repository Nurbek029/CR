package com.example.kp;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeamsViewModel extends AndroidViewModel {
    private SportsRepository repository;
    private LiveData<List<Team>> favoriteTeams;
    private MutableLiveData<String> currentLeague = new MutableLiveData<>("English Premier League");

    private LiveData<Resource<List<League>>> leagues;
    private LiveData<Resource<List<Team>>> teams;
    private MutableLiveData<List<Team>> filteredTeams = new MutableLiveData<>();
    private MutableLiveData<Resource<List<Player>>> players = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Для хранения команд из всех лиг
    private MutableLiveData<Map<String, List<Team>>> allTeamsByLeague = new MutableLiveData<>();
    private List<String> availableLeagues = new ArrayList<>();

    public TeamsViewModel(Application application) {
        super(application);
        repository = new SportsRepository(application);
        favoriteTeams = repository.getFavoriteTeams();

        // Получаем список доступных лиг
        availableLeagues = repository.getAvailableLeagues();
        android.util.Log.d("TeamsViewModel", "Доступно лиг: " + availableLeagues.size());

        // Инициализация преобразованных LiveData
        teams = Transformations.switchMap(currentLeague, league -> {
            if (league != null && !league.isEmpty()) {
                isLoading.setValue(true);
                return repository.getTeamsByLeague(league);
            }
            return new MutableLiveData<>();
        });

        // Наблюдатель за командами
        teams.observeForever(resource -> {
            if (resource != null) {
                isLoading.setValue(false);
                switch (resource.status) {
                    case SUCCESS:
                        if (resource.data != null) {
                            filteredTeams.setValue(resource.data);
                            errorMessage.setValue(null);
                        }
                        break;
                    case ERROR:
                        errorMessage.setValue(resource.message);
                        filteredTeams.setValue(null);
                        break;
                    case LOADING:
                        isLoading.setValue(true);
                        break;
                }
            }
        });

        // Инициализация списка лиг
        leagues = repository.getLeagues();
    }

    // LiveData для UI
    public LiveData<List<Team>> getFavoriteTeams() {
        return favoriteTeams;
    }

    public LiveData<Resource<List<League>>> getLeagues() {
        return leagues;
    }

    public LiveData<Resource<List<Team>>> getTeams() {
        return teams;
    }

    public LiveData<List<Team>> getFilteredTeams() {
        return filteredTeams;
    }

    public LiveData<Resource<List<Player>>> getPlayers() {
        return players;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Map<String, List<Team>>> getAllTeamsByLeague() {
        return allTeamsByLeague;
    }

    public List<String> getAvailableLeagues() {
        return availableLeagues;
    }

    public String getLeagueDisplayName(String leagueKey) {
        return repository.getLeagueDisplayName(leagueKey);
    }

    public LiveData<Team> getTeamByIdFromDb(String teamId) {
        return repository.getTeamByIdFromDb(teamId);
    }

    // Методы для работы с данными
    public void setCurrentLeague(String leagueName) {
        if (leagueName != null && !leagueName.isEmpty()) {
            // Проверяем, что лига есть в списке доступных
            if (availableLeagues.contains(leagueName)) {
                currentLeague.setValue(leagueName);
                android.util.Log.d("TeamsViewModel", "Установлена лига: " + leagueName);
            } else {
                android.util.Log.w("TeamsViewModel", "Лига не найдена: " + leagueName);
                errorMessage.setValue("Лига не найдена: " + leagueName);
            }
        }
    }

    // Загрузка команд из всех лиг
    public void loadAllTeamsFromAllLeagues() {
        isLoading.setValue(true);
        repository.getAllTeamsFromAllLeagues().observeForever(resource -> {
            isLoading.setValue(false);
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                allTeamsByLeague.setValue(resource.data);
                android.util.Log.d("TeamsViewModel", "Загружены команды из " + resource.data.size() + " лиг");
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                errorMessage.setValue(resource.message);
            }
        });
    }

    public void loadTeamPlayers(String teamId) {
        if (teamId == null || teamId.isEmpty()) return;

        repository.getTeamPlayers(teamId).observeForever(resource -> {
            if (resource != null) {
                players.setValue(resource);
            }
        });
    }

    // Поиск и фильтрация
    public void searchTeams(String query) {
        List<Team> currentFiltered = filteredTeams.getValue();
        if (currentFiltered == null) return;

        if (query == null || query.trim().isEmpty()) {
            // Если запрос пустой, показываем все команды текущей лиги
            Resource<List<Team>> currentTeamsResource = teams.getValue();
            if (currentTeamsResource != null && currentTeamsResource.data != null) {
                filteredTeams.setValue(currentTeamsResource.data);
            }
            return;
        }

        String searchQuery = query.toLowerCase().trim();
        List<Team> result = currentFiltered.stream()
                .filter(team ->
                        (team.strTeam != null && team.strTeam.toLowerCase().contains(searchQuery)) ||
                                (team.strLeague != null && team.strLeague.toLowerCase().contains(searchQuery)))
                .collect(java.util.stream.Collectors.toList());

        filteredTeams.setValue(result);
    }

    public void filterByCountry(String country) {
        Resource<List<Team>> currentTeams = teams.getValue();
        if (currentTeams == null || currentTeams.data == null) return;

        List<Team> allTeams = currentTeams.data;

        if (country == null || country.equals("Все страны")) {
            filteredTeams.setValue(allTeams);
            return;
        }

        List<Team> result = allTeams.stream()
                .filter(team -> team.strCountry != null &&
                        team.strCountry.equalsIgnoreCase(country))
                .collect(java.util.stream.Collectors.toList());

        filteredTeams.setValue(result);
    }

    public void resetFilter() {
        Resource<List<Team>> currentTeams = teams.getValue();
        if (currentTeams != null && currentTeams.data != null) {
            filteredTeams.setValue(currentTeams.data);
        }
    }

    // Избранное
    public void addToFavorites(Team team) {
        if (team != null) {
            repository.addToFavorites(team);
        }
    }

    public void removeFromFavorites(String teamId) {
        if (teamId != null && !teamId.isEmpty()) {
            repository.removeFromFavorites(teamId);
        }
    }

    public void updateTeamComment(String teamId, String comment, float rating) {
        if (teamId != null && !teamId.isEmpty()) {
            repository.updateTeamComment(teamId, comment, rating);
        }
    }

    // Перезагрузка данных
    public void reloadTeams() {
        String currentLeagueValue = currentLeague.getValue();
        if (currentLeagueValue != null && !currentLeagueValue.isEmpty()) {
            currentLeague.setValue(currentLeagueValue); // Триггерим перезагрузку
        }
    }

    // Получить следующую лигу (для циклического переключения)
    public void nextLeague() {
        String current = currentLeague.getValue();
        if (current != null && !availableLeagues.isEmpty()) {
            int currentIndex = availableLeagues.indexOf(current);
            int nextIndex = (currentIndex + 1) % availableLeagues.size();
            setCurrentLeague(availableLeagues.get(nextIndex));
        }
    }

    // Получить предыдущую лигу
    public void previousLeague() {
        String current = currentLeague.getValue();
        if (current != null && !availableLeagues.isEmpty()) {
            int currentIndex = availableLeagues.indexOf(current);
            int prevIndex = (currentIndex - 1 + availableLeagues.size()) % availableLeagues.size();
            setCurrentLeague(availableLeagues.get(prevIndex));
        }
    }
}