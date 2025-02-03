package ru.yandex.practicum.scheduler.utils;

import java.util.HashMap;
import ru.yandex.practicum.scheduler.models.Task;

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
        if (!(tasks.containsKey(task.getId())) && task.getId() != null) {
            tasks.put(task.getId(), task);
        }
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
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

    public void setTasks(HashMap<Integer, Task> tasks) {
        this.tasks = tasks;
    }
}
