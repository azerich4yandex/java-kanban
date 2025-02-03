package ru.yandex.practicum.task_manager.models.base;

import ru.yandex.practicum.task_manager.models.enums.StatusTypes;
import ru.yandex.practicum.task_manager.utils.TaskManager;

public abstract class AbstractTask {
    Integer id;
    StatusTypes status;
    String name;
    String description;

    public AbstractTask() {
        this.id = TaskManager.getNextId();
        this.status = StatusTypes.NEW;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }

        AbstractTask that = (AbstractTask) obj;

        return this.id.equals(that.id);
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
    public String toString() {
        return "Id: " + this.getId() + "; Name: " + this.getName() + "; Description: " + this.getDescription() + "; Status: " + this.getStatus().name();
    }
}
