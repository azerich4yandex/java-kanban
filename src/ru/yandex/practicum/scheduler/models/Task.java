package ru.yandex.practicum.scheduler.models;

import java.util.HashMap;
import ru.yandex.practicum.scheduler.models.base.AbstractTask;

public class Task extends AbstractTask {
    HashMap<Integer, Epic> epics;

    public Task() {
        super();
        this.epics = new HashMap<>();
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public boolean addEpic(Epic epic) {
        if (!epics.containsKey(epic.getId()) && epic.getId() != null) {
            epics.put(epic.getId(), epic);
            return true;
        }
        return false;
    }

    public boolean updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            return true;
        }
        return false;
    }

    public boolean removeEpic(Epic epic) {
        if (epics != null) {
            epics.remove(epic.getId());
            return true;
        }
        return false;
    }

    public boolean clearEpics() {
        if (epics != null) {
            epics.clear();
            return true;
        }
        return false;
    }

    public Epic getEpic(int epicId) {
        return this.epics.get(epicId);
    }
}
