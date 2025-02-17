package ru.yandex.practicum.scheduler.managers;

public class Managers {
    public TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
