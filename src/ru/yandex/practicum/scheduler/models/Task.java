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

    public void addEpic(Epic epic) {
        if (!epics.containsKey(epic.getId()) && epic.getId() != null) {
            epics.put(epic.getId(), epic);
        }
    }

    public void removeEpic(Epic epic) {
        epics.remove(epic.getId());
    }

    public void clearEpics() {
        epics.clear();
    }

    public Epic getEpic(int epicId) {
        return this.epics.get(epicId);
    }
}
