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
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ClearHistoryDialogFragment extends DialogFragment {

    private AppUsageTracker appUsageTracker;

    // Интерфейс для передачи событий обратно в активность (Activity)
    public interface OnClearHistoryListener {
        void onClearHistory();
    }

    private OnClearHistoryListener listener;

    // Этот метод вызывается для создания и возврата иерархии View для диалога.
    // Аналогичен методу onCreateView в обычном Fragment.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация AppUsageTracker. Лучше делать это здесь или в onViewCreated,
        // чтобы убедиться, что контекст Activity доступен.
        appUsageTracker = new AppUsageTracker(getActivity());

        // Загружаем макет вашего кастомного диалога
        View view = inflater.inflate(R.layout.dialog_clear_history, container, false); // Укажите свой layout для кастомного диалога

        // Обработчики нажатий на кнопки будут установлены в onViewCreated,
        // после того как View будет полностью создана.

        return view;
    }

    // Этот метод вызывается для создания объекта Dialog.
    // Здесь можно настроить базовые свойства диалога, такие как отмена по касанию вне.
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Вызываем метод суперкласса для получения стандартного объекта Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Запрещаем закрытие диалога при касании вне его области.
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    // Этот метод вызывается сразу после onCreateView, когда View уже создана.
    // Здесь удобно инициализировать элементы View и устанавливать слушатели.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Привязываем кнопки из макета и устанавливаем обработчики нажатий
        ImageButton deleteButton = view.findViewById(R.id.imageButtonClear);
        ImageButton cancelButton = view.findViewById(R.id.imageButtonClose);

        // Устанавливаем слушатель для кнопки "Удалить"
        deleteButton.setOnClickListener(v -> {
            // Очищаем историю использования приложения, если appUsageTracker инициализирован
            if (appUsageTracker != null) {
                appUsageTracker.clearHistory();
            }
            // Уведомляем слушателя (обычно Activity), что история очищена
            if (listener != null) {
                listener.onClearHistory();
            }
            dismiss(); // Закрываем диалог
        });

        // Устанавливаем слушатель для кнопки "Отмена"
        cancelButton.setOnClickListener(v -> {
            dismiss(); // Просто закрываем диалог
        });
    }

    // Этот метод вызывается после onCreateDialog и onCreateView.
    // Здесь мы настраиваем размер и внешний вид окна диалога.
    @Override
    public void onStart() {
        super.onStart();
        // Получаем объект Dialog, убеждаемся, что он не null
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            // Убеждаемся, что Window не null
            if (window != null) {
                // Получаем метрики дисплея для расчета размера диалога
                DisplayMetrics metrics = new DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int screenWidth = metrics.widthPixels;

                // Устанавливаем ширину диалога в 80% от ширины экрана, высота - WRAP_CONTENT
                int dialogWidth = (int) (screenWidth * 0.8);
                window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                // Делаем фон окна диалога прозрачным, чтобы видеть закругленные углы или тени макета
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Настраиваем параметры окна, например, затемнение фона за диалогом
                WindowManager.LayoutParams params = window.getAttributes();
                params.dimAmount = 0.7f; // Устанавливаем затемнение на 70%
                window.setAttributes(params);
            }
        }
    }

    // Метод для установки слушателя событий.
    // Activity или Fragment, который вызывает этот DialogFragment, будет реализовывать этот интерфейс.
    public void setOnClearHistoryListener(OnClearHistoryListener listener) {
        this.listener = listener;
    }
}