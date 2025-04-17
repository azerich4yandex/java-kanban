package ru.yandex.practicum.scheduler.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class Task implements Comparable<Task> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");
    protected Integer rowId;
    protected StatusTypes status;
    protected String name;
    protected String description;
    protected TaskTypes type;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = StatusTypes.NEW;
        this.type = TaskTypes.TASK;
    }

    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
        this.status = StatusTypes.NEW;
        this.type = TaskTypes.TASK;
    }

    public Task(Integer rowId, StatusTypes status, String name, String description) {
        this.rowId = rowId;
        this.status = status;
        this.name = name;
        this.description = description;
        this.type = TaskTypes.TASK;
    }

    public Task(Integer rowId, StatusTypes status, String name, String description, LocalDateTime startTime,
                Duration duration) {
        this.rowId = rowId;
        this.status = status;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
        this.type = TaskTypes.TASK;
    }

    public static DateTimeFormatter getFormatter() {
        return FORMATTER;
    }

    public Integer getId() {
        return rowId;
    }

    public void setId(Integer rowId) {
        this.rowId = rowId;
    }

    public StatusTypes getStatus() {
        return status;
    }

    public void setStatus(StatusTypes status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(TaskTypes type) {
        this.type = type;
    }

    public TaskTypes getType() {
        return type;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        try {
            return startTime;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        try {
            return startTime.plus(duration);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public int hashCode() {
        return rowId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }

        Task that = (Task) obj;

        return Objects.equals(this.rowId, that.rowId);
    }

    @Override
    public String toString() {
        return "Id: " + rowId + "; Name: " + name + "; Description: " + description + "; Status: " + status.name()
                + "; Start time: " + (startTime != null ? startTime.format(FORMATTER) : "") + "; Duration: " + (
                duration != null ? duration.toString() : "") + "; End time: " + (getEndTime() != null
                ? getEndTime().format(FORMATTER) : "");
    }

    public String toCSV() {
        String epicId = "";
        String startTimeString = "";
        long durationMinutes = 0;
        if (this.getClass().equals(Subtask.class)) {
            Subtask subtask = (Subtask) this;
            epicId = subtask.getEpic().getId().toString();
        }

        if (this.getClass().equals(Task.class) || this.getClass().equals(Subtask.class)) {
            durationMinutes = duration == null ? 0 : duration.toMinutes();
            startTimeString = startTime == null ? "" : startTime.format(FORMATTER);
        }
        return rowId + "," + type.toString() + "," + name + "," + status.toString() + "," + description + "," + epicId
                + "," + startTimeString + "," + durationMinutes;
    }

    @Override
    public int compareTo(Task o) {
        // Для ожидаемой работы TreeSet
        if (rowId.equals(o.getId())) {
            return 0;
        } else if (startTime.isBefore(o.getStartTime())) {
            return -1;
        } else {
            return 1;
        }
    }
}
