package ru.yandex.practicum.scheduler.servers.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.Charset;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;

public class HistoryHttpHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHttpHandler(TaskManager taskManager, Gson gson, Charset charset) {
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
            if (method.equals("GET")) {
                // Проверяем путь.
                if (pathParts.length == 2) { // GET /history/
                    // Возвращаем коллекцию.
                    sendText(exchange, 200, gson.toJson(taskManager.getHistory()));
                } else {
                    // Выбрасываем исключение 500.
                    throw new Exception("Ошибка при обращении к ресурсу " + path);
                }
            } else {
                // Выбрасываем исключение для других методов.
                throw new Exception("Во время обработки запроса использован неизвестный метод \"" + method + "\"");
            }
        } catch (Exception e) {
            // Отправляем другие ошибки 500
            sendInternalError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}
