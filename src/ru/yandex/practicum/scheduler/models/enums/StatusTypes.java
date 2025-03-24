package ru.yandex.practicum.scheduler.models.enums;

public enum StatusTypes {
    NEW,
    IN_PROGRESS,
    DONE;

    @Override
    public String toString() {
        return switch (this) {
            case NEW -> "NEW";
            case IN_PROGRESS -> "IN_PROGRESS";
            case DONE -> "DONE";
        };
    }

    public StatusTypes fromString(String value) {
        return switch (value.trim().toUpperCase()) {
            case "NEW" -> StatusTypes.NEW;
            case "IN_PROGRESS" -> StatusTypes.IN_PROGRESS;
            case "DONE" -> StatusTypes.DONE;
            default -> null;
        };
    }
}
