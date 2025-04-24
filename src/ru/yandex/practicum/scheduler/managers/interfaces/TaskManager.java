package ru.yandex.practicum.scheduler.managers.interfaces;

import java.util.List;
import java.util.Optional;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

public interface TaskManager {

    List<Task> getHistory();

    List<Task> getTasks();

    List<Task> getPrioritizedTasks();

    Optional<Task> getTaskById(Integer id);

    Integer createTask(Task task);

    void updateTask(Task task);

    void deleteTask(Integer id);

    void deleteTasks();

    List<Epic> getEpics();

    Optional<Epic> getEpicById(Integer id);

    Integer createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpic(Integer id);

    void deleteEpics();

    List<Subtask> getSubtasks();

    Optional<Subtask> getSubtaskById(Integer id);

    List<Subtask> getEpicSubtasks(Integer epicId);

    Integer createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(Integer id);

    void deleteSubtasks();

    void calculateEpicFields(Epic epic);
}