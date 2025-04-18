package ru.yandex.practicum.scheduler.managers.interfaces;

import java.util.List;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

public interface TaskManager {

    List<Task> getHistory();

    List<Task> getTasks();

    List<Task> getPrioritizedTasks();

    Task getTask(int id);

    int addNewTask(Task task);

    void updateTask(Task task);

    void deleteTask(int id);

    void deleteTasks();

    List<Epic> getEpics();

    Epic getEpic(int id);

    int addNewEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpic(int id);

    void deleteEpics();

    List<Subtask> getSubtasks();

    Subtask getSubtask(int id);

    List<Subtask> getEpicSubtasks(int epicId);

    int addNewSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    void deleteSubtasks();
}