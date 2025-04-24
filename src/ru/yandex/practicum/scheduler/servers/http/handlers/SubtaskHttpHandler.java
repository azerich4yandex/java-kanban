package ru.yandex.practicum.scheduler.servers.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import ru.yandex.practicum.scheduler.exceptions.ManagerSaveException;
import ru.yandex.practicum.scheduler.exceptions.NotAcceptableException;
import ru.yandex.practicum.scheduler.exceptions.NotFoundException;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class SubtaskHttpHandler extends BaseHttpHandler implements HttpHandler {

    public SubtaskHttpHandler(TaskManager taskManager, Gson gson, Charset charset) {
        super(taskManager, gson, charset);
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
            // Перебираем методы.
            switch (method) {
                case "GET" -> {
                    // Проверяем путь.
                    if (pathParts.length == 2) { // GET /subtasks/
                        // Возвращаем коллекцию подзадач.
                        sendText(exchange, 200, gson.toJson(taskManager.getSubtasks()));
                    } else if (pathParts.length == 3) { // GET /subtasks/{id}
                        // Получаем id из url.
                        int id = Integer.parseInt(pathParts[2]);
                        // Получаем подзадачу по id.
                        Optional<Subtask> subtask = taskManager.getSubtaskById(id);
                        // Если подзадача получена,
                        if (subtask.isPresent()) {
                            // то отправляем её.
                            sendText(exchange, 200, gson.toJson(subtask.get()));
                        } else {
                            // Выбрасываем исключение 404.
                            throw new NotFoundException("Подзадача с id " + id + " не найдена");
                        }
                    } else {
                        // Выбрасываем исключение 500.
                        throw new Exception("Ошибка при обращении к ресурсу " + path);
                    }
                }
                case "POST" -> {
                    // Получаем тело запроса.
                    String requestBody = getRequestBody(exchange);
                    // Создаём JSON из тела запроса.
                    JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();

                    // Проверяем тип переданной сущности.
                    if (!jsonObject.has("type") || !jsonObject.get("type").getAsString()
                            .equals(TaskTypes.SUBTASK.toString())) {
                        // Выбрасываем ошибку 500.
                        throw new Exception("Передан некорректный тип сущности");
                    } else {
                        // Создаём экземпляр Subtask.
                        Subtask subtask;
                        try {
                            // Получаем экземпляр Subtask из тела запроса.
                            subtask = gson.fromJson(requestBody, Subtask.class);
                        } catch (JsonParseException e) {
                            // Выбрасываем ошибку 500.
                            throw new Exception("Некорректный формат JSON: " + e.getMessage());
                        }
                        // Проверяем путь
                        if (pathParts.length == 2) { // POST /subtasks/ (createSubtask)
                            // Добавляем подзадачу в хранилище.
                            taskManager.createSubtask(subtask);
                            // Отправляем сообщение об успехе.
                            JsonObject successful = new JsonObject();
                            successful.addProperty("message", "Подзадача успешно добавлена");
                            successful.addProperty("id", subtask.getId());
                            sendText(exchange, 201, successful.toString());
                        } else if (pathParts.length == 3) { // POST /subtasks/{id} (updateTask)
                            // Получаем id из url.
                            int id = Integer.parseInt(pathParts[2]);
                            // Получаем подзадачу по id.
                            Optional<Subtask> existingSubtask = taskManager.getSubtaskById(id);
                            // Если подзадача получена.
                            if (existingSubtask.isPresent()) {
                                // Обновляем задачу в хранилище.
                                taskManager.updateSubtask(subtask);
                                // Отправляем сообщение об успехе.
                                JsonObject successful = new JsonObject();
                                successful.addProperty("message", "Подзадача успешно обновлена.");
                                successful.addProperty("id", subtask.getId());
                                sendText(exchange, 201, successful.toString());
                            } else {
                                // Выбрасываем исключение 404.
                                throw new NotFoundException("Подзадача с id " + id + " не найдена");
                            }
                        } else {
                            // Выбрасываем исключение 500.
                            throw new Exception("Ошибка при обращении к ресурсу " + path);
                        }
                    }
                }
                case "DELETE" -> {
                    // Проверяем путь.
                    if (pathParts.length == 3) { // DELETE /subtasks/{id}
                        // Получаем id из url.
                        int id = Integer.parseInt(pathParts[2]);
                        // Получаем подзадачу по id.
                        Optional<Subtask> existingSubtask = taskManager.getSubtaskById(id);
                        // Если подзадача получена.
                        if (existingSubtask.isPresent()) {
                            // Удаляем подзадачу.
                            taskManager.deleteSubtask(existingSubtask.get().getId());
                            // Отправляем сообщение об успехе.
                            JsonObject successful = new JsonObject();
                            successful.addProperty("message", "Подзадача успешно удалена");
                            sendText(exchange, 200, successful.toString());
                        } else {
                            // Выбрасываем исключение 404.
                            throw new NotFoundException("Подзадача с id " + id + " не найдена");
                        }
                    } else {
                        // Выбрасываем исключение 500.
                        throw new Exception("Ошибка при обращении к ресурсу " + path);
                    }
                }
                // Выбрасываем исключение для других методов.
                default -> throw new Exception(
                        "Во время обработки запроса использован неизвестный метод \"" + method + "\"");
            }
        } catch (NotFoundException e) {
            // Отправляем ошибку 404
            sendNotFound(exchange, e.getMessage());
        } catch (NotAcceptableException e) {
            // Отправляем ошибку 406
            sendHasInteractions(exchange, e.getMessage());
        } catch (ManagerSaveException e) {
            // Отправляем ошибку 500 при работе с файлами.
            sendInternalError(exchange, e.getMessage());
        } catch (Exception e) {
            // Отправляем другие ошибки 500.
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }

    }
}