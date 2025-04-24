package ru.yandex.practicum.scheduler.servers.http.typeadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Duration;

public class DurationTypeAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (duration == null) {
            jsonWriter.value("");
        } else {
            jsonWriter.value(duration.toMinutes());
        }
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        String stringValue = jsonReader.nextString();
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        } else {
            return Duration.ofMinutes(Long.parseLong(stringValue));
        }

    }
}
