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
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class TaskHttpHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHttpHandler(TaskManager taskManager, Gson gson, Charset charset) {
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
                    if (pathParts.length == 2) { // GET /tasks/
                        // Возвращаем коллекцию задач.
                        sendText(exchange, 200, gson.toJson(taskManager.getTasks()));
                    } else if (pathParts.length == 3) { // GET /tasks/{id}
                        // Получаем id из url.
                        int id = Integer.parseInt(pathParts[2]);
                        // Получаем задачу по id.
                        Optional<Task> taskOpt = taskManager.getTaskById(id);
                        // Если задача получена,
                        if (taskOpt.isPresent()) {
                            // то отправляем её.
                            sendText(exchange, 200, gson.toJson(taskOpt.get()));
                        } else {
                            // Иначе выбрасываем ошибку 404.
                            throw new NotFoundException("Задача с id " + id + " не найдена");
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
                            .equals(TaskTypes.TASK.toString())) {
                        // Выбрасываем ошибку 500.
                        throw new Exception("Передан некорректный тип сущности");
                    } else {
                        // Создаём экземпляр Task.
                        Task task;
                        try {
                            // Получаем экземпляр Task из тела запроса.
                            task = gson.fromJson(requestBody, Task.class);
                        } catch (JsonParseException e) {
                            // Отправляем ошибку 400.
                            throw new IllegalArgumentException("Некорректный формат JSON: " + e.getMessage());
                        }
                        // Проверяем путь.
                        if (pathParts.length == 2) { // POST /tasks/ (createTask)
                            // Добавляем задачу в хранилище.
                            taskManager.createTask(task);
                            // Отправляем сообщение об успехе.
                            JsonObject successful = new JsonObject();
                            successful.addProperty("message", "Задача успешно добавлена");
                            successful.addProperty("id", task.getId());
                            sendText(exchange, 201, successful.toString());
                        } else if (pathParts.length == 3) { // POST /tasks/{id} (updateTask)
                            // Получаем id из url.
                            int id = Integer.parseInt(pathParts[2]);
                            // Получаем задачу по id.
                            Optional<Task> existingTask = taskManager.getTaskById(id);
                            // Если задача получена.
                            if (existingTask.isPresent()) {
                                // Обновляем задачу в хранилище.
                                taskManager.updateTask(task);
                                // Отправляем сообщение об успехе.
                                JsonObject successful = new JsonObject();
                                successful.addProperty("message", "Задача успешно обновлена.");
                                successful.addProperty("id", task.getId());
                                sendText(exchange, 201, successful.toString());
                            } else {
                                // Отправляем ошибку 404.
                                throw new NotFoundException("Задача с id " + id + " не найдена");
                            }
                        } else {
                            // Выбрасываем исключение 500.
                            throw new Exception("Ошибка при обращении к ресурсу " + path);
                        }
                    }
                }
                case "DELETE" -> {
                    // Проверяем путь.
                    if (pathParts.length == 3) { // DELETE /tasks/{id}
                        // Получаем id из url.
                        int id = Integer.parseInt(pathParts[2]);
                        // Получаем задачу по id.
                        Optional<Task> existingTask = taskManager.getTaskById(id);
                        // Если задача получена.
                        if (existingTask.isPresent()) {
                            // Удаляем задачу.
                            taskManager.deleteTask(existingTask.get().getId());
                            // Отправляем сообщение об успехе.
                            JsonObject successful = new JsonObject();
                            successful.addProperty("message", "Задача успешно удалена");
                            sendText(exchange, 200, successful.toString());
                        } else {
                            // Отправляем ошибку 404.
                            throw new NotFoundException("Задача с id " + id + " не найдена");
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
            // Отправляем ошибку 500 при работе с файлами
            sendInternalError(exchange, e.getMessage());
        } catch (Exception e) {
            // Отправляем другие ошибки 500
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e);
        }
    }
}