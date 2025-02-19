package ru.yandex.practicum.scheduler.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.Managers;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private TaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
    }

    @DisplayName("Задачи с одинаковым Id должны совпадать (Через менеджер задач)")
    @Test
    void shouldBeEqualsWithSameIdThroughTaskManager() {
        Task task = new Task("Task name", "Task description");
        int taskId = taskManager.addNewTask(task);

        Task task2 = taskManager.getTask(taskId);
        task2.setName("Modified name");
        task2.setDescription("Modified description");

        assertEquals(task2, taskManager.getTask(taskId), "Задачи с разными Id не совпадают");
    }

    @DisplayName("Задачи с одинаковым Id должны совпадать (Без менеджера задач)")
    @Test
    void shouldBeEqualsWithSameId() {
        Task task1 = new Task("First task", "First task description");
        task1.setId(1);
        Task task2 = new Task("Second task", "Second task description");
        task2.setId(2);
        assertNotEquals(task1, task2, "Разные задачи совпадают");

        task2.setId(task1.getId());
        assertEquals(task1, task2, "Задачи с одинаковым Id не совпадают");
    }
}