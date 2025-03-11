package com.example.charger;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class Settings extends DialogFragment {
    ImageButton closeButton;
    TextView server_status;
    ImageView statusBarView;
    EditText editTextHostName;
    EditText editTextName;
    EditText editTextTextPassword;
    ImageButton imageButtonConnection;
    ImageButton toggleButton;
    private AnimationDrawable connectionAnimation;
    private final Handler handler = new Handler();
    private boolean isPasswordVisible;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);
        server_status = view.findViewById(R.id.server_status);
        statusBarView = view.findViewById(R.id.statusBarView);
        editTextHostName = view.findViewById(R.id.editTextHostName);
        editTextName = view.findViewById(R.id.editTextName);
        editTextTextPassword = view.findViewById(R.id.editTextTextPassword);
        imageButtonConnection = view.findViewById(R.id.imageButtonConnection);
        closeButton = view.findViewById(R.id.closeButton);
        toggleButton = view.findViewById(R.id.toggleButton);

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        String savedHost = sharedPreferences.getString("host", "");
        String savedName = sharedPreferences.getString("name", "");
        String savedPassword = sharedPreferences.getString("password", "");
        isPasswordVisible = sharedPreferences.getBoolean("password_status", false); // Читаем состояние

        editTextHostName.setText(savedHost);
        editTextName.setText(savedName);
        editTextTextPassword.setText(savedPassword);

        if (MqttManager.getInstance().isConnected.get()) {
            server_status.setText("Подключено");
            statusBarView.setImageResource(R.drawable.status_con);
        } else if (!MqttManager.getInstance().isConnected.get() &&
                !editTextHostName.getText().toString().trim().isEmpty() &&
                !editTextName.getText().toString().trim().isEmpty() &&
                !editTextTextPassword.getText().toString().trim().isEmpty()) {
            connectToMqtt();
        } else {
            server_status.setText("Ожидание подключения");
        }

        setPasswordVisibility();

        toggleButton.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            setPasswordVisibility();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("password_status", isPasswordVisible);
            editor.apply();
        });

        closeButton.setOnClickListener(v -> dismiss());
        imageButtonConnection.setOnClickListener(v -> connectToMqtt());
    }

    private void setPasswordVisibility() {
        int selection = editTextTextPassword.getSelectionStart();
        Typeface typeface = editTextTextPassword.getTypeface();

        if (isPasswordVisible) {
            editTextTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleButton.setImageResource(R.drawable.hide);
        } else {
            editTextTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleButton.setImageResource(R.drawable.view);
        }

        editTextTextPassword.setTypeface(typeface);
        editTextTextPassword.setSelection(selection);
    }

    private void startConnectionAnimation() {
        if (statusBarView == null) {
            return;
        }

        statusBarView.post(() -> {
            statusBarView.setImageResource(R.drawable.status_animation);
            connectionAnimation = (AnimationDrawable) statusBarView.getDrawable();
            connectionAnimation.stop();
            connectionAnimation.start();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;

        int dialogWidth = (int) (screenWidth * 0.8);
        window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = window.getAttributes();
        params.dimAmount = 0.7f;
        window.setAttributes(params);
    }

    public void connectToMqtt() {
        String host = editTextHostName.getText().toString().trim();
        String name = editTextName.getText().toString().trim();
        String password = editTextTextPassword.getText().toString().trim();
        if (host.isEmpty() || name.isEmpty() || password.isEmpty()) {
            server_status.setText("Ошибка: заполните все поля");
            return;
        }
        MqttManager.getInstance().onChange(host, 8883, name, password);
        MqttManager.getInstance().initializeClient();
        MqttManager.getInstance().connect();
        server_status.setText("Подключение...");

        startConnectionAnimation();

        handler.postDelayed(() -> {
            if (!isAdded()) return;
            if (MqttManager.getInstance().isConnected.get()) {
                server_status.setText("Подключено");
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("connection_status", "connected");
                editor.apply();

                connectionAnimation.stop();
                statusBarView.setImageResource(R.drawable.status_con);
            } else {
                server_status.setText("Ошибка подключения");
                connectionAnimation.stop();
                statusBarView.setImageResource(R.drawable.status);
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("connection_status", "disconnected");
                editor.apply();;
            }

        }, 3000);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        handler.removeCallbacksAndMessages(null);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("host", editTextHostName.getText().toString());
        editor.putString("name", editTextName.getText().toString());
        editor.putString("password", editTextTextPassword.getText().toString());
        editor.apply();
    }

}
