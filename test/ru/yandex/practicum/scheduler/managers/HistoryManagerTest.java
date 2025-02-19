package ru.yandex.practicum.scheduler.managers;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryManagerTest {

    private static HistoryManager historyManager;
    private static TaskManager taskManager;

    @BeforeEach
    void createEntities() {
        historyManager = Managers.getDefaultHistory();
        taskManager = new InMemoryTaskManager(historyManager);

        Task task = new Task("First task", "First task description");
        taskManager.addNewTask(task);
        task = new Task("Second task", "Second task description");
        taskManager.addNewTask(task);

        Epic epic = new Epic("First epic", "First epic description");
        taskManager.addNewEpic(epic);

        Subtask subtask = new Subtask("First subtask", "First subtask description", epic);
        epic.addNewSubtask(subtask);
        taskManager.addNewSubtask(subtask);
        subtask = new Subtask("Second subtask", "Second task description", epic);
        epic.addNewSubtask(subtask);
        taskManager.addNewSubtask(subtask);
        taskManager.updateEpic(epic);
    }

    @DisplayName("Операции с менеджером истории")
    @Test
    void historyManagerOperations() {
        List<Task> expected = new ArrayList<>();

        for (Task task : taskManager.getTasks()) {
            expected.add(taskManager.getTask(task.getId()));
        }

        for (Epic epic : taskManager.getEpics()) {
            expected.add(taskManager.getEpic(epic.getId()));
            for (Subtask subtask : epic.getSubtasks()) {
                expected.add(taskManager.getSubtask(subtask.getId()));
            }
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(expected, history, "История формируется некорректно");

        expected = history;
        history = taskManager.getHistory();

        assertEquals(expected, history, "Одинаковые списки полученные разными методами не совпадают");
    }
}
