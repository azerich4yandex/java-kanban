package ru.yandex.practicum.scheduler.models;

public class Subtask extends Task {

    private final Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }
}
