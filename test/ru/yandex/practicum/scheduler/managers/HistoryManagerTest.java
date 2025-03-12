package ru.yandex.practicum.scheduler.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.InMemoryHistoryManager.HistoricalLinkedList;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HistoryManagerTest {

    private HistoryManager historyManager;
    private TaskManager taskManager;
    private int taskId;

    @BeforeEach
    void createEntities() {
        historyManager = Managers.getDefaultHistory();
        taskManager = new InMemoryTaskManager(historyManager);

        Task task = new Task("First task", "First task description");
        taskId = taskManager.addNewTask(task);
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

        // Переворачиваем ожидаемый результат,
        // так как теперь история просмотров возвращается с конца
        Collections.reverse(expected);

        List<Task> history = historyManager.getHistory();

        assertEquals(expected, history, "История формируется некорректно");

        expected = history;
        history = taskManager.getHistory();

        assertEquals(expected, history, "Одинаковые списки полученные разными методами не совпадают");


    }

    @DisplayName("Операции с коллекцией HistoricalLinkedList")
    @Test
    void HistoricalHistoricalLinkedList() {
        // Создаём коллекцию необходимого типа данных
        HistoricalLinkedList<Task> result = new HistoricalLinkedList<>();
        // Создаём задачу
        Task firstTask = new Task("First task", "Task description");
        // Помещаем задачу в хранилище
        int firstTaskId = taskManager.addNewTask(firstTask);
        // Помещаем задачу в коллекцию
        result.linkLast(firstTask);

        assertEquals(firstTask, result.getHead().element, "У коллекции некорректный первый элемент");
        assertEquals(firstTask, result.getTail().element, "У коллекции некорректный последний элемент");

        // Удаляем элемент из коллекции
        result.remove(firstTaskId);

        assertNull(result.getTail(), "В коллекции присутствует удалённый элемент(последний)");
        assertNull(result.getHead(), "В коллекции присутствует удалённый элемент(первый)");

        // Удаляем несуществующий элемент из коллекции
        result.remove(firstTaskId);
        assertNull(result.getTail(), "В коллекции присутствует элемент(последний) после удаления несуществующего");
        assertNull(result.getHead(), "В коллекции присутствует элемент(первый) после удаления несуществующего");

        // Создаём пустую коллекцию
        ArrayList<Task> expected = new ArrayList<>();
        assertEquals(expected, result.getTasks(), "В коллекции присутствуют неожиданные элементы");
    }
}
