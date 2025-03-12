package ru.yandex.practicum.scheduler.managers.interfaces;

import java.util.List;
import ru.yandex.practicum.scheduler.models.Task;

public interface HistoryManager {
    void addToHistory(Task task);
    void remove(int id);
    List<Task> getHistory();
}
