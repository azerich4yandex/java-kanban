package ru.yandex.practicum.scheduler.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.exceptions.ManagerSaveException;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest {

    private TaskManager taskManager;
    private Path tempFile;

    @BeforeEach
    void init() throws ManagerSaveException {
        HistoryManager historyManager = Managers.getDefaultHistory();

        try {
            tempFile = File.createTempFile("database", "csv").toPath();
            taskManager = new FileBackedTaskManager(historyManager, tempFile.toFile());
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    @AfterEach
    void onExit() {
        tempFile.toFile().deleteOnExit();
    }

    @DisplayName("Операции с Task: Добавление")
    @Test
    void addNew() {
        List<Task> expected = new ArrayList<>();
        Task task = new Task("Test task", "Test task description");
        int taskId1 = taskManager.addNewTask(task);
        expected.add(task);

        assertEquals(taskId1, taskManager.getTask(taskId1).getId(), "Id созданной и сохранённой задач не совпадают");
        assertEquals(task, taskManager.getTask(taskId1), "Созданная и сохранённая задачи не совпадают ");
        assertEquals(StatusTypes.NEW, taskManager.getTask(taskId1).getStatus(), "Статус новой задачи не NEW");

        task = new Task("2nd test task", "2nd test description");
        int taskId2 = taskManager.addNewTask(task);
        expected.add(task);

        assertNotEquals(taskId1, taskId2, "Id разных задач совпадают");
        assertEquals(expected, taskManager.getTasks(), "Списки задач не совпадают");

        task = taskManager.getSubtask(0);
        assertNull(task, "Найдена не добавленная задача");
    }

    @DisplayName("Операции с Task: Изменение")
    @Test
    void updateTask() {
        Task task = new Task("Test task", "Test task description");
        int taskId = taskManager.addNewTask(task);

        String expectedName = "Modified name";
        String expectedDescription = "Modified description";
        StatusTypes expectedStatus = StatusTypes.IN_PROGRESS;

        task = taskManager.getTask(taskId);
        task.setName(expectedName);
        task.setDescription(expectedDescription);
        task.setStatus(expectedStatus);
        taskManager.updateTask(task);

        task = taskManager.getTask(taskId);

        assertEquals(expectedName, task.getName(), "Изменённое название не сохранилось");
        assertEquals(expectedDescription, task.getDescription(), "Изменённое описание не сохранилось");
        assertEquals(expectedStatus, task.getStatus(), "Изменённый статус не сохранился");
    }

    @DisplayName("Операции с Task: Удаление")
    @Test
    void deleteTask() {
        List<Task> expected = new ArrayList<>();
        Task task = new Task("Test task", "Test task description");
        taskManager.addNewTask(task);
        expected.add(task);

        task = new Task("2nd test task", "2nd test description");
        int taskId2 = taskManager.addNewTask(task);
        expected.add(task);

        taskManager.deleteTask(0);
        assertEquals(expected, taskManager.getTasks(), "Списки после удаления несуществующей записи не совпадают");

        expected.remove(task);
        taskManager.deleteTask(taskId2);
        assertEquals(expected, taskManager.getTasks(), "Списки после удаления задачи не совпадают");

        taskManager.deleteTasks();
        expected.clear();
        assertEquals(expected, taskManager.getTasks(), "Списки после очистки не совпадают");
    }

    @DisplayName("Операции с Subtask: Добавление")
    @Test
    void addNewSubtask() {
        List<Subtask> expected = new ArrayList<>();
        Epic epic = new Epic("Epic name", "Epic description");
        Subtask subtask = new Subtask("Test subtask", "Test subtask description", epic);
        taskManager.addNewEpic(epic);

        int subtaskId1 = taskManager.addNewSubtask(subtask);
        epic.addNewSubtask(subtask);
        taskManager.updateEpic(epic);
        expected.add(subtask);

        assertEquals(subtask, taskManager.getSubtask(subtaskId1), "Созданная и сохранённая подзадачи не совпадают");
        assertEquals(StatusTypes.NEW, taskManager.getSubtask(subtaskId1).getStatus(), "Статус новой подзадачи не NEW");

        subtask = new Subtask("2nd test subtask", "2nd test description", epic);
        epic.addNewSubtask(subtask);
        int subtaskId2 = taskManager.addNewSubtask(subtask);
        taskManager.updateEpic(epic);
        expected.add(subtask);

        assertNotEquals(subtaskId1, subtaskId2, "Id разных задач совпадают");
        assertEquals(expected, taskManager.getSubtasks(), "Неожиданное количество подзадач в TaskManager");
        assertEquals(expected, taskManager.getEpic(epic.getId()).getSubtasks(), "Неожиданное количество задач в эпике");

        subtask = taskManager.getSubtask(0);
        assertNull(subtask, "Найдена не добавленная задача");
    }

    @DisplayName("Операции с Subtask: Изменение")
    @Test
    void updateSubtask() {
        Epic epic = new Epic("Epic name", "Epic description");
        Subtask subtask = new Subtask("Test subtask", "Test subtask description", epic);
        taskManager.addNewEpic(epic);

        int subtaskId = taskManager.addNewSubtask(subtask);
        epic.addNewSubtask(subtask);
        taskManager.updateEpic(epic);

        String expectedName = "Modified name";
        String expectedDescription = "Modified description";
        StatusTypes expectedStatus = StatusTypes.IN_PROGRESS;
        subtask = taskManager.getSubtask(subtaskId);
        subtask.setName(expectedName);
        subtask.setDescription(expectedDescription);
        subtask.setStatus(expectedStatus);
        taskManager.updateSubtask(subtask);

        assertEquals(expectedName, taskManager.getSubtask(subtask.getId()).getName(),
                "Изменённое название не сохранилось");
        assertEquals(expectedDescription, taskManager.getSubtask(subtask.getId()).getDescription(),
                "Изменённое описание не сохранилось");
        assertEquals(expectedStatus, taskManager.getSubtask(subtask.getId()).getStatus(),
                "Изменённый статус не сохранился");
    }

    @DisplayName("Операции с Subtask: Удаление")
    @Test
    void deleteSubtask() {
        List<Subtask> expected = new ArrayList<>();
        Epic epic = new Epic("Epic name", "Epic description");
        Subtask subtask = new Subtask("Test subtask", "Test subtask description", epic);
        taskManager.addNewEpic(epic);

        taskManager.addNewSubtask(subtask);
        epic.addNewSubtask(subtask);
        taskManager.updateEpic(epic);
        expected.add(subtask);

        subtask = new Subtask("2nd test subtask", "2nd test description", epic);

        int subtaskId2 = taskManager.addNewSubtask(subtask);
        epic.addNewSubtask(subtask);

        taskManager.updateEpic(epic);
        expected.add(subtask);

        taskManager.deleteSubtask(0);
        assertEquals(expected, taskManager.getSubtasks(),
                "Списки подзадач не совпадают после удаления несуществующей подзадачи");

        taskManager.deleteSubtask(subtaskId2);
        expected.remove(subtask);
        subtask = taskManager.getSubtask(subtaskId2);
        assertNull(subtask, "Подзадача не удалена");
        assertEquals(expected, taskManager.getSubtasks(), "Списки после удаления одной подзадачи не совпадают");
        assertEquals(expected, taskManager.getEpic(epic.getId()).getSubtasks(),
                "Списки подзадач после удаления подзадачи не совпадают");

        taskManager.deleteSubtasks();
        expected.clear();

        assertEquals(expected, taskManager.getSubtasks(), "Неожиданное количество подзадач");
    }

    @DisplayName("Операции с Epic: Добавление")
    @Test
    void addNewEpic() {
        List<Epic> expected = new ArrayList<>();
        List<Subtask> subtasks = new ArrayList<>();

        Epic epic1 = new Epic("Epic name", "Epic description");
        int epicId1 = taskManager.addNewEpic(epic1);
        Subtask subtask = new Subtask("Test subtask", "Test subtask description", epic1);
        taskManager.addNewSubtask(subtask);
        epic1.addNewSubtask(subtask);
        taskManager.updateEpic(epic1);
        subtasks.add(subtask);
        expected.add(epic1);
        assertEquals(epic1, taskManager.getEpic(epicId1), "Созданный и сохранённый эпики не совпадают");

        Epic epic2 = new Epic("Second epic name", "Second epic description");
        int epicId2 = taskManager.addNewEpic(epic2);
        expected.add(epic2);
        assertNotEquals(epicId1, epicId2, "Id разных эпиков совпадают");
        assertNotEquals(taskManager.getEpic(epicId1), taskManager.getEpic(epicId2), "Разные эпики совпадают");
        assertEquals(expected, taskManager.getEpics(), "Списки эпиков не совпадают");

        subtask = new Subtask("2nd test subtask", "2nd test description", epic1);
        taskManager.addNewSubtask(subtask);
        epic1.addNewSubtask(subtask);
        taskManager.updateEpic(epic1);
        subtasks.add(subtask);

        assertEquals(subtasks, taskManager.getEpic(epicId1).getSubtasks(), "Неожиданное количество подзадач");
    }

    @DisplayName("Операции с Epic: Изменение")
    @Test
    void updateEpic() {
        Epic epic = new Epic("Epic name", "Epic description");
        Subtask subtask = new Subtask("Test subtask", "Test subtask description", epic);
        int epicId = taskManager.addNewEpic(epic);
        int subtaskId = taskManager.addNewSubtask(subtask);
        epic.addNewSubtask(subtask);
        taskManager.updateEpic(epic);

        String expectedName = "Modified name";
        String expectedDescription = "Modified description";
        StatusTypes expectedStatus = StatusTypes.IN_PROGRESS;
        subtask = taskManager.getSubtask(subtaskId);
        subtask.setStatus(expectedStatus);
        epic.setName(expectedName);
        epic.setDescription(expectedDescription);
        taskManager.updateSubtask(subtask);
        taskManager.updateEpic(epic);

        epic = taskManager.getEpic(epicId);
        assertEquals(expectedName, epic.getName(), "Изменённое название не сохранилось");
        assertEquals(expectedDescription, epic.getDescription(), "Изменённое описание не сохранилось");
        assertEquals(expectedStatus, epic.getStatus(), "Статус эпика не пересчитался после изменения подзадачи");
    }

    @DisplayName("Операции с Epic: Удаление")
    @Test
    void deleteEpic() {
        List<Epic> expected = new ArrayList<>();

        Epic epic = new Epic("Epic name", "Epic description");
        int epicId1 = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test subtask", "Test subtask description", epic);
        int subtaskId = taskManager.addNewSubtask(subtask);
        subtask.setId(subtaskId);
        epic.addNewSubtask(subtask);
        taskManager.updateEpic(epic);
        expected.add(epic);

        epic = new Epic("Second epic name", "Second epic description");
        taskManager.addNewEpic(epic);
        expected.add(epic);

        taskManager.deleteEpic(0);
        assertEquals(expected, taskManager.getEpics(),
                "Списки эпиков не совпадают после удаления несуществующего эпика");

        epic = taskManager.getEpic(epicId1);
        taskManager.deleteEpic(epicId1);
        expected.remove(epic);
        assertEquals(expected, taskManager.getEpics(), "Списки эпиков не совпадают после удаления эпика");

        epic = taskManager.getEpic(epicId1);
        assertNull(epic, "Эпик не удалён");

        taskManager.deleteEpics();
        expected.clear();
        assertEquals(expected, taskManager.getEpics(), "Списки эпиков не совпадают после очистки");
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

        Task taskFirstManager = new Task("First task name", "First task description");
        firstTaskManager.addNewTask(taskFirstManager);
        Epic epicFirstManager = new Epic("First epic name", "First epic description");
        firstTaskManager.addNewEpic(epicFirstManager);
        Subtask subtaskFirstManager = new Subtask("First subtask name", "First subtask description", epicFirstManager);
        firstTaskManager.addNewSubtask(subtaskFirstManager);
        FileBackedTaskManager secondTaskManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertEquals(firstTaskManager.getTasks().size(), secondTaskManager.getTasks().size(),
                "Не совпадают загруженный и сохранённый списки задач");
        assertEquals(firstTaskManager.getEpics().size(), secondTaskManager.getEpics().size(),
                "Не совпадают загруженный и сохранённый списки эпиков");
        assertEquals(firstTaskManager.getSubtasks().size(), secondTaskManager.getSubtasks().size(),
                "Не совпадают загруженный и сохранённый списки подзадач");

        Task taskSecondManager = secondTaskManager.getTask(taskFirstManager.getId());
        boolean isMatch = taskFirstManager.equals(taskSecondManager) && taskFirstManager.getName()
                .equals(taskSecondManager.getName()) && taskFirstManager.getDescription()
                .equals(taskSecondManager.getDescription()) && taskFirstManager.getStatus()
                .equals(taskSecondManager.getStatus()) && taskFirstManager.getType()
                .equals(taskSecondManager.getType());
        assertTrue(isMatch, "Не совпадают загруженная и сохранённая задачи");

        Epic epicSecondManager = secondTaskManager.getEpic(epicFirstManager.getId());
        isMatch = epicFirstManager.equals(epicSecondManager) && epicFirstManager.getName()
                .equals(epicSecondManager.getName()) && epicFirstManager.getDescription()
                .equals(epicSecondManager.getDescription()) && epicFirstManager.getStatus()
                .equals(epicSecondManager.getStatus()) && epicFirstManager.getSubtasks()
                .equals(epicSecondManager.getSubtasks()) && epicFirstManager.getType()
                .equals(epicSecondManager.getType());
        assertTrue(isMatch, "Не совпадают загруженный и сохранённый эпики");

        Subtask subtaskSecondManager = secondTaskManager.getSubtask(subtaskFirstManager.getId());
        isMatch = subtaskFirstManager.equals(subtaskSecondManager) && subtaskFirstManager.getName()
                .equals(subtaskSecondManager.getName()) && subtaskFirstManager.getDescription()
                .equals(subtaskSecondManager.getDescription()) && subtaskFirstManager.getStatus()
                .equals(subtaskSecondManager.getStatus()) && subtaskFirstManager.getType()
                .equals(subtaskSecondManager.getType());
        assertTrue(isMatch, "Не совпадают загруженная и сохранённая подзадачи");
    }
}
