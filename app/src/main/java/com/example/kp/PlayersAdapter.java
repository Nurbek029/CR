package com.example.kp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import java.util.ArrayList;
import java.util.List;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder> {
    private List<Player> players = new ArrayList<>();
    private OnPlayerClickListener listener;

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
    }

    public PlayersAdapter(OnPlayerClickListener listener) {
        this.listener = listener;
    }

    public void setPlayers(List<Player> players) {
        this.players = players != null ? players : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player, listener);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        ImageView playerImage;
        TextView playerName;
        TextView playerPosition;
        TextView playerNumber;

        public PlayerViewHolder(View itemView) {
            super(itemView);
            playerImage = itemView.findViewById(R.id.playerImage);
            playerName = itemView.findViewById(R.id.playerName);
            playerPosition = itemView.findViewById(R.id.playerPosition);
            playerNumber = itemView.findViewById(R.id.playerNumber);
        }

        public void bind(Player player, OnPlayerClickListener listener) {
            playerName.setText(player.strPlayer != null ? player.strPlayer : "");
            playerPosition.setText(player.strPosition != null ? player.strPosition : "");
            playerNumber.setText(player.strNumber != null ? "#" + player.strNumber : "#0");

            if (player.strThumb != null && !player.strThumb.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(player.strThumb)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(playerImage);
            } else {
                playerImage.setImageResource(R.drawable.ic_person);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayerClick(player);
                }
            });
        }
    }
}