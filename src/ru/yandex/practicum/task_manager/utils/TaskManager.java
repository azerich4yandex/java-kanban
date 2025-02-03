package ru.yandex.practicum.task_manager.utils;

import java.util.HashMap;
import ru.yandex.practicum.task_manager.models.Task;

public class TaskManager {

    static int id;
    HashMap<Integer, Task> tasks;

    public TaskManager() {
        this.tasks = new HashMap<>();
    }

    public static Integer getNextId() {
        id++;
        return id;
    }

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public Task getTask(Integer id) {
        if (id != null) {
            return this.tasks.get(id);
        }
        return null;
    }

    public void addTask(Task task) {
        if (!(tasks.containsKey(task.id)) && task.id != null) {
            tasks.put(task.id, task);
        }
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.id)) {
            tasks.put(task.id, task);
        }
    }

    public void removeTask(Integer id) {
        if (this.tasks != null) {
            this.tasks.remove(id);
        }
    }

    public void clearTasks() {
        tasks.clear();
    }
}
