package ru.yandex.practicum.scheduler.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class Epic extends Task {

    private final List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        this.type = TaskTypes.EPIC;
    }

    public Epic(Integer id, StatusTypes status, String name, String description) {
        super(id, status, name, description);
        this.type = TaskTypes.EPIC;
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

    public void updateSubtask(Subtask subtask) {
        int index = subtasks.indexOf(subtask);

        if (index != -1) {
            subtasks.set(index, subtask);
        }
    }

    public void deleteSubtask(Subtask subtask) {
        int index = subtasks.indexOf(subtask);
        if (index != -1) {
            subtasks.remove(index);
        }
    }

    public void clearSubtasks() {
        subtasks.clear();
    }

    public void calculateFields() {
        // Пересчитаем статус эпика
        calculateStatusField();
        // Пересчитаем дату начала и окончания эпика и его длительность
        calculateTimeFields();
    }

    private void calculateStatusField() {
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

        status = resultStatus;
    }

    private void calculateTimeFields() {
        // Находим и присваиваем эпику самую раннюю дату начала подзадачи
        startTime = subtasks.stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .min(Comparator.comparing(Task::getStartTime))
                .map(Task::getStartTime)
                .orElse(null);

        // Находим и присваиваем эпику самую позднюю дату окончания подзадачи
        endTime = subtasks.stream()
                .filter(subtask -> subtask.getEndTime() != null)
                .max(Comparator.comparing(Task::getEndTime))
                .map(Task::getEndTime)
                .orElse(null);

        // Подсчитываем общую длительность подзадач
        duration = subtasks.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

    }


    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public Duration getDuration() {
        return super.getDuration();
    }
}
