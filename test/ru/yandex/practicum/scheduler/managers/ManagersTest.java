package ru.yandex.practicum.scheduler.managers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @DisplayName("Получение менеджера задач по умолчанию")
    @Test
    void getDefault() {
        TaskManager taskManager = Managers.getDefault();
        TaskManager expected = new InMemoryTaskManager(Managers.getDefaultHistory());

        assertEquals(expected.getClass(), taskManager.getClass(), "Менеджер задач по умолчанию и конкретный менеджер задач не совпадают");
    }

    @DisplayName("Получение менеджера истории по умолчанию")
    @Test
    void getDefaultHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        HistoryManager expected = new InMemoryHistoryManager();

        assertEquals(expected.getClass(), historyManager.getClass(), "Менеджер истории по умолчанию и конкретный менеджер истории не совпадают");
    }
}