package com.example.kp.ui;

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
import com.example.kp.R;
import com.example.kp.entities.Team;

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
                            oldItem.isFavorite == newItem.isFavorite &&
                            oldItem.rating == newItem.rating &&
                            (oldItem.comment == null ? newItem.comment == null :
                                    oldItem.comment.equals(newItem.comment));
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
        Team team = getItem(position);
        holder.bind(team, listener);
    }

    public void setTeams(List<Team> teams) {
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

            // ⭐ ВАЖНОЕ ИЗМЕНЕНИЕ: убираем .placeholder() при успешной загрузке
            int badgeResId = getTeamBadgeResource(team.idTeam);
            if (badgeResId != R.drawable.ic_soccer) {
                // Если есть реальное изображение - загружаем БЕЗ placeholder
                Glide.with(itemView.getContext())
                        .load(badgeResId)
                        .error(R.drawable.ic_soccer) // Только error, без placeholder
                        .into(teamBadge);
            } else {
                // Если нет изображения - показываем заглушку
                teamBadge.setImageResource(R.drawable.ic_soccer);
            }

            if (team.isFavorite) {
                favoriteIcon.setImageResource(R.drawable.ic_favorite_filled);
                favoriteIcon.setContentDescription("В избранном");
            } else {
                favoriteIcon.setImageResource(R.drawable.ic_favorite_border);
                favoriteIcon.setContentDescription("Добавить в избранное");
            }

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

        // ⭐ ОБНОВЛЕННЫЙ МЕТОД ДЛЯ ВСЕХ ЛИГ
        private int getTeamBadgeResource(String teamId) {
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
    }
}