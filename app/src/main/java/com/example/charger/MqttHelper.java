package com.example.charger;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MqttHelper {
    private String host;
    private int port;
    private String name;
    private String password;
    private Mqtt3AsyncClient client;
    private MqttListener listener;
    final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final Queue<Runnable> pendingMessages = new ConcurrentLinkedQueue<>();
    private final Queue<String> pendingSubscriptions = new ConcurrentLinkedQueue<>();

    public MqttHelper() {
        this.host = "default_host";
        this.port = 1883;
        this.name = "default_user";
        this.password = "default_pass";
        initializeClient();
    }

    void initializeClient() {
        this.client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(UUID.randomUUID().toString())
                .serverHost(host)
                .serverPort(port)
                .sslWithDefaultConfig()
                .buildAsync();

        client.toAsync().disconnect().whenComplete((ack, throwable) -> {
            isConnected.set(false);
            System.out.println("MQTT отключен!");
        });
    }

    public void onChange(String host, int port, String name, String password) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.password = password;
        initializeClient();
    }

    public void connect() {
        client.connectWith()
                .simpleAuth()
                .username(name)
                .password(password.getBytes())
                .applySimpleAuth()
                .send()
                .whenComplete((ack, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Ошибка подключения: " + throwable.getMessage());
                    } else {
                        System.out.println("Подключено к брокеру!");
                        isConnected.set(true);
                        processPendingMessages();
                        processPendingSubscriptions();
                    }
                });
    }

    public void publish(String topic, String message) {
        Runnable publishTask = () -> client.publishWith()
                .topic(topic)
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.EXACTLY_ONCE)
                .send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Ошибка публикации: " + throwable.getMessage());
                    } else {
                        System.out.println("Сообщение опубликовано в топик: " + topic);
                    }
                });

        if (isConnected.get()) {
            publishTask.run();
        } else {
            System.out.println("Соединение не установлено. Сообщение добавлено в очередь.");
            pendingMessages.add(publishTask);
        }
    }

    private void processPendingMessages() {
        Runnable task;
        while ((task = pendingMessages.poll()) != null) {
            task.run();
        }
    }

    public void subscribe(String topic) {
        Runnable subscribeTask = () -> client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    String message = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    System.out.println("Получено сообщение из " + topic + ": " + message);
                    if (listener != null) {
                        listener.onMessageReceived(topic, message);
                    }
                })

                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Ошибка подписки: " + throwable.getMessage());
                    } else {
                        System.out.println("Подписан на " + topic);
                    }
                });

        if (isConnected.get()) {
            subscribeTask.run();
        } else {
            System.out.println("Соединение не установлено. Подписка добавлена в очередь.");
            pendingSubscriptions.add(topic);
        }
    }

    private void processPendingSubscriptions() {
        String topic;
        while ((topic = pendingSubscriptions.poll()) != null) {
            subscribe(topic);
        }
    }

    public void disconnect() {
        client.disconnect()
                .whenComplete((ack, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Ошибка отключения: " + throwable.getMessage());
                    } else {
                        System.out.println("Отключено от брокера.");
                        isConnected.set(false);
                    }
                });
    }

    public interface MqttListener {
        void onMessageReceived(String topic, String message);
    }

    public void setMqttListener(MqttListener listener) {
        this.listener = listener;
    }
}
