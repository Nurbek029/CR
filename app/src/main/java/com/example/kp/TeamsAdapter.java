// TeamsAdapter.java
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
import java.util.ArrayList;
import java.util.List;

public class TeamsAdapter extends ListAdapter<Team, TeamsAdapter.TeamViewHolder> {
    private OnTeamClickListener listener;
    private List<Team> teams = new ArrayList<>();

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
                            oldItem.comment.equals(newItem.comment);
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

    @Override
    public int getItemCount() {
        return teams != null ? teams.size() : 0;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams != null ? teams : new ArrayList<>();
        submitList(new ArrayList<>(this.teams)); // Создаем копию для ListAdapter
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
            teamName.setText(team.strTeam);
            teamLeague.setText(team.strLeague);
            teamCountry.setText(team.strCountry);
            teamSport.setText(team.strSport);

            if (team.strBadge != null && !team.strBadge.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(team.strBadge)
                        .into(teamBadge);
            }

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