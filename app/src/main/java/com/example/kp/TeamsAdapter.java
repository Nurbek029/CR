package com.example.kp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import java.util.ArrayList;
import java.util.List;

public class TeamsAdapter extends ListAdapter<Team, TeamsAdapter.TeamViewHolder> {
    private OnTeamClickListener listener;

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
        void onFavoriteClick(Team team, boolean isFavorite);
    }

    public TeamsAdapter(OnTeamClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Team> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Team>() {
                @Override
                public boolean areItemsTheSame(@NonNull Team oldItem, @NonNull Team newItem) {
                    return oldItem.idTeam.equals(newItem.idTeam);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Team oldItem, @NonNull Team newItem) {
                    return oldItem.strTeam.equals(newItem.strTeam) &&
                            oldItem.isFavorite == newItem.isFavorite;
                }
            };

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = getItem(position); // Важно: используем getItem от ListAdapter
        holder.bind(team, listener);
    }

    // УДАЛЕНО: метод getItemCount() - ListAdapter сам управляет размером
    // УДАЛЕНО: поле private List<Team> teams и связанная с ним логика

    public void setTeams(List<Team> teams) {
        // Используем submitList для обновления данных в ListAdapter
        submitList(teams != null ? new ArrayList<>(teams) : new ArrayList<>());
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        ImageView teamBadge;
        ImageView favoriteIcon;
        TextView teamName;
        TextView teamLeague;
        TextView teamCountry;
        TextView teamSport;

        public TeamViewHolder(View itemView) {
            super(itemView);
            teamBadge = itemView.findViewById(R.id.teamBadge);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            teamName = itemView.findViewById(R.id.teamName);
            teamLeague = itemView.findViewById(R.id.teamLeague);
            teamCountry = itemView.findViewById(R.id.teamCountry);
            teamSport = itemView.findViewById(R.id.teamSport);
        }

        public void bind(Team team, OnTeamClickListener listener) {
            teamName.setText(team.strTeam != null ? team.strTeam : "");
            teamLeague.setText(team.strLeague != null ? team.strLeague : "");
            teamCountry.setText(team.strCountry != null ? team.strCountry : "");
            teamSport.setText(team.strSport != null ? team.strSport : "");

            // Исправляем ошибку загрузки изображений
            if (team.strBadge != null && !team.strBadge.isEmpty()) {
                // Используем Glide с обработкой ошибок
                Glide.with(itemView.getContext())
                        .load(team.strBadge)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.ic_soccer)
                        .error(R.drawable.ic_soccer)
                        .into(teamBadge);
            } else {
                teamBadge.setImageResource(R.drawable.ic_soccer);
            }

            // Обновляем иконку избранного
            favoriteIcon.setImageResource(
                    team.isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
            );

            favoriteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(team, !team.isFavorite);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTeamClick(team);
                }
            });
        }
    }
}