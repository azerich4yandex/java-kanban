package ru.yandex.practicum.scheduler.models;

import ru.yandex.practicum.scheduler.models.base.AbstractTask;

public class SubTask extends AbstractTask {

    Epic epic;

    public SubTask(Epic epic) {
        super();
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }
}
