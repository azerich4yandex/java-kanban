package ru.yandex.practicum.scheduler.servers.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Optional;
import ru.yandex.practicum.scheduler.exceptions.ManagerSaveException;
import ru.yandex.practicum.scheduler.exceptions.NotFoundException;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class EpicHttpHandler extends BaseHttpHandler {

    public EpicHttpHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Получаем метод.
        String method = exchange.getRequestMethod();
        // Получаем путь.
        String path = exchange.getRequestURI().getPath();
        // Разбираем путь на составляющие.
        String[] pathParts = path.split("/");

        try {
            // Перебираем методы
            switch (method) {
                case "GET" -> handleGet(pathParts, exchange);
                case "POST" -> handlePost(pathParts, exchange);
                case "DELETE" -> handleDelete(pathParts, exchange);
                // Выбрасываем исключение для других методов.
                default -> throw new Exception(
                        "Во время обработки запроса использован неизвестный метод \"" + method + "\"");
            }
        } catch (NotFoundException e) {
            // Отправляем ошибку 404
            sendNotFound(exchange, e.getMessage());
        } catch (ManagerSaveException e) {
            // Отправляем ошибку 500 при работе с файлами.
            sendInternalError(exchange, e.getMessage());
        } catch (Exception e) {
            // Отправляем другие ошибки 500.
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleDelete(String[] pathParts, HttpExchange exchange) throws Exception {
        // Проверяем путь.
        if (pathParts.length == 3) { // DELETE /epics/{id}
            // Получаем id из url.
            int id = Integer.parseInt(pathParts[2]);
            // Получаем эпик по id.
            Optional<Epic> existingEpic = taskManager.getEpicById(id);
            // Если эпик получен.
            if (existingEpic.isPresent()) {
                // Удаляем эпик.
                taskManager.deleteEpic(existingEpic.get().getId());
                // Отправляем сообщение об успехе.
                JsonObject successful = new JsonObject();
                successful.addProperty("message", "Эпик успешно удалён");
                sendText(exchange, 200, successful.toString());
            } else {
                // Выбрасываем исключение 404
                throw new NotFoundException("Эпик с id " + id + " не найден");
            }
        } else {
            // Выбрасываем исключение 500.
            throw new Exception("Ошибка при обращении к ресурсу " + String.join("/", pathParts));
        }
    }

    private void handlePost(String[] pathParts, HttpExchange exchange) throws Exception {
        // Получаем тело запроса.
        String requestBody = getRequestBody(exchange);
        // Создаём JSON из тела запроса.
        JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();

        // Проверяем тип переданной сущности.
        if (!jsonObject.has("type") || !jsonObject.get("type").getAsString()
                .equals(TaskTypes.EPIC.toString())) {
            // Выбрасываем ошибку 500.
            throw new Exception("Передан некорректный тип сущности");
        } else {
            // Создаём экземпляр Epic.
            Epic epic;
            try {
                // Получаем экземпляр Epic из тела запроса.
                epic = gson.fromJson(requestBody, Epic.class);
            } catch (JsonParseException e) {
                // Отправляем ошибку 400.
                throw new IllegalArgumentException("Некорректный формат JSON: " + e.getMessage());
            }
            // Проверяем путь.
            if (pathParts.length == 2) { // POST /epics/ (createEpic)
                // Добавляем эпик в хранилище.
                taskManager.createEpic(epic);
                // Отправляем сообщение об успехе.
                JsonObject successful = new JsonObject();
                successful.addProperty("message", "Эпик успешно добавлен");
                successful.addProperty("id", epic.getId());
                sendText(exchange, 201, successful.toString());
            } else {
                // Выбрасываем исключение 500.
                throw new Exception("Ошибка при обращении к ресурсу " + String.join("/", pathParts));
            }
        }
    }

    private void handleGet(String[] pathParts, HttpExchange exchange) throws Exception {
        // Проверяем путь.
        if (pathParts.length == 2) { // GET /epics/
            // Возвращаем коллекцию задач.
            sendText(exchange, 200, gson.toJson(taskManager.getEpics()));
        } else if (pathParts.length == 3) { // GET /epics/{id}
            // Получаем id из url.
            int id = Integer.parseInt(pathParts[2]);
            // Получаем эпик по id.
            Optional<Epic> epicOpt = taskManager.getEpicById(id);
            // Если эпик найден,
            if (epicOpt.isPresent()) {
                // то отправляем его
                sendText(exchange, 200, gson.toJson(epicOpt.get()));
            } else {
                // Иначе выбрасываем исключение
                throw new NotFoundException("Эпик с id " + id + " не найден");
            }
        } else if (pathParts.length == 4) { // GET /epics/{id}/subtasks
            // Получаем id из url
            int id = Integer.parseInt(pathParts[2]);
            // Получаем эпик по id
            Optional<Epic> epicOpt = taskManager.getEpicById(id);
            // Если эпик найден
            if (epicOpt.isPresent()) {
                // Отправляем коллекцию подзадач
                sendText(exchange, 200, gson.toJson(taskManager.getEpicSubtasks(epicOpt.get().getId())));
            } else {
                throw new NotFoundException("Эпик с id " + id + " не найден");
            }
        } else {
            // Выбрасываем исключение 500.
            throw new Exception("Ошибка при обращении к ресурсу " + String.join("/", pathParts));
        }
    }
}