package ru.yandex.practicum.scheduler.managers;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
