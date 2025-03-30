package ru.yandex.practicum.scheduler.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Task;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @DisplayName("Получение менеджера задач по умолчанию")
    @Test
    void getDefault() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "Менеджер задач вернулся пустым");
    }

    @DisplayName("Получение менеджера истории по умолчанию")
    @Test
    void getDefaultHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "Менеджер истории вернулся пустым");
    }
}