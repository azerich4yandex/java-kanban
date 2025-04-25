package ru.yandex.practicum.scheduler.servers.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;

public class PrioritizedHttpHandler extends BaseHttpHandler {

    public PrioritizedHttpHandler(TaskManager taskManager, Gson gson) {
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
            if (method.equals("GET")) {
                handleGet(pathParts, exchange);
            } else {
                // Выбрасываем исключение для других методов.
                throw new Exception("Во время обработки запроса использован неизвестный метод \"" + method + "\"");
            }
        } catch (Exception e) {
            // Отправляем другие ошибки 500
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(String[] pathParts, HttpExchange exchange) throws Exception {
        // Проверяем путь
        if (pathParts.length == 2) { // GET /prioritized/
            // Возвращаем коллекцию
            sendText(exchange, 200, gson.toJson(taskManager.getPrioritizedTasks()));
        } else {
            // Выбрасываем исключение 500.
            throw new Exception("Ошибка при обращении к ресурсу " + String.join("/", pathParts));
        }
    }
}
