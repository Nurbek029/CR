package com.example.kp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class PlayerDialogFragment extends DialogFragment {

    private static final String ARG_PLAYER = "player";

    public static PlayerDialogFragment newInstance(Player player) {
        PlayerDialogFragment fragment = new PlayerDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLAYER, player);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_player_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Player player = (Player) getArguments().getSerializable(ARG_PLAYER);
        if (player == null) {
            dismiss();
            return;
        }

        displayPlayerDetails(view, player);

        // Настраиваем размер диалога
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void displayPlayerDetails(View view, Player player) {
        TextView name = view.findViewById(R.id.dialogPlayerName);
        TextView position = view.findViewById(R.id.dialogPlayerPosition);
        TextView number = view.findViewById(R.id.dialogPlayerNumber);
        TextView nationality = view.findViewById(R.id.dialogPlayerNationality);
        TextView birthDate = view.findViewById(R.id.dialogPlayerBirthDate);
        TextView height = view.findViewById(R.id.dialogPlayerHeight);
        TextView weight = view.findViewById(R.id.dialogPlayerWeight);
        TextView description = view.findViewById(R.id.dialogPlayerDescription);
        ImageView photo = view.findViewById(R.id.dialogPlayerPhoto);
        ImageView cutout = view.findViewById(R.id.dialogPlayerCutout);

        ImageView closeButton = view.findViewById(R.id.closeButton);

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
            Glide.with(requireContext())
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
            Glide.with(requireContext())
                    .load(player.strCutout)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(cutout);
        } else {
            cutout.setImageResource(R.drawable.ic_person);
        }

        // Кнопка закрытия
        closeButton.setOnClickListener(v -> dismiss());
    }
}