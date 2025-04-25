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
import ru.yandex.practicum.scheduler.servers.http.typetokens.EpicListTypeToken;
import ru.yandex.practicum.scheduler.servers.http.typetokens.SubtaskListTypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpicHttpHandlerTest {

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

    @DisplayName("Операции с Epic: POST & GET (без ошибок)")
    @Test
    void testAddAndUpdateEpicNoErrors() throws IOException, InterruptedException {
        // Создаём запрос на добавление
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(epic)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics"))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        // Получаем id
        int id = jsonObject.get("id").getAsInt();

        epic.setId(id);

        // Формируем запрос на получение эпика по id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/" + id))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        Epic receivedEpic = taskServer.getGson().fromJson(jsonObject, Epic.class);

        assertEquals(epic, receivedEpic, "ИД созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getStatus(), receivedEpic.getStatus(),
                "Статус созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getName(), receivedEpic.getName(), "Имя созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getDescription(), receivedEpic.getDescription(),
                "Описание созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getType(), receivedEpic.getType(), "Тип созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getSubtaskIds(), receivedEpic.getSubtaskIds(),
                "Списки созданного и сохранённого эпиков не совпадают");

        // Формируем запрос на получение списка эпиков
        request = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        List<Epic> expectedEpics = new ArrayList<>(List.of(epic));
        List<Epic> receivedEpics = taskServer.getGson().fromJson(jsonArray, new EpicListTypeToken().getType());

        assertFalse(receivedEpics.isEmpty(), "Не получен список эпиков");
        assertEquals(expectedEpics, receivedEpics, "Сохранённый и полученный списки не совпадают");
        assertEquals(epic, receivedEpics.getFirst(), "ИД сохранённого и полученного эпика не совпадают");

        // Формируем запрос на добавление подзадачи
        Subtask subtask = new Subtask("First subtask", "Second subtask", LocalDateTime.now(), Duration.ofMinutes(5),
                epic.getId());
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(subtask)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        id = jsonObject.get("id").getAsInt();

        subtask.setId(id);

        // Формируем запрос на получение подзадач эпика
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/" + epic.getId() + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        List<Subtask> expectedSubtasks = new ArrayList<>(List.of(subtask));
        List<Subtask> receivedSubtasks = taskServer.getGson().fromJson(jsonArray, new SubtaskListTypeToken().getType());

        assertFalse(receivedSubtasks.isEmpty(), "Список подзадач не получен");
        assertEquals(subtask, receivedSubtasks.getFirst(), "Сохранённая и полученная подзадачи не совпадают");
        assertEquals(expectedSubtasks, receivedSubtasks, "Сохранённый и полученный списки подзадач не совпадают");

        // Формируем запрос на получение эпика по id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/" + epic.getId()))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        receivedEpic = taskServer.getGson().fromJson(jsonObject, Epic.class);

        assertEquals(epic, receivedEpic, "ИД созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getStatus(), receivedEpic.getStatus(),
                "Статус созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getName(), receivedEpic.getName(), "Имя созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getDescription(), receivedEpic.getDescription(),
                "Описание созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getType(), receivedEpic.getType(), "Тип созданного и сохранённого эпиков не совпадают");
    }

    @DisplayName("Операции с Epic: DELETE (без ошибок)")
    @Test
    void testDeleteEpicNoErrors() throws IOException, InterruptedException {
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

        // Создаём запрос на удаление эпика
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код не соответствует ожидаемому");

        // Формируем запрос на получение эпика по id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/" + id))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }

    @DisplayName("Операции с Epic: POST & GET (с менеджером задач)")
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

        // Получаем эпик по id из менеджера задач
        Optional<Epic> receivedEpicOpt = taskManager.getEpicById(id);

        assertTrue(receivedEpicOpt.isPresent(), "Эпик не получен из менеджера задач");

        Epic receivedEpic = receivedEpicOpt.get();

        assertEquals(epic, receivedEpic, "ИД созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getStatus(), receivedEpic.getStatus(),
                "Статус созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getName(), receivedEpic.getName(), "Имя созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getDescription(), receivedEpic.getDescription(),
                "Описание созданного и сохранённого эпиков не совпадают");
        assertEquals(epic.getType(), receivedEpic.getType(), "Тип созданного и сохранённого эпиков не совпадают");
    }

    @DisplayName("Операции с Epic: POST & GET (с ошибками)")
    @Test
    void testAddAndUpdateEpicWithErrors() throws IOException, InterruptedException {
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

        // Формируем запрос на получение эпика с неправильным id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/" + (id + 1)))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем пустой запрос на добавление эпика
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson("{}")))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код не соответствует ожидаемому");

        // Формируем ошибочный запрос на добавление эпика
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString("Epic"))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код не соответствует ожидаемому");

        // Формируем запрос на добавление эпика с неправильным URI
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(epic)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/add"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код не соответствует ожидаемому");

        // Формируем запрос с необработанным методом
        request = HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(epic)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/add"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код не соответствует ожидаемому");

        // Формируем запрос на получение списка подзадач с неправильным id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/" + (id + 1) + "/subtasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }

    @DisplayName("Операции с Epic: DELETE (с ошибками)")
    @Test
    void testDeleteEpicWithErrors() throws IOException, InterruptedException {
        // Создаём запрос на удаление эпика без указания id
        HttpRequest request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/"))
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

        // Формируем запрос на удаление эпика с неправильным URI
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/" + (id + 1)))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем запрос на удаление эпика с неправильным URI
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/delete/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Формируем запрос на удаление эпика с правильным id
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/epics/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Повторно направляем запрос на удаление с теми же параметрами
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }
}
