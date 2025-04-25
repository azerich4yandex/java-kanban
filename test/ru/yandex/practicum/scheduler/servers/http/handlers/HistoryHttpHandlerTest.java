package ru.yandex.practicum.scheduler.servers.http.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.servers.http.HttpTaskServer;
import ru.yandex.practicum.scheduler.servers.http.typetokens.TaskListTypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoryHttpHandlerTest {

    private HttpTaskServer taskServer;
    private HttpClient client;
    private TaskManager taskManager;

    @BeforeEach
    void init() throws IOException {
        taskServer = new HttpTaskServer();
        taskManager = taskServer.getTaskManager();
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    void halt() {
        taskManager.deleteTasks();
        taskManager.deleteSubtasks();
        taskManager.deleteEpics();
        taskServer.stop();
    }

    @DisplayName("Операции с историей")
    @Test
    void testGetHistory() throws IOException, InterruptedException {
        // Формируем запрос к пустой истории
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/history"))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        List<Task> expectedHistory = new ArrayList<>();
        List<Task> receivedHistory = taskServer.getGson().fromJson(jsonArray, new TaskListTypeToken().getType());

        assertTrue(receivedHistory.isEmpty(), "Получена неожиданная история обращений");
        assertEquals(expectedHistory, receivedHistory, "Пустые истории не совпадают");

        // Создаём задачи
        Task task1 = new Task("Task 1 name", "Task 1 description", LocalDateTime.now(), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 description", task1.getEndTime().plusMinutes(1),
                Duration.ofMinutes(5));

        // Добавляем задачи в хранилище с помощью менеджера задач
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Добавляем задачи в историю
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        // Формируем запрос к истории
        request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/history"))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        expectedHistory = new ArrayList<>(List.of(task1, task2));
        receivedHistory = taskServer.getGson().fromJson(jsonArray, new TaskListTypeToken().getType());

        assertFalse(receivedHistory.isEmpty(), "История не получена");
        assertEquals(expectedHistory, receivedHistory, "Получена неожиданная история обращений");

        // Формируем запрос к истории с неправильным URI
        request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/history/get"))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем запрос к истории с необработанным методом
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task1)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/history"))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }
}
