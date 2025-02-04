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
        if (task.getId() == null) {
            task.setId(getNextId());
        }

        if (!tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
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
        if (epic.getId() == null) {
            epic.setId(getNextId());
        }
        if (!epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
        return epic.getId();
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    public void deleteEpic(int id) {
        epics.remove(id);
    }

    public void deleteEpics() {
        epics.clear();
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
        List<Subtask> result = new ArrayList<>();

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpic() == getEpic(epicId)) {
                result.add(subtask);
            }
        }
        // return getEpic(epicId).getSubtasks();
        return result;
    }

    public int addNewSubtask(Subtask subtask) {
        if (subtask.getId() == null) {
            subtask.setId(getNextId());
        }
        if (!subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);

            Epic epic = subtask.getEpic();
            epic.addNewSubtask(subtask);
            updateEpic(epic);
        }
        return subtask.getId();
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
        }
    }

    public void deleteSubtask(int id) {
        Subtask subtask = getSubtask(id);
        Epic epic = subtask.getEpic();
        epic.deleteSubtask(subtask);

        updateEpic(epic);
        subtasks.remove(id);
    }

    public void deleteSubtasks() {
        subtasks.clear();
    }
    //</editor-fold>
}
