package ru.yandex.practicum.scheduler.models;

import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class Subtask extends Task {

    private final Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
        this.type = TaskTypes.SUBTASK;
    }

    public Subtask(Epic epic) {
        super();
        this.epic = epic;
        this.type = TaskTypes.SUBTASK;
    }

    public Epic getEpic() {
        return epic;
    }
}
