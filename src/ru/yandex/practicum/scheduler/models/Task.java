package ru.yandex.practicum.scheduler.models;

import java.util.Objects;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class Task {

    protected Integer id;
    protected StatusTypes status;
    protected String name;
    protected String description;
    protected TaskTypes type;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = StatusTypes.NEW;
        this.type = TaskTypes.TASK;
    }

    public Task(Integer id, StatusTypes status, String name, String description) {
        this.id = id;
        this.status = status;
        this.name = name;
        this.description = description;
        this.type = TaskTypes.TASK;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public boolean isCompleted() {
        return !(name.isEmpty() || name.isBlank()) && !(description.isEmpty() || description.isBlank());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
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

        return Objects.equals(this.id, that.id);
    }

    @Override
    public String toString() {
        return "Id: " + id + "; Name: " + name + "; Description: " + description + "; Status: " + status.name();
    }

    public String toCSV() {
        String epicId = "";
        if (this.getClass().equals(Subtask.class)) {
            Subtask subtask = (Subtask) this;
            epicId = subtask.getEpic().getId().toString();
        }
        return id + "," + type.toString() + "," + name + "," + status.toString() + "," + description + "," + epicId;
    }
}
