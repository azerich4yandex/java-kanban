package ru.yandex.practicum.task_manager.models;

public class SubTask extends Task{

    Epic epic;

    public SubTask(Epic epic) {
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }
}
