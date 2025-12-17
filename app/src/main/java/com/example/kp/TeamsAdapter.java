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

            if (team.strBadge != null && !team.strBadge.isEmpty()) {
                // Конвертируем SVG URL в PNG URL если нужно
                String imageUrl = convertSvgToPngUrl(team.strBadge);

                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.ic_soccer)
                        .error(R.drawable.ic_soccer)
                        .into(teamBadge);
            } else {
                teamBadge.setImageResource(R.drawable.ic_soccer);
            }

            // ★ ИСПРАВЛЕНА СИНХРОНИЗАЦИЯ ИКОНКИ ★
            if (team.isFavorite) {
                // Заполненное сердечко или звезда
                favoriteIcon.setImageResource(R.drawable.ic_favorite_filled);
                favoriteIcon.setContentDescription("В избранном");
            } else {
                // Пустое сердечко или звезда
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

        private String convertSvgToPngUrl(String originalUrl) {
            if (originalUrl == null || originalUrl.isEmpty()) {
                return originalUrl;
            }

            // Если это SVG из Wikimedia, конвертируем в PNG
            if (originalUrl.contains("wikimedia.org") && originalUrl.toLowerCase().endsWith(".svg")) {
                // Просто заменяем .svg на .png
                String pngUrl = originalUrl.replace(".svg", ".png");

                // Для Wikimedia можно использовать более надежный метод
                if (pngUrl.contains("/commons/")) {
                    try {
                        // Формат для Wikimedia: /commons/thumb/.../512px-...
                        pngUrl = pngUrl.replace("/commons/", "/commons/thumb/");

                        // Добавляем размер
                        int lastSlash = pngUrl.lastIndexOf("/");
                        if (lastSlash != -1) {
                            String fileName = pngUrl.substring(lastSlash + 1);
                            if (fileName.startsWith("FC_") || fileName.startsWith("File:")) {
                                pngUrl = pngUrl.substring(0, lastSlash + 1) + "512px-" + fileName;
                            }
                        }
                    } catch (Exception e) {
                        // Если что-то пошло не так, используем простую замену
                        pngUrl = originalUrl.replace(".svg", ".png");
                    }
                }

                android.util.Log.d("TeamsAdapter", "Конвертирован URL SVG -> PNG: " + originalUrl + " -> " + pngUrl);
                return pngUrl;
            }

            // Если это другой SVG источник, тоже попробуем заменить на PNG
            if (originalUrl.toLowerCase().endsWith(".svg")) {
                String pngUrl = originalUrl.replace(".svg", ".png");
                android.util.Log.d("TeamsAdapter", "Конвертирован общий SVG -> PNG: " + originalUrl + " -> " + pngUrl);
                return pngUrl;
            }

            return originalUrl;
        }
    }
}