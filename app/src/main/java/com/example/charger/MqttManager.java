package com.example.charger;

public class MqttManager {
    private static MqttHelper instance;

    public static MqttHelper getInstance() {
        if (instance == null) {
            instance = new MqttHelper();
        }
        return instance;
    }
}

