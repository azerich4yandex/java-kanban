package ru.yandex.practicum.scheduler.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

public class InMemoryTaskManager implements TaskManager {

    private final HistoryManager historyManager;
    private int id = 0;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public int getNextId() {
        id++;
        return id;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //<editor-fold desc="Task methods">
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.addToHistory(task);
        return task;
    }

    @Override
    public int addNewTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);

        return task.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteTasks() {
        tasks.clear();
    }
    //</editor-fold>

    //<editor-fold desc="Epic methods">
    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = getEpicInternal(id);
        historyManager.addToHistory(epic);
        return epic;
    }

    private Epic getEpicInternal(int id) {
        Epic epic = epics.get(id);
        return epic;
    }

    @Override
    public int addNewEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);

        return epic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic tempEpic = getEpicInternal(epic.getId());

            tempEpic.setName(epic.getName());
            tempEpic.setDescription(epic.getDescription());

            epics.put(tempEpic.getId(), tempEpic);
        }
    }

    @Override
    public void deleteEpic(int id) {
        for (Subtask subtask : getEpicSubtasks(id)) {
            subtasks.remove(subtask.getId());
        }
        epics.remove(id);
    }

    @Override
    public void deleteEpics() {
        subtasks.clear();
        epics.clear();
    }
    //</editor-fold>

    //<editor-fold desc="Subtask methods">
    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = getSubtaskInternal(id);
        historyManager.addToHistory(subtask);
        return subtask;
    }

    private Subtask getSubtaskInternal(int id) {
        Subtask subtask = subtasks.get(id);
        return subtask;
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = getEpicInternal(epicId);

        if (epic != null) {
            return epic.getSubtasks();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        Epic epic = subtask.getEpic();

        if (getEpicInternal(epic.getId()) != null) {
            subtask.setId(getNextId());
            epic.addNewSubtask(subtask);
            subtasks.put(subtask.getId(), subtask);
            epic.calculateStatus();

            return subtask.getId();
        }

        return 0;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);

            Epic epic = getEpicInternal(subtask.getEpic().getId());
            epic.updateSubtask(subtask);
            epic.calculateStatus();
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = getSubtask(id);

        if (subtask != null) {
            Epic epic = getEpicInternal(subtask.getEpic().getId());
            epic.deleteSubtask(subtask);

            subtasks.remove(id);
            epic.calculateStatus();
        }
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
    }
    //</editor-fold>
}
