package com.example.charger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class History extends AppCompatActivity {

    ScrollView scrollView;
    private AppUsageTracker usageTracker;
    private LinearLayout historyContainer;
    ImageButton homeButton;
    ImageButton eraseButton;

    @Override
    protected void onStart() {
        super.onStart();
        scrollView = findViewById(R.id.scrollView_1);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        homeButton = findViewById(R.id.homeButton);
        eraseButton = findViewById(R.id.eraserButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(History.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        historyContainer = findViewById(R.id.historyContainer);
        usageTracker = new AppUsageTracker(this);

        eraseButton.setOnClickListener(v -> {
                showClearHistoryDialog();
        });

        displayHistory();  // Загружаем историю при создании активности
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(History.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void displayHistory() {
        historyContainer.removeAllViews();

        List<String[]> historyList = usageTracker.getHistoryList();

        for (int i = historyList.size() - 1; i >= 0; i--) {
            String[] record = historyList.get(i);
            addHistoryView(record[0], record[1], record[2], record[3], record[4]);
        }
    }

    private void addHistoryView(String date, String day, String launches, String time, String data) {
        // Используем historyContainer в качестве родителя, но не добавляем сразу в него
        final View historyItem = getLayoutInflater().inflate(R.layout.history_item, historyContainer, false);

        // Получаем ссылки на элементы
        TextView textData = historyItem.findViewById(R.id.textData);
        TextView textInformation = historyItem.findViewById(R.id.textInformation);
        ImageButton deleteButton = historyItem.findViewById(R.id.imageButton);

        // Устанавливаем данные
        textData.setText(date + " (" + day + ")");
        textInformation.setText("Запусков: " + launches + " | Время: " + time + " мин | Трафик: " + data + " MB");

        // Удаление элемента
        deleteButton.setOnClickListener(v -> {
            // Анимация для удаления элемента
            animateItemRemoval(historyItem);
        });

        // Добавляем в начало списка
        historyContainer.addView(historyItem, 0);
    }

    private void animateItemRemoval(View historyItem) {
        // Анимация для перемещения элемента влево
        ObjectAnimator translationX = ObjectAnimator.ofFloat(historyItem, "translationX", 0f, -historyItem.getWidth());
        translationX.setDuration(300); // 300 миллисекунд для плавного удаления

        // После окончания анимации, удаляем элемент из контейнера и SharedPreferences
        translationX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Удаляем из памяти
                String date = ((TextView) historyItem.findViewById(R.id.textData)).getText().toString().trim();
                usageTracker.deleteHistoryItem(date);

                historyContainer.removeView(historyItem);
            }
        });

        translationX.start();
        animateRemainingItems();
    }

    private void animateItemRemovalWithDelay(View historyItem, int delay) {
        ObjectAnimator translationX = ObjectAnimator.ofFloat(historyItem, "translationX", 0f, -historyItem.getWidth());
        translationX.setDuration(300);
        translationX.setStartDelay(delay);

        translationX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                historyContainer.removeView(historyItem);
            }
        });

        translationX.start();
    }

    private void animateRemainingItems() {
        for (int i = 0; i < historyContainer.getChildCount(); i++) {
            View item = historyContainer.getChildAt(i);
            item.animate().translationY(0).setDuration(300).start();
        }
    }

    private void showClearHistoryDialog() {
        ClearHistoryDialogFragment dialogFragment = new ClearHistoryDialogFragment();

        dialogFragment.setOnClearHistoryListener(() -> {
            int childCount = historyContainer.getChildCount();
            if (childCount == 0) return;

            historyContainer.setLayoutTransition(null);

            historyContainer.getLayoutParams().height = historyContainer.getHeight();
            historyContainer.requestLayout();

            for (int i = 0; i < childCount; i++) {
                final View historyItem = historyContainer.getChildAt(i);
                int delay = i * 100;
                animateItemRemovalWithDelay(historyItem, delay);
            }

            historyContainer.postDelayed(() -> {
                historyContainer.removeAllViews();
                historyContainer.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                historyContainer.requestLayout();
            }, childCount * 100 + 300);
        });

        dialogFragment.show(getSupportFragmentManager(), "ClearHistoryDialog");
    }


    @Override
    protected void onResume() {
        super.onResume();
        displayHistory();
    }
}
