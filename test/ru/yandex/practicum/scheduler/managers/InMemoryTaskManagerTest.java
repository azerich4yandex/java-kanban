package ru.yandex.practicum.scheduler.managers;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.exceptions.NotAcceptableException;
import ru.yandex.practicum.scheduler.models.Task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager>{

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @DisplayName("Пересечение интервалов: добавление с пересечением")
    @Test
    void testTimeIntersection() {
        Task task1 = new Task("First task", "Task description", subtask.getEndTime().plusMinutes(1),
                Duration.ofMinutes(30));
        Task task2 = new Task("Second task", "Task description", task1.getStartTime(), Duration.ofMinutes(3));
        taskManager.createTask(task1);

        assertThrows(NotAcceptableException.class, () -> taskManager.createTask(task2),
                "Не выброшено ожидаемое исключение");
    }

    @DisplayName("Пересечение интервалов: добавление без пересечения")
    @Test
    void testNoTimeIntersection() {
        Task task1 = new Task("First task", "Task description", subtask.getEndTime().plusMinutes(1),
                Duration.ofMinutes(30));
        Task task2 = new Task("Second task", "Task description", task1.getEndTime().plusMinutes(1), Duration.ofMinutes(30));

        assertDoesNotThrow(() -> {
            taskManager.createTask(task1);
            taskManager.createTask(task2);
        }, "Выброшено исключение");
    }
}
