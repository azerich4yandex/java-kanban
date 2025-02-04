package ru.yandex.practicum.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.yandex.practicum.manager.models.Epic;
import ru.yandex.practicum.manager.models.Subtask;
import ru.yandex.practicum.manager.models.Task;

public class TaskManager {

    private int id;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    public TaskManager() {
        this.id = 0;
    }

    private int getNextId() {
        id++;
        return id;
    }

    //<editor-fold desc="Task methods">
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public int addNewTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);

        return task.getId();
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteTasks() {
        tasks.clear();
    }
    //</editor-fold>

    //<editor-fold desc="Epic methods">
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public int addNewEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);

        return epic.getId();
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic tempEpic = getEpic(epic.getId());

            tempEpic.setName(epic.getName());
            tempEpic.setDescription(epic.getDescription());

            epics.put(tempEpic.getId(), tempEpic);
        }
    }

    public void deleteEpic(int id) {
        for (Subtask subtask : getEpicSubtasks(id)) {
            deleteSubtask(subtask.getId());
        }

        epics.remove(id);
    }

    public void deleteEpics() {
        for (Epic epic : getEpics()) {
            deleteEpic(epic.getId());
        }
    }
    //</editor-fold>

    //<editor-fold desc="Subtask methods">
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public List<Subtask> getEpicSubtasks(int epicId) {
        return getEpic(epicId).getSubtasks();
    }

    public int addNewSubtask(Subtask subtask) {
        Epic epic = subtask.getEpic();

        if (getEpic(epic.getId()) != null) {
            subtask.setId(getNextId());
            epic.addNewSubtask(subtask);
            subtasks.put(subtask.getId(), subtask);
            epic.calculateStatus();

            return subtask.getId();
        }

        return 0;
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            subtask.getEpic().calculateStatus();
        }
    }

    public void deleteSubtask(int id) {
        Subtask subtask = getSubtask(id);

        if (subtask != null) {
            Epic epic = subtask.getEpic();
            epic.deleteSubtask(subtask);

            subtasks.remove(id);
            epic.calculateStatus();
        }
    }

    public void deleteSubtasks() {
        subtasks.clear();

        for (Epic epic : getEpics()) {
            epic.calculateStatus();
        }
    }
    //</editor-fold>
}
