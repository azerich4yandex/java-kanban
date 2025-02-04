package ru.yandex.practicum.manager.models;

import java.util.Objects;
import ru.yandex.practicum.manager.models.enums.StatusTypes;

public class Task {
    protected Integer id;
    protected StatusTypes status;
    protected String name;
    protected String description;

    public Task() {
        this.status = StatusTypes.NEW;
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

        Task that = (Task)obj;

        return Objects.equals(this.id, that.id);
    }

    @Override
    public String toString() {
        return "Id: " + id + "; Name: " + name + "; Description: " + description + "; Status: " + status.name();
    }
}
