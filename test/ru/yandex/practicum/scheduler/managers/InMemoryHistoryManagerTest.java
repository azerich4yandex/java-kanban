package ru.yandex.practicum.scheduler.managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private TaskManager taskManager;
    private int taskId;

    @BeforeEach
    void createEntities() {
        historyManager = Managers.getDefaultHistory();
        taskManager = new InMemoryTaskManager(historyManager);

        Task task = new Task("First task", "First task description", LocalDateTime.now(), Duration.ofMinutes(30));
        taskId = taskManager.addNewTask(task);
        task = new Task("Second task", "Second task description", task.getEndTime().plusMinutes(1), task.getDuration());
        taskManager.addNewTask(task);

        Epic epic = new Epic("First epic", "First epic description");
        taskManager.addNewEpic(epic);

        Subtask subtask = new Subtask("First subtask", "First subtask description", task.getEndTime().plusMinutes(1),
                task.getDuration(), epic);
        epic.addNewSubtask(subtask);
        taskManager.addNewSubtask(subtask);
        subtask = new Subtask("Second subtask", "Second task description", subtask.getEndTime().plusMinutes(1),
                subtask.getDuration(), epic);
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

        int expectedSize = historyManager.getHistory().size();
        taskManager.deleteTask(taskId);
        expectedSize -= 1;

        assertEquals(expectedSize, historyManager.getHistory().size(),
                "После удаления задачи размер истории некорректный");

    }
}
