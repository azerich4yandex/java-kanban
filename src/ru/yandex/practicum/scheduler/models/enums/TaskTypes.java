package ru.yandex.practicum.scheduler.models.enums;

public enum TaskTypes {
    TASK,
    EPIC,
    SUBTASK;


    @Override
    public String toString() {
        return switch (this) {
            case TASK -> "TASK";
            case EPIC -> "EPIC";
            case SUBTASK -> "SUBTASK";
        };
    }
    
    public static TaskTypes fromString(String value) {
        return switch (value.trim().toUpperCase()) {
            case "TASK" -> TaskTypes.TASK;
            case "EPIC" -> TaskTypes.EPIC;
            case "SUBTASK" -> TaskTypes.SUBTASK;
            default -> null;
        };
    }
}
