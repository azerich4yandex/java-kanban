package ru.yandex.practicum.task_manager.models;

import ru.yandex.practicum.task_manager.models.enums.StatusTypes;
import ru.yandex.practicum.task_manager.utils.TaskManager;

public class Task {

    public int id;
    public String name;
    public String description;
    public StatusTypes status;

    public Task() {
        this.id = TaskManager.getNextId();
    }

    @Override
    public int hashCode() {
        return id;
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

        Task that = (Task) obj;

        return this.id == that.id;
    }
}
