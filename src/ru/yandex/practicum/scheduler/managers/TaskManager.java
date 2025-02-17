package ru.yandex.practicum.scheduler.managers;

import java.util.List;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

public interface TaskManager {

    int getNextId();

    //<editor-fold desc="Task methods">
    List<Task> getTasks();

    Task getTask(int id);

    int addNewTask(Task task);

    void updateTask(Task task);

    void deleteTask(int id);

    void deleteTasks();
    //</editor-fold>

    //<editor-fold desc="Epic methods">
    List<Epic> getEpics();

    Epic getEpic(int id);

    int addNewEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpic(int id);

    void deleteEpics();
    //</editor-fold>

    //<editor-fold desc="Subtask methods">
    List<Subtask> getSubtasks();

    Subtask getSubtask(int id);

    List<Subtask> getEpicSubtasks(int epicId);

    int addNewSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    void deleteSubtasks();
    //</editor-fold>

    //<editor-fold desc="History methods">
    void addToHistory(Task task);

    List<Task> getHistory();
    //</editor-fold>
}