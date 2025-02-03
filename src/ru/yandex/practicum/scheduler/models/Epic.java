package ru.yandex.practicum.scheduler.models;

import java.util.HashMap;
import ru.yandex.practicum.scheduler.models.base.AbstractTask;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;

public class Epic extends AbstractTask {

    Task task;
    HashMap<Integer, SubTask> subTasks;

    public Epic(Task task) {
        super();
        this.task = task;
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

    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public void addSubTask(SubTask subTask) {
        if (subTask.getId() != null && !subTasks.containsKey(subTask.getId())) {
            subTasks.put(subTask.getId(), subTask);
        }
    }

    public void updateSubTask(SubTask subTask) {
        if (subTask.getId() != null && subTasks.containsKey(subTask.getId())) {
            subTasks.put(subTask.getId(), subTask);
        }
    }

    public void removeSubtask(int id) {
        subTasks.remove(id);
    }

    public void clearSubTasks() {
        this.subTasks.clear();
    }

    public SubTask getSubTask(int subTaskId) {
        return this.getSubTasks().get(subTaskId);
    }
}
