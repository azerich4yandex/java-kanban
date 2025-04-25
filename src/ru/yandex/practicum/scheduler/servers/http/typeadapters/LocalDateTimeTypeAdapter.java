package ru.yandex.practicum.scheduler.servers.http.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import ru.yandex.practicum.scheduler.models.Task;

public class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        if (localDateTime != null) {
            jsonWriter.value(localDateTime.format(Task.getFormatter()));
        } else {
            jsonWriter.value("");
        }
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        String stringValue = null;
        if (jsonReader.hasNext()) {
            stringValue = jsonReader.nextString();
        }
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        } else {
            return LocalDateTime.parse(stringValue, Task.getFormatter());
        }
    }
}
