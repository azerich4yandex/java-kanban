package ru.yandex.practicum.scheduler.models;

import ru.yandex.practicum.scheduler.models.enums.StatusTypes;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class Subtask extends Task {

    private final Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
        this.type = TaskTypes.SUBTASK;
    }

    public Subtask(Integer id, StatusTypes status, String name, String description, Epic epic) {
        super(id, status, name, description);
        this.type = TaskTypes.SUBTASK;
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }
}
