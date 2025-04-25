package ru.yandex.practicum.scheduler.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class Epic extends Task {

    private final List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        this.type = TaskTypes.EPIC;
    }

    public Epic(Integer id, StatusTypes status, String name, String description) {
        super(id, status, name, description);
        this.type = TaskTypes.EPIC;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(Integer subtaskId) {
        if (subtaskId != null) {
            int index = subtaskIds.indexOf(subtaskId);
            if (index == -1) {
                subtaskIds.add(subtaskId);
            }
        }
    }

    public void updateSubtask(Integer subtaskId) {
        if (subtaskId != null) {
            int index = subtaskIds.indexOf(subtaskId);

            if (index != -1) {
                subtaskIds.set(index, subtaskId);
            }
        }
    }

    public void deleteSubtask(Subtask subtask) {
        int index = subtaskIds.indexOf(subtask.getId());
        if (index != -1) {
            subtaskIds.remove(index);
        }
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
