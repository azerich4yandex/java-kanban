package ru.yandex.practicum.scheduler.managers;

import java.util.ArrayList;
import java.util.List;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.models.Task;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new ArrayList<>();

    //<editor-fold desc="History methods">
    @Override
    public List<Task> getHistory() {
        return history;
    }

    @Override
    public void addToHistory(Task task) {
        if (task != null) {
            if (history.size() >= 10) {
                history.remove(1);
            }
            history.add(task);
        }
    }
    //</editor-fold>
}
