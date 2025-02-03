package ru.yandex.practicum.task_manager.utils;

import java.util.HashMap;
import ru.yandex.practicum.task_manager.models.Epic;
import ru.yandex.practicum.task_manager.models.SubTask;
import ru.yandex.practicum.task_manager.models.Task;

public class TaskManager {

    static int uniqueId;
    HashMap<Integer, Task> tasks;
    HashMap<Integer, Epic> epics;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
    }

    public static Integer getNextUniqueId() {
        uniqueId++;
        return uniqueId;
    }

    //<editor-fold desc="Tasks collection methods">
    public HashMap<Integer, Task> getTasks() {
        return this.tasks;
    }

    public Task getTask(Integer id) {
        if (id != null) {
            return this.tasks.get(id);
        }
        return null;
    }

    public void addTask(Task task) {
        if (!(tasks.containsKey(task.uniqueId)) && task.uniqueId != null) {
            this.setTask(task);
        }
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.uniqueId)) {
            this.setTask(task);
        }
    }

    void setTask(Task task) {
        this.tasks.put(task.uniqueId, task);
    }

    public void removeTask(Integer id) {
        if (this.tasks != null) {
            this.tasks.remove(id);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Epic collection methods">
    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public Epic getEpic(Integer id) {
        if (id != null) {
            return epics.get(id);
        }
        return null;
    }

    public void addEpic(Epic epic) {
        if (!(epics.containsKey(epic.uniqueId)) && epic.uniqueId != null) {
            setEpic(epic);
        }
    }

    public void updateEpic(Epic epic) {
        if (this.epics.containsKey(epic.uniqueId)) {
            this.epics.put(epic.uniqueId, epic);
        }
    }

    void setEpic(Epic epic) {
        this.epics.put(epic.uniqueId, epic);
    }

    public void removeEpic(Integer id) {
        if (this.epics != null) {
            this.epics.remove(id);
        }
    }
    //</editor-fold>
}
