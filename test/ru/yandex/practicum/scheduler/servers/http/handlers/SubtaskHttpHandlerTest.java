package ru.yandex.practicum.scheduler.servers.http.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.servers.http.HttpTaskServer;
import ru.yandex.practicum.scheduler.servers.http.typetokens.SubtaskListTypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubtaskHttpHandlerTest {

    private HttpTaskServer taskServer;
    private HttpClient client;
    private Epic epic;

    @BeforeEach
    void init() throws IOException {
        taskServer = new HttpTaskServer();
        client = HttpClient.newHttpClient();
        epic = new Epic("First epic", "First epic description");
        taskServer.start();
    }

    @AfterEach
    void halt() {
        taskServer.getTaskManager().deleteTasks();
        taskServer.getTaskManager().deleteSubtasks();
        taskServer.getTaskManager().deleteEpics();
        taskServer.stop();
    }

    @DisplayName("Операции с Subtask: POST & GET (без ошибок)")
    @Test
    void testAddAndUpdateSubtaskNoErrors() throws IOException, InterruptedException {
        // Создаём запрос на добавление эпика
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(epic)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id эпика
        Integer id = jsonObject.get("id").getAsInt();

        assertNotNull(id, "Эпик не создан");

        epic.setId(id);
        Subtask subtask = new Subtask("First subtask", "First subtask description", LocalDateTime.now(),
                Duration.ofMinutes(30), epic.getId());

        // Создаём запрос на добавление подзадачи
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id подзадачи
        id = jsonObject.get("id").getAsInt();

        assertNotNull(id, "Подзадача не добавлена");

        subtask.setId(id);

        // Получаем подзадачу по id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        Subtask receivedSubtask = taskServer.getGson().fromJson(jsonObject, Subtask.class);

        assertEquals(subtask, receivedSubtask, "ИД созданной и сохранённой подзадач не совпадают");
        assertEquals(subtask.getStatus(), receivedSubtask.getStatus(),
                "Статус созданной и сохранённой задач не совпадают");
        assertEquals(subtask.getName(), receivedSubtask.getName(), "Имя созданной и сохранённой подзадач не совпадают");
        assertEquals(subtask.getDescription(), receivedSubtask.getDescription(),
                "Описание созданной и сохранённой задач не совпадают");
        assertEquals(subtask.getType(), receivedSubtask.getType(), "Тип созданной и сохранённой подзадач не совпадают");
        assertEquals(subtask.getDuration(), receivedSubtask.getDuration(),
                "Длительность созданной и сохранённой подзадач не совпадают");
        assertEquals(subtask.getStartTime().truncatedTo(ChronoUnit.MINUTES), receivedSubtask.getStartTime(),
                "Время начала созданной и сохранённой подзадач не совпадают");
        assertEquals(subtask.getEpicId(), receivedSubtask.getEpicId(),
                "Эпики созданной и сохранённой подзадач не совпадают");

        // Формируем запрос на получение списка задач
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        List<Subtask> expectedSubtasks = new ArrayList<>(List.of(subtask));
        List<Subtask> receivedSubtasks = taskServer.getGson().fromJson(jsonArray, new SubtaskListTypeToken().getType());

        assertFalse(receivedSubtasks.isEmpty(), "Не получен список подзадач");
        assertEquals(expectedSubtasks, receivedSubtasks, "Сохранённый и полученный списки не совпадают");
        assertEquals(subtask, receivedSubtasks.getFirst(), "ИД сохранённой и полученной подзадач не совпадают");

        // Обновляем подзадачу
        subtask.setName("Updated name");
        subtask.setDescription("Updated description");
        subtask.setStartTime(LocalDateTime.now().plusMinutes(15));
        subtask.setDuration(Duration.ofMinutes(10));

        // Формируем запрос на обновление подзадачи
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/" + subtask.getId()))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        assertEquals(id, jsonObject.get("id").getAsInt(), "ИД обновлённой и сохранённой задачи не совпадают");
    }

    @DisplayName("Операции с Subtask: DELETE (без ошибок)")
    @Test
    void testDeleteSubtaskNoErrors() throws IOException, InterruptedException {
        // Формируем запрос на добавление эпика
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(epic)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id эпика
        Integer id = jsonObject.get("id").getAsInt();

        assertNotNull(id, "Эпик не создан");

        epic.setId(id);
        Subtask subtask = new Subtask("First subtask", "First subtask description", LocalDateTime.now(),
                Duration.ofMinutes(30), epic.getId());

        // Создаём запрос на добавление подзадачи
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id подзадачи
        id = jsonObject.get("id").getAsInt();

        assertNotNull(id, "Подзадача не добавлена");

        subtask.setId(id);

        // Создаём запрос на удаление подзадачи
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем запрос на получение задачи по id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }

    @DisplayName("Операции с Subtask: POST & GET (с менеджером задач)")
    @Test
    void testAddAndUpdateWithTaskManager() throws IOException, InterruptedException {
        // Получаем менеджер задач
        TaskManager taskManager = taskServer.getTaskManager();

        // Формируем запрос на добавление эпика
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(epic)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id эпика
        Integer id = jsonObject.get("id").getAsInt();

        assertNotNull(id, "Эпик не создан");

        epic.setId(id);
        Subtask subtask = new Subtask("First subtask", "First subtask description", LocalDateTime.now(),
                Duration.ofMinutes(30), epic.getId());

        // Создаём запрос на добавление подзадачи
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id подзадачи
        id = jsonObject.get("id").getAsInt();

        assertNotNull(id, "Подзадача не добавлена");

        subtask.setId(id);

        // Получаем подзадачу по id из менеджера задач
        Optional<Subtask> receivedSubtaskOpt = taskManager.getSubtaskById(subtask.getId());

        assertTrue(receivedSubtaskOpt.isPresent(), "Подзадача не получена из менеджера задач");

        Subtask receivedSubtask = receivedSubtaskOpt.get();

        assertEquals(subtask, receivedSubtask, "ИД созданной и сохранённой задач не совпадают");
        assertEquals(subtask.getStatus(), receivedSubtask.getStatus(),
                "Статус созданной и сохранённой задач не совпадают");
        assertEquals(subtask.getName(), receivedSubtask.getName(), "Имя созданной и сохранённой задач не совпадают");
        assertEquals(subtask.getDescription(), receivedSubtask.getDescription(),
                "Описание созданной и сохранённой задач не совпадают");
        assertEquals(subtask.getType(), receivedSubtask.getType(), "Тип созданной и сохранённой задач не совпадают");
        assertEquals(subtask.getDuration(), receivedSubtask.getDuration(),
                "Длительность созданной и сохранённой задач не совпадают");
        assertEquals(subtask.getStartTime().truncatedTo(ChronoUnit.MINUTES), receivedSubtask.getStartTime(),
                "Время начала созданной и сохранённой задач не совпадают");
    }

    @DisplayName("Операции с Subtask: POST & GET (с ошибками)")
    @Test
    void testAddAndUpdateSubtaskWithErrors() throws IOException, InterruptedException {
        // Формируем запрос на добавление эпика
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(epic)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id эпика
        int id = jsonObject.get("id").getAsInt();

        epic.setId(id);
        Subtask subtask = new Subtask("First subtask", "First subtask description", LocalDateTime.now(),
                Duration.ofMinutes(30), epic.getId());

        // Создаём запрос на добавление подзадачи
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id подзадачи
        id = jsonObject.get("id").getAsInt();

        // Формируем запрос на получение подзадачи по неправильном id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/" + (id + 1)))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем запрос на добавление задачи с пересечением по времени
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Код не соответствует ожидаемому");

        // Формируем пустой запрос на добавление подзадачи
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString("{}"))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем ошибочный запрос на добавление подзадачи
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString("Subtask"))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/add"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем запрос на добавление подзадачи с неправильными URI
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/add"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код не соответствует ожидаемому");

        // Формируем запрос с необработанным методом
        request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }

    @DisplayName("Операции с Subtask: DELETE (с ошибками)")
    @Test
    void testDeleteSubtaskWithErrors() throws IOException, InterruptedException {
        // Создаём запрос на удаление задачи без указания id
        HttpRequest request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем запрос на добавление эпика
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(epic)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id эпика
        int id = jsonObject.get("id").getAsInt();

        epic.setId(id);
        Subtask subtask = new Subtask("First subtask", "First subtask description", LocalDateTime.now(),
                Duration.ofMinutes(30), epic.getId());

        // Создаём запрос на добавление подзадачи
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id подзадачи
        id = jsonObject.get("id").getAsInt();

        // Формируем запрос на удаление подзадачи с неправильным id
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/" + (id + 1)))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем запрос на удаление с неправильным URI
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/delete/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем запрос на удаление с правильным id
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Повторно направляем запрос на удаление с теми же параметрами
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }
}