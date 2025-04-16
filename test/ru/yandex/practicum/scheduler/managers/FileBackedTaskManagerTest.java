package ru.yandex.practicum.scheduler.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
        List<Task> firstTaskManagerPrioritizedTasks = firstTaskManager.getPrioritizedTasks();
        List<Task> secondTaskManagerPrioritizedTasks = secondTaskManager.getPrioritizedTasks();

        assertEquals(firstTaskManager.getTasks().size(), secondTaskManager.getTasks().size(),
                "Не совпадают загруженный и сохранённый списки задач");
        assertEquals(firstTaskManager.getEpics().size(), secondTaskManager.getEpics().size(),
                "Не совпадают загруженный и сохранённый списки эпиков");
        assertEquals(firstTaskManager.getSubtasks().size(), secondTaskManager.getSubtasks().size(),
                "Не совпадают загруженный и сохранённый списки подзадач");
        assertEquals(firstTaskManagerPrioritizedTasks, secondTaskManagerPrioritizedTasks,
                "Не совпадают сохранённый и загруженный списки приоритетов");

        Optional<Task> taskSecondManager = Optional.ofNullable(secondTaskManager.getTask(taskFirstManager.getId()));

        assertTrue(taskSecondManager.isPresent(), "Не вернулась задача из второго менеджера");

        Task taskSM = taskSecondManager.get();

        assertEquals(taskFirstManager, taskSM, "Задачи: Не совпадают значения поля rowId");
        assertEquals(taskFirstManager.getName(), taskSM.getName(), "Задачи: Не совпадают значения поля name");
        assertEquals(taskFirstManager.getType(), taskSM.getType(), "Задачи: Не совпадают значения поля type");
        assertEquals(taskFirstManager.getDescription(), taskSM.getDescription(),
                "Задачи: Не совпадают значения поля description");
        assertEquals(taskFirstManager.getStatus(), taskSM.getStatus(), "Задачи: Не совпадают значения поля status");
        assertEquals(taskFirstManager.getStartTime().truncatedTo(ChronoUnit.MINUTES),
                taskSM.getStartTime().truncatedTo(ChronoUnit.MINUTES), "Задачи: Не совпадают значения поля startTime");
        assertEquals(taskFirstManager.getDuration(), taskSM.getDuration(),
                "Задачи: Не совпадают значения поля duration");
        assertEquals(taskFirstManager.getEndTime().truncatedTo(ChronoUnit.MINUTES),
                taskSM.getEndTime().truncatedTo(ChronoUnit.MINUTES),
                "Задачи: Не совпадают результаты метода getEndTime()");

        Optional<Epic> epicSecondManager = Optional.ofNullable(secondTaskManager.getEpic(epicFirstManager.getId()));

        assertTrue(epicSecondManager.isPresent(), "Эпики: Из файла не получен эпик");

        Epic epicSM = epicSecondManager.get();

        assertEquals(epicFirstManager, epicSM, "Эпики: Не совпадают значения поля rowId");
        assertEquals(epicFirstManager.getName(), epicSM.getName(), "Эпики: Не совпадают значения поля name");
        assertEquals(epicFirstManager.getType(), epicSM.getType(), "Эпики: Не совпадают значения поля type");
        assertEquals(epicFirstManager.getDescription(), epicSM.getDescription(),
                "Эпики: Не совпадают значения поля description");
        assertEquals(epicFirstManager.getStatus(), epicSM.getStatus(), "Эпики: Не совпадают значения поля status");
        assertEquals(epicFirstManager.getSubtasks(), epicSM.getSubtasks(), "Эпики: Не совпадают списки подзадач");
        assertEquals(epicFirstManager.getStartTime() != null ? epicFirstManager.getStartTime()
                        .truncatedTo(ChronoUnit.MINUTES) : null,
                epicSM.getStartTime() != null ? epicSecondManager.get().getStartTime().truncatedTo(ChronoUnit.MINUTES)
                        : null, "Эпики: Не совпадает значение поля startTime");
        assertEquals(epicFirstManager.getDuration(), epicSM.getDuration(),
                "Эпики: Не совпадают значения поля duration");
        assertEquals(epicFirstManager.getEndTime().truncatedTo(ChronoUnit.MINUTES),
                epicSM.getEndTime().truncatedTo(ChronoUnit.MINUTES),
                "Эпики: Не совпадают значения метода getEndTime()");

        Optional<Subtask> subtaskSecondManager = Optional.ofNullable(
                secondTaskManager.getSubtask(subtaskFirstManager.getId()));

        assertTrue(subtaskSecondManager.isPresent(), "Подзадачи: Из файла не получена подзадача");

        Subtask subtaskSM = subtaskSecondManager.get();

        assertEquals(subtaskFirstManager, subtaskSM, "Подзадачи: Не совпадают значения поля rowId");
        assertEquals(subtaskFirstManager.getName(), subtaskSM.getName(), "Подзадачи: Не совпадают значения поля name");
        assertEquals(subtaskFirstManager.getType(), subtaskSM.getType(), "Подзадачи: Не совпадают значения поля type");
        assertEquals(subtaskFirstManager.getDescription(), subtaskSM.getDescription(),
                "Подзадачи: Не совпадают значения поля description");
        assertEquals(subtaskFirstManager.getStatus(), subtaskSM.getStatus(),
                "Подзадачи: Не совпадают значения поля status");
        assertEquals(subtaskFirstManager.getEpic(), subtaskSM.getEpic(), "Подзадачи: Не совпадают родительские эпики");
        assertEquals(subtaskFirstManager.getStartTime().truncatedTo(ChronoUnit.MINUTES),
                subtaskSM.getStartTime().truncatedTo(ChronoUnit.MINUTES),
                "Подзадачи: Не совпадает значение поля startTime");
        assertEquals(subtaskFirstManager.getDuration(), subtaskSM.getDuration(),
                "Подзадачи: Не совпадает значение поля duration");
        assertEquals(subtaskFirstManager.getEndTime().truncatedTo(ChronoUnit.MINUTES),
                subtaskSM.getEndTime().truncatedTo(ChronoUnit.MINUTES),
                "Подзадачи: Не совпадает значение метода getEndTime()");
    }
}
