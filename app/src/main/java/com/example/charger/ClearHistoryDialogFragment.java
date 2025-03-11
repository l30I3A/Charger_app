package com.example.charger;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import java.util.Objects;

public class ClearHistoryDialogFragment extends DialogFragment {

    private AppUsageTracker appUsageTracker;

    // Интерфейс для передачи событий обратно в активность
    public interface OnClearHistoryListener {
        void onClearHistory();
    }

    private OnClearHistoryListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Инициализация AppUsageTracker
        appUsageTracker = new AppUsageTracker(getActivity());

        // Строим кастомный диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_clear_history, null); // Укажите свой layout для кастомного диалога

        // Привязываем кнопки и задаем обработчики
        ImageButton deleteButton = view.findViewById(R.id.imageButtonClear);
        ImageButton cancelButton = view.findViewById(R.id.imageButtonClose);

        deleteButton.setOnClickListener(v -> {
            // Очищаем историю и передаем событие в активность
            appUsageTracker.clearHistory();
            if (listener != null) {
                listener.onClearHistory();
            }
            dismiss(); // Закрываем диалог
        });

        cancelButton.setOnClickListener(v -> {
            dismiss(); // Просто закрываем диалог
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);
        return builder.create();
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

    public void setOnClearHistoryListener(OnClearHistoryListener listener) {
        this.listener = listener;
    }
}