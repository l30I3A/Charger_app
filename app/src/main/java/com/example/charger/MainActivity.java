package com.example.charger;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private float batteryLevel_1 = 34;
    private float batteryLevel_2 = 23;
    private float batteryLevel_3 = 12;
    private float batteryLevel_4 = 54;
    private int current_power = 600;
    private int current_temperature = 76;
    private int current_RPM = 0;
    private int currentColor;
    private float X = 281;
    private float Y = 281;
    private float current_X;
    private float current_Y;
    private boolean switchStateVent;
    private boolean switchState;
    private float lastFanRotation = 0f;
    BatteryView batteryView_1;
    BatteryView batteryView_2;
    BatteryView batteryView_3;
    BatteryView batteryView_4;
    TemperatureView temperatureview;
    TextView textViewChargingLevel1;
    TextView textViewChargingLevel2;
    TextView textViewChargingLevel3;
    TextView textViewChargingLevel4;
    TextView textViewTemperature;
    TextView textViewPower1;
    TextView textViewPower2;
    TextView textViewPower3;
    TextView textViewRPM;
TextView textcolorlevel;
    ImageView imageViewCharger1;
    ImageView imageViewCharger2;
    ImageView imageViewCharger3;
    ImageView imageViewCharger4;
    ImageView imageViewFan;
    SeekBar seekBar;
    ColorCircleView colorCircleView;
    ColorViewer colorViewer;
    CustomSwitch customSwitch;
    CustomSwitch customSwitchVent;
    ScrollView scrollView;
    ImageButton historyButton;
    ImageButton settingsButton;
    Settings settings;
    private AppUsageTracker usageTracker;

    private ObjectAnimator fanRotationAnimator;

    @Override
    protected void onStart() {
        super.onStart();
        scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        View mainLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(getColor(R.color.background_gray));

        settings = new Settings();
        AppUsageTracker appUsageTracker = new AppUsageTracker(this);
        if (!MqttManager.getInstance().isConnected.get()) {
            settings.show(getSupportFragmentManager(), "settingsFragment");
        }

        usageTracker = new AppUsageTracker(this);
        usageTracker.checkAndSaveDailyUsage(); // Проверяем, не наступил ли новый день

        updateHistory();

        MqttManager.getInstance().subscribe("charger/power/1");
        MqttManager.getInstance().subscribe("charger/power/2");
        MqttManager.getInstance().subscribe("charger/power/3");
        MqttManager.getInstance().subscribe("charger/power/4");
        MqttManager.getInstance().subscribe("charger/temp");
        MqttManager.getInstance().setMqttListener((topic, message) -> {
            Log.d("MQTT_Reseiver", "Получено сообщение: " + message + " из " + topic);
            float value = Float.parseFloat(message); // Конвертируем сообщение в float
            switch (topic) {
                case "charger/power/1":
                    batteryLevel_1 = value;
                    updateBatteryUI(1, batteryLevel_1);
                    break;
                case "charger/power/2":
                    batteryLevel_2 = value;
                    updateBatteryUI(2, batteryLevel_2);
                    break;
                case "charger/power/3":
                    batteryLevel_3 = value;
                    updateBatteryUI(3, batteryLevel_3);
                    break;
                case "charger/power/4":
                    batteryLevel_4 = value;
                    updateBatteryUI(4, batteryLevel_4);
                    break;
                case "charger/temp":
                    current_temperature = (int) value;
                    updateTemperatureUI(current_temperature);
                    break;
            }
        });

        batteryView_1 = findViewById(R.id.batteryView_1);
        batteryView_1.setTemperatureLevel(batteryLevel_1);
        batteryView_1.startWaveAnimation();
        batteryView_2 = findViewById(R.id.batteryView_2);
        batteryView_2.setTemperatureLevel(batteryLevel_2);
        batteryView_2.startWaveAnimation();
        batteryView_3 = findViewById(R.id.batteryView_3);
        batteryView_3.setTemperatureLevel(batteryLevel_3);
        batteryView_3.startWaveAnimation();
        batteryView_4 = findViewById(R.id.batteryView_4);
        batteryView_4.setTemperatureLevel(batteryLevel_4);
        batteryView_4.startWaveAnimation();
        textViewChargingLevel1 = findViewById(R.id.textViewChargingLevel1);
        textViewChargingLevel2 = findViewById(R.id.textViewChargingLevel2);
        textViewChargingLevel3 = findViewById(R.id.textViewChargingLevel3);
        textViewChargingLevel4 = findViewById(R.id.textViewChargingLevel4);
        textViewPower1 = findViewById(R.id.textViewPower1);
        textViewPower2 = findViewById(R.id.textViewPower2);
        textViewPower3 = findViewById(R.id.textViewPower3);
        imageViewCharger1 = findViewById(R.id.imageViewCharger1);
        imageViewCharger2 = findViewById(R.id.imageViewCharger2);
        imageViewCharger3 = findViewById(R.id.imageViewCharger3);
        imageViewCharger4 = findViewById(R.id.imageViewCharger4);
        imageViewFan = findViewById(R.id.imageViewFan);
        textViewRPM = findViewById(R.id.textViewRPM);
        colorCircleView = findViewById(R.id.colorCircleView);
        colorViewer = findViewById(R.id.colorViewer);
        textcolorlevel = findViewById(R.id.textcolorlevel);
        temperatureview = findViewById(R.id.temperatureView);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        scrollView = findViewById(R.id.scrollView);

        SharedPreferences sharedPreferences = getSharedPreferences("charger_data", MODE_PRIVATE);
        batteryLevel_1 = sharedPreferences.getFloat("batteryLevel_1", 0f);
        batteryLevel_2 = sharedPreferences.getFloat("batteryLevel_2", 0f);
        batteryLevel_3 = sharedPreferences.getFloat("batteryLevel_3", 0f);
        batteryLevel_4 = sharedPreferences.getFloat("batteryLevel_4", 0f);
        current_power = sharedPreferences.getInt("current_power", 600);
        current_temperature = sharedPreferences.getInt("current_temperature", 0);
        current_RPM = sharedPreferences.getInt("current_RPM", 0);
        currentColor = sharedPreferences.getInt("currentColor", 0xFFFFFFFF);
        current_X = sharedPreferences.getFloat("current_X", X);
        current_Y = sharedPreferences.getFloat("current_Y", Y);
        switchState = sharedPreferences.getBoolean("customSwitchState", false);
        switchStateVent = sharedPreferences.getBoolean("customSwitchStateVent", false);
        lastFanRotation = sharedPreferences.getFloat("lastFanRotation", 0f);

        MqttManager.getInstance().publish("charger/color", String.format("#%06X", (0xFFFFFF & currentColor), true), true);
        MqttManager.getInstance().publish("charger/cooler", switchStateVent ? "true" : "false", true);
        MqttManager.getInstance().publish("charger/charging/power", String.valueOf(current_power), true);

        updateBatteryUI(1, batteryLevel_1);
        updateBatteryUI(2, batteryLevel_2);
        updateBatteryUI(3, batteryLevel_3);
        updateBatteryUI(4, batteryLevel_4);
        updateTemperatureUI(current_temperature);
        colorCircleView.post(() -> colorCircleView.setSelectorPosition(current_X, current_Y));

        colorCircleView.setOnColorSelectedListener(color -> {
            colorViewer.SetViewingColor(color);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("currentColor", currentColor);
            editor.putFloat("current_X", colorCircleView.get_my_X());
            editor.putFloat("current_Y", colorCircleView.get_my_Y());
            editor.apply();
            if (currentColor != color && switchState){
                currentColor = color;
                MqttManager.getInstance().publish("charger/color", String.format("#%06X", (0xFFFFFF & currentColor)), true);
            }
        });

        customSwitch = findViewById(R.id.customSwitch);
        customSwitch.setChecked(switchState);
        if (switchState) {
            colorCircleView.setEnabled(true);
            textcolorlevel.setAlpha(1);
            colorCircleView.setAlpha(1);
            colorViewer.setAlpha(1);
        } else {
            colorCircleView.setEnabled(false);
            textcolorlevel.setAlpha(0.3f);
            colorCircleView.setAlpha(0.3f);
            colorViewer.setAlpha(0.3f);
            currentColor = 0x000000;
        }
        customSwitch.setOnCheckedChangeListener(isChecked -> {
            switchState = isChecked;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("customSwitchState", isChecked);
            editor.apply();

            if (isChecked) {
                colorCircleView.setEnabled(true);
                textcolorlevel.setAlpha(1);
                colorCircleView.setAlpha(1);
                colorViewer.setAlpha(1);
                currentColor = colorCircleView.getColorAtPoint();
                MqttManager.getInstance().publish("charger/color", String.format("#%06X", (0xFFFFFF & currentColor)), true);
            } else {
                colorCircleView.setEnabled(false);
                textcolorlevel.setAlpha(0.3f);
                colorCircleView.setAlpha(0.3f);
                colorViewer.setAlpha(0.3f);
                currentColor = 0x000000;
                MqttManager.getInstance().publish("charger/color", String.format("#%06X", (0)), true);
            }
            editor.putInt("currentColor", currentColor);
            editor.putFloat("current_X", colorCircleView.get_my_X());
            editor.putFloat("current_Y", colorCircleView.get_my_Y());
            editor.apply();
        });

        customSwitchVent = findViewById(R.id.customSwitchVent);

        customSwitchVent.setChecked(switchStateVent);
        if (switchStateVent) {
            updateTemperatureUI(current_temperature);
        } else {
            current_RPM = 0;
            rotateImage();
        }

        customSwitchVent.setOnCheckedChangeListener(isChecked -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("customSwitchStateVent", isChecked);
            editor.apply();
            switchStateVent = isChecked;
            MqttManager.getInstance().publish("charger/cooler", switchStateVent ? "true" : "false", true);
            if (switchStateVent) {
                updateTemperatureUI(current_temperature);
            } else {
                current_RPM = 0;
                rotateImage();
            }
        });

        seekBar = findViewById(R.id.powerSeekBar);
        switch (current_power) {
            case 300: {
                seekBar.setProgress(0);
                textViewPower2.setTextColor(getColor(R.color.text_gray2));
                textViewPower3.setTextColor(getColor(R.color.text_gray2));
                break;
            }
            case 600: {
                seekBar.setProgress(50);
                textViewPower2.setTextColor(getColor(R.color.text_progress_blue));
                textViewPower3.setTextColor(getColor(R.color.text_gray2));
                break;
            }
            case 900: {
                seekBar.setProgress(100);
                textViewPower2.setTextColor(getColor(R.color.text_progress_blue));
                textViewPower3.setTextColor(getColor(R.color.text_progress_blue));
                break;
            }
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 50) {
                    textViewPower2.setTextColor(getColor(R.color.text_gray2));
                    textViewPower3.setTextColor(getColor(R.color.text_gray2));
                } else if (progress != 100) {
                    textViewPower2.setTextColor(getColor(R.color.text_progress_blue));
                    textViewPower3.setTextColor(getColor(R.color.text_gray2));
                } else {
                    textViewPower2.setTextColor(getColor(R.color.text_progress_blue));
                    textViewPower3.setTextColor(getColor(R.color.text_progress_blue));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                int targetProgress;
                if (progress < 25) {
                    targetProgress = 0;
                    current_power = 300;
                } else if (progress < 75) {
                    targetProgress = 50;
                    current_power = 600;
                } else {
                    targetProgress = 100;
                    current_power = 900;
                }
                MqttManager.getInstance().publish("charger/charging/power", String.valueOf(current_power), true);

                int distance = Math.abs(progress - targetProgress);
                long duration = Math.max(100, (long) (1000 * (distance / 100.0)));

                ObjectAnimator animation = ObjectAnimator.ofInt(seekBar, "progress", targetProgress);
                animation.setDuration(duration);
                animation.start();
            }
        });
        historyButton = findViewById(R.id.homeButton);
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, History.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> settings.show(getSupportFragmentManager(), "settingsFragment"));
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));

        if (imageViewFan != null) {
            imageViewFan.setRotation(lastFanRotation);
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateBatteryUI(int batteryId, float batteryLevel) {
        switch (batteryId) {
            case 1:
                batteryView_1.setTemperatureLevel(batteryLevel); // Update battery view
                textViewChargingLevel1.setText(String.format("%d%%", (int) batteryLevel));
                if (batteryLevel != 0) {
                    imageViewCharger1.setImageResource(R.drawable.flash);
                    textViewChargingLevel1.setTextColor(getColor(R.color.text_progress_green));
                } else {
                    imageViewCharger1.setImageResource(R.drawable.cancel);
                    textViewChargingLevel1.setTextColor(getColor(R.color.text_gray2));
                }
                break;
            case 2:
                batteryView_2.setTemperatureLevel(batteryLevel); // Update battery view
                textViewChargingLevel2.setText(String.format("%d%%", (int) batteryLevel));
                if (batteryLevel != 0) {
                    imageViewCharger2.setImageResource(R.drawable.flash);
                    textViewChargingLevel2.setTextColor(getColor(R.color.text_progress_green));
                } else {
                    imageViewCharger2.setImageResource(R.drawable.cancel);
                    textViewChargingLevel2.setTextColor(getColor(R.color.text_gray2));
                }
                break;
            case 3:
                batteryView_3.setTemperatureLevel(batteryLevel); // Update battery view
                textViewChargingLevel3.setText(String.format("%d%%", (int) batteryLevel));
                if (batteryLevel != 0) {
                    imageViewCharger3.setImageResource(R.drawable.flash);
                    textViewChargingLevel3.setTextColor(getColor(R.color.text_progress_green));
                } else {
                    imageViewCharger3.setImageResource(R.drawable.cancel);
                    textViewChargingLevel3.setTextColor(getColor(R.color.text_gray2));
                }
                break;
            case 4:
                batteryView_4.setTemperatureLevel(batteryLevel); // Update battery view
                textViewChargingLevel4.setText(String.format("%d%%", (int) batteryLevel));
                if (batteryLevel != 0) {
                    imageViewCharger4.setImageResource(R.drawable.flash);
                    textViewChargingLevel4.setTextColor(getColor(R.color.text_progress_green));
                } else {
                    imageViewCharger4.setImageResource(R.drawable.cancel);
                    textViewChargingLevel4.setTextColor(getColor(R.color.text_gray2));
                }
                break;
        }
    }

    private void updateTemperatureUI(int temperatureLevel) {
        int finalTemp = Math.max(30, Math.min(temperatureLevel, 80));
        int finalTempRPM = Math.max(40, Math.min(temperatureLevel, 100));
        temperatureview.setTemperatureLevel(finalTemp);
        temperatureview.post(() -> {
            int X = temperatureview.getWidth() / 2;
            int Y = (int) ((temperatureview.getHeight() - temperatureview.returnBoarder() * 2) * (1 - (float) finalTemp / 80) + 20);
            int pixelColor = getPixelColorFromView(temperatureview, X, Y);
            textViewTemperature.setTextColor(pixelColor);
            if (current_temperature < 100 && current_temperature >= 30)
                textViewTemperature.setText(String.format("%d℃", current_temperature));
            else if (current_temperature < 30)
                textViewTemperature.setText(String.format('<' + "%d℃", 30));
            else
                textViewTemperature.setText(String.format('>' + "%d℃", 99));
            if (switchStateVent && finalTempRPM > 40) {
                current_RPM = map(finalTempRPM, 50, 100, 1500, 6000);
            } else {
                current_RPM = 0;
            }
            rotateImage();
        });
    }

    private void rotateImage() {
        if (current_RPM > 0) {
            float currentRotation = imageViewFan.getRotation();
            if (fanRotationAnimator != null) {
                fanRotationAnimator.cancel();
            }
            fanRotationAnimator = ObjectAnimator.ofFloat(imageViewFan, "rotation", currentRotation, currentRotation + 360f);
            fanRotationAnimator.setInterpolator(new LinearInterpolator());
            fanRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
            int rotate_current_RPM = Math.min(current_RPM, 6000);
            fanRotationAnimator.setDuration(3600000L / rotate_current_RPM);
            textViewRPM.setText(rotate_current_RPM + "RPM");

            fanRotationAnimator.start();

        } else {
            if (fanRotationAnimator != null && fanRotationAnimator.isStarted()) {
                fanRotationAnimator.cancel();
            }
            textViewRPM.setText("0RPM");
        }
    }

    private int getPixelColorFromView(TemperatureView view, int x, int y) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap.getPixel(x, y);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences("charger_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("batteryLevel_1", batteryLevel_1);
        editor.putFloat("batteryLevel_2", batteryLevel_2);
        editor.putFloat("batteryLevel_3", batteryLevel_3);
        editor.putFloat("batteryLevel_4", batteryLevel_4);
        editor.putInt("current_power", current_power);
        editor.putInt("current_temperature", current_temperature);
        editor.putInt("current_RPM", current_RPM);
        editor.putInt("currentColor", currentColor);
        editor.putFloat("current_X", colorCircleView.get_my_X());
        editor.putFloat("current_Y", colorCircleView.get_my_Y());
        editor.putFloat("lastFanRotation", imageViewFan.getRotation());
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            MqttManager.getInstance().disconnect();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 100);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }

    private void updateHistory() {
        List<String[]> history = usageTracker.getHistoryList();
    }

    private int map(int x, int in_min, int in_max, int out_min, int out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}