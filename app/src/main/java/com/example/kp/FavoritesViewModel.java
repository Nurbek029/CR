// C:/CR/app/src/main/java/com/example/kp/FavoritesViewModel.java
package com.example.kp;import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class FavoritesViewModel extends AndroidViewModel {
    private final SportsRepository repository;
    private final LiveData<List<Team>> favoriteTeams;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        repository = new SportsRepository(application);
        favoriteTeams = repository.getFavoriteTeams();
    }

    public LiveData<List<Team>> getFavoriteTeams() {
        return favoriteTeams;
    }

    // ИЗМЕНЕНИЕ 3: ViewModel больше не работает с базой данных напрямую.
    // Он просто вызывает метод репозитория. Это правильное разделение ответственности.
    public void updateCommentAndRating(String teamId, String comment, float rating) {
        repository.updateTeamComment(teamId, comment, rating);
    }
}
