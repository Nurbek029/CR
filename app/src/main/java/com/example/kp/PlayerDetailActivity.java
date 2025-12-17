package com.example.kp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class PlayerDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_player_detail);

        Player player = (Player) getIntent().getSerializableExtra("PLAYER");
        if (player == null) {
            finish();
            return;
        }

        displayPlayerDetails(player);
    }

    private void displayPlayerDetails(Player player) {
        TextView name = findViewById(R.id.playerName);
        TextView position = findViewById(R.id.playerPosition);
        TextView number = findViewById(R.id.playerNumber);
        TextView nationality = findViewById(R.id.dialogPlayerNationality);
        TextView birthDate = findViewById(R.id.dialogPlayerBirthDate);
        TextView height = findViewById(R.id.dialogPlayerHeight);
        TextView weight = findViewById(R.id.dialogPlayerWeight);
        TextView description = findViewById(R.id.dialogPlayerDescription);
        ImageView photo = findViewById(R.id.dialogPlayerPhoto);
        ImageView cutout = findViewById(R.id.dialogPlayerCutout);

        name.setText(player.strPlayer != null ? player.strPlayer : "Имя не указано");
        position.setText(player.strPosition != null ? player.strPosition : "Позиция не указана");
        number.setText(player.strNumber != null ? "Номер: " + player.strNumber : "Номер: -");
        nationality.setText(player.strNationality != null ? "Национальность: " + player.strNationality : "Национальность: не указана");
        birthDate.setText(player.dateBorn != null ? "Дата рождения: " + player.dateBorn : "Дата рождения: не указана");
        height.setText(player.strHeight != null ? "Рост: " + player.strHeight : "Рост: не указан");
        weight.setText(player.strWeight != null ? "Вес: " + player.strWeight : "Вес: не указан");
        description.setText(player.strDescriptionEN != null ? player.strDescriptionEN : "Описание отсутствует");

        // Загрузка фото с Glide
        if (player.strThumb != null && !player.strThumb.isEmpty()) {
            Glide.with(this)
                    .load(player.strThumb)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(photo);
        } else {
            photo.setImageResource(R.drawable.ic_person);
        }

        // Загрузка большого фото (cutout)
        if (player.strCutout != null && !player.strCutout.isEmpty()) {
            Glide.with(this)
                    .load(player.strCutout)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(cutout);
        } else {
            cutout.setImageResource(R.drawable.ic_person);
        }
    }

    public static void start(AppCompatActivity activity, Player player) {
        Intent intent = new Intent(activity, PlayerDetailActivity.class);
        intent.putExtra("PLAYER", player);
        activity.startActivity(intent);
    }
}