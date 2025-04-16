package ru.yandex.practicum.scheduler.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.exceptions.ManagerSaveException;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private TaskManager taskManager;
    private Path tempFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        try {
            tempFile = File.createTempFile("database", "csv").toPath();
            tempFile.toFile().deleteOnExit();
            taskManager = new FileBackedTaskManager(historyManager, tempFile.toFile());
            return (FileBackedTaskManager) taskManager;
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    @DisplayName("Операции с файлами: Сохранение и загрузка")
    @Test
    void saveAndLoadOperations() throws ManagerSaveException {
        Path tempFile;

        try {
            tempFile = File.createTempFile("database", ".csv").toPath();
            tempFile.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new ManagerSaveException(e);
        }

        HistoryManager historyManager = Managers.getDefaultHistory();
        FileBackedTaskManager firstTaskManager = new FileBackedTaskManager(historyManager, tempFile.toFile());

        Task taskFirstManager = new Task("First task name", "First task description", LocalDateTime.now(),
                Duration.ofMinutes(1));
        firstTaskManager.addNewTask(taskFirstManager);
        Epic epicFirstManager = new Epic("First epic name", "First epic description");
        firstTaskManager.addNewEpic(epicFirstManager);
        Subtask subtaskFirstManager = new Subtask("First subtask name", "First subtask description",
                taskFirstManager.getEndTime().plusMinutes(1), taskFirstManager.getDuration(), epicFirstManager);
        firstTaskManager.addNewSubtask(subtaskFirstManager);
        FileBackedTaskManager secondTaskManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertEquals(firstTaskManager.getTasks().size(), secondTaskManager.getTasks().size(),
                "Не совпадают загруженный и сохранённый списки задач");
        assertEquals(firstTaskManager.getEpics().size(), secondTaskManager.getEpics().size(),
                "Не совпадают загруженный и сохранённый списки эпиков");
        assertEquals(firstTaskManager.getSubtasks().size(), secondTaskManager.getSubtasks().size(),
                "Не совпадают загруженный и сохранённый списки подзадач");

        Optional<Task> taskSecondManager = Optional.ofNullable(secondTaskManager.getTask(taskFirstManager.getId()));
        assertTrue(taskSecondManager.isPresent(), "Не вернулась задача из второго менеджера");
        assertEquals(taskFirstManager, taskSecondManager.get(), "Задачи: Не совпадают значения поля rowId");
        assertEquals(taskFirstManager.getName(), taskSecondManager.get().getName(),
                "Задачи: Не совпадают значения поля name");
        assertEquals(taskFirstManager.getType(), taskSecondManager.get().getType(),
                "Задачи: Не совпадают значения поля type");
        assertEquals(taskFirstManager.getDescription(), taskSecondManager.get().getDescription(),
                "Задачи: Не совпадают значения поля description");
        assertEquals(taskFirstManager.getStatus(), taskSecondManager.get().getStatus(),
                "Задачи: Не совпадают значения поля status");
        assertEquals(taskFirstManager.getStartTime().truncatedTo(ChronoUnit.MINUTES),
                taskSecondManager.get().getStartTime().truncatedTo(ChronoUnit.MINUTES),
                "Задачи: Не совпадают значения поля startTime");
        assertEquals(taskFirstManager.getDuration(), taskSecondManager.get().getDuration(),
                "Задачи: Не совпадают значения поля duration");
        assertEquals(taskFirstManager.getEndTime().truncatedTo(ChronoUnit.MINUTES),
                taskSecondManager.get().getEndTime().truncatedTo(ChronoUnit.MINUTES),
                "Задачи: Не совпадают результаты метода getEndTime()");

        Optional<Epic> epicSecondManager = Optional.ofNullable(secondTaskManager.getEpic(epicFirstManager.getId()));
        assertTrue(epicSecondManager.isPresent(), "Эпики: Из файла не получен эпик");
        assertEquals(epicFirstManager, epicSecondManager.get(), "Эпики: Не совпадают значения поля rowId");
        assertEquals(epicFirstManager.getName(), epicSecondManager.get().getName(),
                "Эпики: Не совпадают значения поля name");
        assertEquals(epicFirstManager.getType(), epicSecondManager.get().getType(),
                "Эпики: Не совпадают значения поля type");
        assertEquals(epicFirstManager.getDescription(), epicSecondManager.get().getDescription(),
                "Эпики: Не совпадают значения поля description");
        assertEquals(epicFirstManager.getStatus(), epicSecondManager.get().getStatus(),
                "Эпики: Не совпадают значения поля status");
        assertEquals(epicFirstManager.getSubtasks(), epicSecondManager.get().getSubtasks(),
                "Эпики: Не совпадают списки подзадач");
        assertEquals(epicFirstManager.getStartTime() != null ? epicFirstManager.getStartTime()
                        .truncatedTo(ChronoUnit.MINUTES) : null,
                epicSecondManager.get().getStartTime() != null ? epicSecondManager.get().getStartTime()
                        .truncatedTo(ChronoUnit.MINUTES) : null,
                "Эпики: Не совпадает значение поля startTime");
        assertEquals(epicFirstManager.getDuration(), epicSecondManager.get().getDuration(),
                "Эпики: Не совпадают значения поля duration");
        assertEquals(epicFirstManager.getEndTime().truncatedTo(ChronoUnit.MINUTES),
                epicSecondManager.get().getEndTime().truncatedTo(ChronoUnit.MINUTES),
                "Эпики: Не совпадают значения метода getEndTime()");

        Optional<Subtask> subtaskSecondManager = Optional.ofNullable(
                secondTaskManager.getSubtask(subtaskFirstManager.getId()));
        assertTrue(subtaskSecondManager.isPresent(), "Подзадачи: Из файла не получена подзадача");
        assertEquals(subtaskFirstManager, subtaskSecondManager.get(), "Подзадачи: Не совпадают значения поля rowId");
        assertEquals(subtaskFirstManager.getName(), subtaskSecondManager.get().getName(),
                "Подзадачи: Не совпадают значения поля name");
        assertEquals(subtaskFirstManager.getType(), subtaskSecondManager.get().getType(),
                "Подзадачи: Не совпадают значения поля type");
        assertEquals(subtaskFirstManager.getDescription(), subtaskSecondManager.get().getDescription(),
                "Подзадачи: Не совпадают значения поля description");
        assertEquals(subtaskFirstManager.getStatus(), subtaskSecondManager.get().getStatus(),
                "Подзадачи: Не совпадают значения поля status");
        assertEquals(subtaskFirstManager.getEpic(), subtaskSecondManager.get().getEpic(),
                "Подзадачи: Не совпадают родительские эпики");
        assertEquals(subtaskFirstManager.getStartTime().truncatedTo(ChronoUnit.MINUTES),
                subtaskSecondManager.get().getStartTime().truncatedTo(ChronoUnit.MINUTES),
                "Подзадачи: Не совпадает значение поля startTime");
        assertEquals(subtaskFirstManager.getDuration(), subtaskSecondManager.get().getDuration(),
                "Подзадачи: Не совпадает значение поля duration");
        assertEquals(subtaskFirstManager.getEndTime().truncatedTo(ChronoUnit.MINUTES),
                subtaskSecondManager.get().getEndTime().truncatedTo(ChronoUnit.MINUTES),
                "Подзадачи: Не совпадает значение метода getEndTime()");
    }
}
