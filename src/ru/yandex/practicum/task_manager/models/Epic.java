package ru.yandex.practicum.task_manager.models;

import java.util.HashMap;
import ru.yandex.practicum.task_manager.models.enums.StatusTypes;

public class Epic extends Task {

    HashMap<Integer, SubTask> subTasks;

    public Epic() {
        super();
        this.subTasks = new HashMap<>();
    }

    @Override
    public StatusTypes getStatus() {
        int newStatus = 0;
        int doneStatus = 0;

        for (SubTask subTask : subTasks.values()) {
            if (subTask.getStatus() == StatusTypes.NEW) {
                newStatus++;
            } else if (subTask.getStatus() == StatusTypes.DONE) {
                doneStatus++;
            }
        }

        if (newStatus == subTasks.size()) {
            super.setStatus(StatusTypes.NEW);
        } else if (doneStatus == subTasks.size()) {
            super.setStatus(StatusTypes.DONE);
        } else {
            super.setStatus(StatusTypes.IN_PROGRESS);
        }

        return super.getStatus();
    }

    @Override
    public void setStatus(StatusTypes status) {
        super.setStatus(this.getStatus());
    }

    public void addSubTask(SubTask subTask) {
        if (subTask.uniqueId != null && subTasks.containsKey(subTask.uniqueId)) {
            subTasks.put(subTask.uniqueId, subTask);
        }
    }

    public void updateSubTask(SubTask subTask) {
        if (subTask.uniqueId != null && subTasks.containsKey(subTask.uniqueId)) {
            subTasks.put(subTask.uniqueId, subTask);
        }
    }
}
