package ru.yandex.practicum.scheduler.exceptions;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException() {
        super();
    }

    public ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
