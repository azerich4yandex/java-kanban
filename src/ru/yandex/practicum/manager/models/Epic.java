package ru.yandex.practicum.manager.models;

import java.util.ArrayList;
import java.util.List;
import ru.yandex.practicum.manager.models.enums.StatusTypes;

public class Epic extends Task {

    private final List<Subtask> subtasks = new ArrayList<>();

    public Epic() {
        super();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void addNewSubtask(Subtask subtask) {
        int index = subtasks.indexOf(subtask);
        if (index == -1) {
            subtasks.add(subtask);
        }
    }

    public void deleteSubtask(Subtask subtask) {
        int index = subtasks.indexOf(subtask);
        if (index != -1) {
            subtasks.remove(index);
        }
    }

    public void calculateStatus() {
        StatusTypes resultStatus = StatusTypes.IN_PROGRESS;
        int newQuantity = 0;
        int doneQuantity = 0;

        for (Subtask subtask : subtasks) {
            switch (subtask.getStatus()) {
                case NEW -> newQuantity++;
                case DONE -> doneQuantity++;
                default -> {
                }
            }
        }

        if (subtasks.isEmpty()) {
            resultStatus = StatusTypes.NEW;
        } else if (newQuantity == subtasks.size()) {
            resultStatus = StatusTypes.NEW;
        } else if (doneQuantity == subtasks.size()) {
            resultStatus = StatusTypes.DONE;
        }

        setStatus(resultStatus);
    }
}
