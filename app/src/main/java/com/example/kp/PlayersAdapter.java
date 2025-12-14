// PlayersAdapter.java
package com.example.kp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayerViewHolder> {
    private List<Player> players;

    public PlayersAdapter(List<Player> players) {
        this.players = players;
    }

    @Override
    public PlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlayerViewHolder holder, int position) {
        Player player = players.get(position);

        holder.playerName.setText(player.strPlayer);
        holder.playerPosition.setText(player.strPosition);
        holder.playerNumber.setText(player.strNumber);

        // Загрузка фото игрока
        if (player.strThumb != null && !player.strThumb.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(player.strThumb)
                    .placeholder(R.drawable.ic_person)
                    .into(holder.playerImage);
        }
    }

    @Override
    public int getItemCount() {
        return players != null ? players.size() : 0;
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
    }
}