package ru.yandex.practicum.task_manager.models;

import java.util.HashMap;
import ru.yandex.practicum.task_manager.models.base.AbstractTask;

public class Task extends AbstractTask {
    HashMap<Integer, Epic> epics;

    public Task() {
        super();
        this.epics = new HashMap<>();
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public void setEpics(HashMap<Integer, Epic> epics) {
        this.epics = epics;
    }

    public void addEpic(Epic epic) {
        if (!epics.containsKey(epic.getId()) && epic.getId() != null) {
            epics.put(epic.getId(), epic);
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    public void removeEpic(Epic epic) {
        epics.remove(epic.getId());
    }

    public void clearEpics() {
        epics.clear();
    }
}
