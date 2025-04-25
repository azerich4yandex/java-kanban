package ru.yandex.practicum.scheduler.servers.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import ru.yandex.practicum.scheduler.managers.Managers;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.servers.http.handlers.EpicHttpHandler;
import ru.yandex.practicum.scheduler.servers.http.handlers.HistoryHttpHandler;
import ru.yandex.practicum.scheduler.servers.http.handlers.PrioritizedHttpHandler;
import ru.yandex.practicum.scheduler.servers.http.handlers.SubtaskHttpHandler;
import ru.yandex.practicum.scheduler.servers.http.handlers.TaskHttpHandler;
import ru.yandex.practicum.scheduler.servers.http.typeadapters.DurationTypeAdapter;
import ru.yandex.practicum.scheduler.servers.http.typeadapters.LocalDateTimeTypeAdapter;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer httpServer;
    private final TaskManager taskManager = Managers.getDefault();
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        // Создаём и настраиваем Gson
        this.gson = configureGson();
        // Создаём сервер
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        // Добавляем точку входа /tasks
        this.httpServer.createContext("/tasks", new TaskHttpHandler(taskManager, gson));
        // Добавляем точку входа /subtasks
        this.httpServer.createContext("/subtasks", new SubtaskHttpHandler(taskManager, gson));
        // Добавляем точку входа /epics
        this.httpServer.createContext("/epics", new EpicHttpHandler(taskManager, gson));
        // Добавляем точку входа /history
        this.httpServer.createContext("/history", new HistoryHttpHandler(taskManager, gson));
        // Добавляем точку входа /prioritized
        this.httpServer.createContext("/prioritized", new PrioritizedHttpHandler(taskManager, gson));
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }

    private Gson configureGson() {
        return new GsonBuilder()
                // Включаем форматирование вывода.
                .setPrettyPrinting()
                // Включаем сериализацию пустых значений.
                .serializeNulls()
                // Подключаем адаптеры
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter()).create();
    }

    public int getPort() {
        return PORT;
    }

    public Gson getGson() {
        return gson;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
    }
}
