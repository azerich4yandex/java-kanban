package ru.yandex.practicum.scheduler.managers;

import java.util.List;
import ru.yandex.practicum.scheduler.models.Task;

public interface HistoryManager {
    void addToHistory(Task task);
    List<Task> getHistory();
}
