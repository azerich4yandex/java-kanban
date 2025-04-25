package ru.yandex.practicum.scheduler.models;

import java.time.Duration;
import java.time.LocalDateTime;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class Subtask extends Task {

    private Integer epicId;

    public Subtask(String name, String description, Integer epicId) {
        super(name, description);
        this.epicId = epicId;
        this.type = TaskTypes.SUBTASK;
    }

    public Subtask(String name, String description, LocalDateTime startTime, Duration duration, int epicId) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
        this.type = TaskTypes.SUBTASK;
    }

    public Subtask(Integer id, StatusTypes status, String name, String description, int epicId) {
        super(id, status, name, description);
        this.type = TaskTypes.SUBTASK;
        this.epicId = epicId;
    }

    public Subtask(Integer id, StatusTypes status, String name, String description, LocalDateTime startTime,
                   Duration duration, int epicId) {
        super(id, status, name, description, startTime, duration);
        this.type = TaskTypes.SUBTASK;
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }
}
