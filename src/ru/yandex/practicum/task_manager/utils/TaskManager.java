package ru.yandex.practicum.task_manager.utils;

public class TaskManager {

    static int id;

    public static int getNextId() {
        id++;
        return id;
    }
}
