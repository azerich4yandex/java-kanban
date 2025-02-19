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

    @DisplayName("Задачи с одинаковым Id должны совпадать")
    @Test
    void shouldBeEqualsWithSameId(){
        Task task = new Task("Task name", "Task description");
        int taskId = taskManager.addNewTask(task);

        Task task2 = taskManager.getTask(taskId);
        task2.setName("Modified name");
        task2.setDescription("Modified description");


        assertEquals(task2, taskManager.getTask(taskId), "Задачи с разными Id не совпадают");
    }
}