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
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.servers.http.HttpTaskServer;
import ru.yandex.practicum.scheduler.servers.http.typetokens.TaskListTypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskHttpHandlerTest {

    private HttpTaskServer taskServer;
    private HttpClient client;
    private Task task;

    @BeforeEach
    void init() throws IOException {
        taskServer = new HttpTaskServer();
        client = HttpClient.newHttpClient();
        task = new Task("Task", "Task description", LocalDateTime.now(), Duration.ofMinutes(5));
        taskServer.start();
    }

    @AfterEach
    void halt() {
        taskServer.getTaskManager().deleteTasks();
        taskServer.getTaskManager().deleteSubtasks();
        taskServer.getTaskManager().deleteEpics();
        taskServer.stop();
    }

    @DisplayName("Операции с Task: POST & GET (без ошибок)")
    @Test
    void testAddAndUpdateTaskNoErrors() throws IOException, InterruptedException {
        // Создаём запрос на добавление
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks"))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        // Получаем id
        Integer id = jsonObject.get("id").getAsInt();

        assertNotNull(id, "Задача не добавлена");

        task.setId(id);

        // Получаем задачу по id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/" + id))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        Task receivedTask = taskServer.getGson().fromJson(jsonObject, Task.class);

        assertEquals(task, receivedTask, "ИД созданной и сохранённой задач не совпадают");
        assertEquals(task.getStatus(), receivedTask.getStatus(), "Статус созданной и сохранённой задач не совпадают");
        assertEquals(task.getName(), receivedTask.getName(), "Имя созданной и сохранённой задач не совпадают");
        assertEquals(task.getDescription(), receivedTask.getDescription(),
                "Описание созданной и сохранённой задач не совпадают");
        assertEquals(task.getType(), receivedTask.getType(), "Тип созданной и сохранённой задач не совпадают");
        assertEquals(task.getDuration(), receivedTask.getDuration(),
                "Длительность созданной и сохранённой задач не совпадают");
        assertEquals(task.getStartTime().truncatedTo(ChronoUnit.MINUTES), receivedTask.getStartTime(),
                "Время начала созданной и сохранённой задач не совпадают");

        // Получаем список задач
        request = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks"))
                .header("Content-Type", "application/json").build();
        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
        List<Task> expectedTasks = new ArrayList<>(List.of(task));
        List<Task> receivedTasks = taskServer.getGson().fromJson(jsonArray, new TaskListTypeToken().getType());

        assertFalse(receivedTasks.isEmpty(), "Не получен список задач");
        assertEquals(expectedTasks, receivedTasks, "Сохранённый и полученный списки не совпадают");
        assertEquals(task, receivedTasks.getFirst(), "ИД сохранённой и полученной задач не совпадают");

        // Обновляем задачу
        task.setName("New name");
        task.setDescription("New description");
        task.setStartTime(LocalDateTime.now().plusMinutes(30));
        task.setDuration(Duration.ofMinutes(25));

        // Создаём запрос на обновление задачи
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/" + task.getId())).build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        assertEquals(id, jsonObject.get("id").getAsInt(), "ИД обновлённой и сохранённой задачи не совпадают");
    }

    @DisplayName("Операции с Task: DELETE (без ошибок)")
    @Test
    void testDeleteTaskNoErrors() throws IOException, InterruptedException {
        // Создаём запрос на добавление задачи
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id
        Integer id = jsonObject.get("id").getAsInt();

        assertNotNull(id, "Задача не добавлена");

        // Создаём запрос на удаление задачи
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Создаём запрос на получение задачи по id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }

    @DisplayName("Операции с Task: POST & GET (с менеджером задач)")
    @Test
    void testAddAndUpdateWithTaskManager() throws IOException, InterruptedException {
        // Получаем менеджер задач
        TaskManager taskManager = taskServer.getTaskManager();

        // Формируем запрос на добавление
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id из ответа
        Integer id = jsonObject.get("id").getAsInt();

        assertNotNull(id, "Задача не добавлена");

        task.setId(id);

        // Получаем задачу по id из менеджера задач
        Optional<Task> receivedTaskOpt = taskManager.getTaskById(id);

        assertTrue(receivedTaskOpt.isPresent(), "Задача не получена из менеджера задач");

        Task receivedTask = receivedTaskOpt.get();

        assertEquals(task, receivedTask, "ИД созданной и сохранённой задач не совпадают");
        assertEquals(task.getStatus(), receivedTask.getStatus(), "Статус созданной и сохранённой задач не совпадают");
        assertEquals(task.getName(), receivedTask.getName(), "Имя созданной и сохранённой задач не совпадают");
        assertEquals(task.getDescription(), receivedTask.getDescription(),
                "Описание созданной и сохранённой задач не совпадают");
        assertEquals(task.getType(), receivedTask.getType(), "Тип созданной и сохранённой задач не совпадают");
        assertEquals(task.getDuration(), receivedTask.getDuration(),
                "Длительность созданной и сохранённой задач не совпадают");
        assertEquals(task.getStartTime().truncatedTo(ChronoUnit.MINUTES), receivedTask.getStartTime(),
                "Время начала созданной и сохранённой задач не совпадают");
    }

    @DisplayName("Операции с Task: POST & GET (с ошибками)")
    @Test
    void testAddAndUpdateTaskWithErrors() throws IOException, InterruptedException {
        // Создаём запрос на добавление задачи
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Получаем id
        int id = jsonObject.get("id").getAsInt();

        // Создаём запрос на получение задачи по неправильному id
        request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/" + (id + 1)))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Создаём запрос на добавление задачи с пересечением по времени выполнения
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Создаём пустой запрос на добавление задачи
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString("{}"))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Создаём ошибочный запрос на добавление задачи
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString("Task"))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Создаём запрос на добавление задачи с неправильным URI
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/add"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Создаём запрос с необработанным методом
        request = HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/add"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }

    @DisplayName("Операции с Task: DELETE (с ошибками)")
    @Test
    public void testDeleteTaskWithErrors() throws IOException, InterruptedException {
        // Создаём запрос на удаление задачи без указания id
        HttpRequest request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Создаём запрос на добавление задачи
        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(taskServer.getGson().toJson(task)))
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/"))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Парсим ответ
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();

        assertNotEquals(0, id, "Задача не добавлена");

        // Создаём запрос на удаление задачи с неправильным id
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/" + (id + 1)))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Создаём запрос на удаление с неправильным URI
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/delete/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Создаём запрос на удаление с правильным id
        request = HttpRequest.newBuilder().DELETE()
                .uri(URI.create("http://localhost:" + taskServer.getPort() + "/tasks/" + id))
                .header("Content-Type", "application/json").build();

        // Получаем ответ
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не соответствует ожидаемому");

        // Повторно направляем запрос на удаление с теми же параметрами
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не соответствует ожидаемому");
    }
}
