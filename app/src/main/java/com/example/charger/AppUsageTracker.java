package com.example.charger;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppUsageTracker {
    private static final String PREFS_NAME = "AppUsagePrefs";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_LAST_DAY = "last_day";
    private static final String KEY_LAUNCH_COUNT = "launch_count";
    private static final String KEY_TOTAL_TIME = "total_time";
    private static final String KEY_TOTAL_DATA = "total_data";
    private static final String KEY_HISTORY = "history";

    private SharedPreferences prefs;
    private long startTime;
    private long startRx;
    private long startTx;
    private Handler handler;
    private Runnable updateRunnable;

    public AppUsageTracker(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        handler = new Handler(Looper.getMainLooper());
        startTracking();
    }

    private void startTracking() {
        String currentDate = getCurrentDate();
        String lastSavedDate = prefs.getString(KEY_LAST_DATE, "");
        String lastSavedDay = prefs.getString(KEY_LAST_DAY, "");

        if (!lastSavedDate.equals(currentDate) && !lastSavedDate.isEmpty()) {
            saveUsageData(lastSavedDate, lastSavedDay);
            resetDailyStats(); // Сброс статистики на новый день
            resetLaunchCount(); // Сброс количества запусков
        }

        startTime = SystemClock.elapsedRealtime();
        startRx = TrafficStats.getUidRxBytes(android.os.Process.myUid());
        startTx = TrafficStats.getUidTxBytes(android.os.Process.myUid());

        prefs.edit()
                .putString(KEY_LAST_DATE, currentDate)
                .putString(KEY_LAST_DAY, getCurrentDay())
                .apply();

        // Запускаем периодическое обновление
        startPeriodicUpdate();
    }

    private void startPeriodicUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateUsageStats(); // Обновляем статистику
                handler.postDelayed(this, 1000); // Повторяем через 500 мс
            }
        };
        handler.post(updateRunnable); // Запускаем первую задачу
    }

    private void stopPeriodicUpdate() {
        if (updateRunnable != null) {
            handler.removeCallbacks(updateRunnable); // Останавливаем обновление
        }
    }

    private void updateUsageStats() {
        long endTime = SystemClock.elapsedRealtime();
        long elapsedTime = endTime - startTime;
        startTime = endTime;
        long rxBytes = TrafficStats.getUidRxBytes(android.os.Process.myUid()) - startRx;
        long txBytes = TrafficStats.getUidTxBytes(android.os.Process.myUid()) - startTx;

        long totalTime = prefs.getLong(KEY_TOTAL_TIME, 0) + elapsedTime;
        long totalData = prefs.getLong(KEY_TOTAL_DATA, 0) + (rxBytes + txBytes);

        prefs.edit()
                .putLong(KEY_TOTAL_TIME, totalTime)
                .putLong(KEY_TOTAL_DATA, totalData)
                .apply();
    }

    private void saveUsageData(String date, String day) {
        SharedPreferences.Editor editor = prefs.edit();

        String newEntry = String.format(Locale.getDefault(),
                "%s|%s|%d|%d|%d\n",
                date,
                day,
                prefs.getInt(KEY_LAUNCH_COUNT, 0), // Записываем количество запусков
                prefs.getLong(KEY_TOTAL_TIME, 0) / 180000, // Время в минутах
                prefs.getLong(KEY_TOTAL_DATA, 0) / 1048576 // Трафик в KB
        );

        String history = prefs.getString(KEY_HISTORY, "");
        history = newEntry + history;

        editor.putString(KEY_HISTORY, history);
        editor.apply();
    }

    private void resetDailyStats() {
        // Сброс всех статистик на новый день
        prefs.edit()
                .putLong(KEY_TOTAL_TIME, 0)
                .putLong(KEY_TOTAL_DATA, 0)
                .apply();
    }

    private void resetLaunchCount() {
        // Сброс количества запусков
        prefs.edit().putInt(KEY_LAUNCH_COUNT, 0).apply();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    }

    private String getCurrentDay() {
        return new SimpleDateFormat("EEEE", new Locale("ru")).format(new Date());
    }

    public List<String[]> getHistoryList() {
        List<String[]> historyList = new ArrayList<>();
        String history = prefs.getString(KEY_HISTORY, "");

        if (!history.isEmpty()) {
            String[] entries = history.split("\n");

            for (String entry : entries) {
                String[] data = entry.split("\\|");
                if (data.length == 5) {
                    historyList.add(data);
                }
            }
        }
        return historyList;
    }

    public void deleteHistoryItem(String rawDate) {
        rawDate = rawDate.trim(); // Убираем пробелы

        // Преобразуем "04-02-2025 (вторник)" → "04-02-2025|вторник"
        String date = rawDate.split(" ")[0]; // Берём только "04-02-2025"
        String day = rawDate.substring(rawDate.indexOf("(") + 1, rawDate.indexOf(")")); // Берём "вторник"
        String formattedDate = date + "|" + day;

        String history = prefs.getString(KEY_HISTORY, "");
        String[] historyItems = history.split("\n");

        StringBuilder updatedHistory = new StringBuilder();
        boolean found = false;

        for (String item : historyItems) {
            if (!item.startsWith(formattedDate)) {
                updatedHistory.append(item).append("\n");
            } else {
                found = true;
            }
        }

        if (found) {
            prefs.edit().putString(KEY_HISTORY, updatedHistory.toString().trim()).apply();
            Log.d("DELETE", "Удалена запись: " + formattedDate);
        } else {
            Log.d("DELETE", "Не найдена запись: " + formattedDate);
        }
    }



    public void clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply();
    }

    public void checkAndSaveDailyUsage() {
        String currentDate = getCurrentDate();
        String lastSavedDate = prefs.getString(KEY_LAST_DATE, "");
        //String lastSavedDay = prefs.getString(KEY_LAST_DAY, "");

        // Если сохраненная дата пуста (первый запуск) — просто обновляем текущую дату
        if (lastSavedDate.isEmpty()) {
            prefs.edit()
                    .putString(KEY_LAST_DATE, currentDate)
                    .putString(KEY_LAST_DAY, getCurrentDay())
                    .apply();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date lastDate = dateFormat.parse(lastSavedDate);
            Date todayDate = dateFormat.parse(currentDate);

            if (lastDate != null && todayDate != null) {
                long diff = (todayDate.getTime() - lastDate.getTime()) / (1000 * 60 * 60 * 24);

                if (diff >= 1) { // Если пропущен хотя бы один день
                    for (int i = (int) diff; i > 0; i--) {
                        // Генерируем даты за пропущенные дни
                        Date missedDate = new Date(todayDate.getTime() - i * 24 * 60 * 60 * 1000);
                        String missedDateString = dateFormat.format(missedDate);
                        String missedDay = new SimpleDateFormat("EEEE", new Locale("ru")).format(missedDate);

                        saveUsageData(missedDateString, missedDay);
                    }

                    // После сохранения сбрасываем статистику
                    resetDailyStats();
                    resetLaunchCount();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Обновляем текущую дату
        prefs.edit()
                .putString(KEY_LAST_DATE, currentDate)
                .putString(KEY_LAST_DAY, getCurrentDay())
                .apply();
    }
}
