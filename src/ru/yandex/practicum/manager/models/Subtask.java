package ru.yandex.practicum.manager.models;

public class Subtask extends Task {

    private final Epic epic;

    public Subtask(Epic epic) {
        super();
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }
}
