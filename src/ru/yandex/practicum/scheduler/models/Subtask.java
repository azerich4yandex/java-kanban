package ru.yandex.practicum.scheduler.models;

import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class Subtask extends Task {

    private final Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
        type = TaskTypes.SUBTASK;
    }

    public Epic getEpic() {
        return epic;
    }
}
