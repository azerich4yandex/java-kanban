package ru.yandex.practicum.scheduler.servers.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;

public class BaseHttpHandler {

    protected final Gson gson;
    private final Charset charset;
    protected TaskManager taskManager;

    public BaseHttpHandler(TaskManager taskManager, Gson gson, Charset charset) {
        this.taskManager = taskManager;
        this.charset = charset;
        this.gson = gson;
    }

    protected void sendText(HttpExchange exchange, int statusCode, String text) throws IOException {
        // Получаем массив байт из сообщения.
        byte[] resp = text.getBytes(charset);
        // Устанавливаем заголовок.
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");

        try (OutputStream os = exchange.getResponseBody()) {
            // Отправляем заголовок.
            exchange.sendResponseHeaders(statusCode, resp.length);
            // Отправляем сообщение.
            os.write(resp);
        }
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, String text) throws IOException {
        JsonObject jsonObject = new JsonObject();
        // Добавляем текст ошибки.
        jsonObject.addProperty("errorCode", 404);
        jsonObject.addProperty("errorMessage", text);
        // Отправляем ответ.
        sendText(exchange, 404, gson.toJson(jsonObject));
    }

    protected void sendHasInteractions(HttpExchange exchange, String text) throws IOException {
        JsonObject jsonObject = new JsonObject();
        // Добавляем текст ошибки.
        jsonObject.addProperty("errorCode", 406);
        jsonObject.addProperty("errorMessage", text);
        // Отправляем ответ.
        sendText(exchange, 406, gson.toJson(jsonObject));
    }

    protected void sendInternalError(HttpExchange exchange, String text) throws IOException {
        JsonObject jsonObject = new JsonObject();
        // Добавляем текст ошибки.
        jsonObject.addProperty("errorCode", 500);
        jsonObject.addProperty("errorMessage", text);
        // Отправляем ответ.
        sendText(exchange, 500, gson.toJson(jsonObject));
    }

    protected String getRequestBody(HttpExchange exchange) throws IOException {
        // Если запрос передан пустым,
        if (exchange.getRequestBody() == null) {
            // то возвращаем пустую строку.
            return "";
        } else {
            // Иначе
            try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), charset))) {
                // собираем тело запроса в строку с разделителем.
                return br.lines().collect(Collectors.joining("\n"));
            }
        }
    }
} 