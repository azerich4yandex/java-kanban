package ru.yandex.practicum.task_manager.utils;

import java.util.HashMap;
import ru.yandex.practicum.task_manager.models.Epic;
import ru.yandex.practicum.task_manager.models.SubTask;
import ru.yandex.practicum.task_manager.models.Task;

public class TaskManager {

    static int id;
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, SubTask> subTasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();

    public static Integer getNextId() {
        id++;
        return id;
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
        if (!(tasks.containsKey(task.id)) && task.id != null) {
            this.setTask(task);
        }
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.id)) {
            this.setTask(task);
        }
    }

    void setTask(Task task) {
        this.tasks.put(task.id, task);
    }

    public void removeTask(Integer id) {
        if (this.tasks != null) {
            this.tasks.remove(id);
        }
    }
    //</editor-fold>

    //<editor-fold desc="SubTasks collection methods">
    public HashMap<Integer, SubTask> getSubTasks() {
        return this.subTasks;
    }

    public SubTask getSubTask(Integer id) {
        if (id != null) {
            return this.subTasks.get(id);
        }
        return null;
    }

    public void addSubTask(SubTask subTask) {
        if (!(subTasks.containsKey(subTask.id)) && subTask.id != null) {
            this.setSubTask(subTask);
        }
    }

    public void updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.id)) {
            this.setSubTask(subTask);
        }
    }

    void setSubTask(SubTask subTask) {
        this.subTasks.put(subTask.id, subTask);
    }

    public void removeSubtask(Integer id) {
        if (this.subTasks != null) {
            this.subTasks.remove(id);
        }
    }
    //</editor-fold>

    public HashMap<Integer, Epic> getEpics() {
        return this.epics;
    }

    public Epic getEpic(Integer id) {
        if (id != null) {
            return this.epics.get(id);
        }
        return null;
    }

    public void addEpic(Epic epic) {
        if (!(epics.containsKey(epic.id)) && epic.id != null) {
            this.setEpic(epic);
        }
    }

    public void updateEpic(Epic epic) {
        if (this.epics.containsKey(epic.id)) {
            this.epics.put(epic.id, epic);
        }
    }

    void setEpic(Epic epic) {
        this.epics.put(epic.id, epic);
    }

    public void removeEpic(Integer id) {
        if (this.epics != null) {
            this.epics.remove(id);
        }
    }
}
