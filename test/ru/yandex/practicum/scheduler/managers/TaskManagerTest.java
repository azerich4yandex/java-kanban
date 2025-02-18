package ru.yandex.practicum.scheduler.managers;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TaskManagerTest {

    static TaskManager taskManager;

    @BeforeAll
    static void init() {
        taskManager = Managers.getDefault();
    }

    @DisplayName("Операции с Task")
    @Test
    void taskCrud() {
        Task task = new Task("Test task", "Test task description");
        int taskId1 = taskManager.addNewTask(task);

        assertEquals(taskId1, taskManager.getTask(taskId1).getId(), "Id созданной и сохранённой задач не совпадают");
        assertEquals(task, taskManager.getTask(taskId1), "Созданная и сохранённая задачи не совпадают ");
        assertEquals(StatusTypes.NEW, taskManager.getTask(taskId1).getStatus(), "Статус новой задачи не NEW");

        task = new Task("2nd test task", "2nd test description");
        int taskId2 = taskManager.addNewTask(task);

        assertNotEquals(taskId1, taskId2, "Id разных задач совпадают");
        assertEquals(2, taskManager.getTasks().size(), "Неожиданное количество задач");

        String expectedName = "Modified name";
        String expectedDescription = "Modified description";
        StatusTypes expectedStatus = StatusTypes.IN_PROGRESS;
        task = taskManager.getTask(taskId1);
        task.setName(expectedName);
        task.setDescription(expectedDescription);
        task.setStatus(expectedStatus);
        taskManager.updateTask(task);

        assertEquals(expectedName, taskManager.getTask(task.getId()).getName(),
                "Изменённое наименование не сохранилось");
        assertEquals(expectedDescription, taskManager.getTask(task.getId()).getDescription(),
                "Изменённое описание не сохранилось");
        assertEquals(expectedStatus, taskManager.getTask(task.getId()).getStatus(), "Изменённый статус не сохранился");

        taskManager.deleteTask(taskId2);

        task = taskManager.getTask(taskId2);

        assertNull(task, "Задача не удалена");

        taskManager.deleteTasks();

        assertEquals(0, taskManager.getTasks().size(), "Неожиданное количество задач");
    }

    @DisplayName("Операции с Subtask")
    @Test
    void subtaskCrud() {
        Epic epic = new Epic("Epic name", "Epic description");
        Subtask subtask = new Subtask("Test subtask", "Test subtask description", epic);
        epic.setId(taskManager.addNewEpic(epic));
        int subtaskId1 = taskManager.addNewSubtask(subtask);
        epic.addNewSubtask(subtask);
        taskManager.updateEpic(epic);

        assertEquals(subtaskId1, taskManager.getSubtask(subtaskId1).getId(),
                "Id созданной и сохранённой подзадач не совпадают");
        assertEquals(subtask, taskManager.getSubtask(subtaskId1), "Созданная и сохранённая подзадачи не совпадают ");
        assertEquals(StatusTypes.NEW, taskManager.getSubtask(subtaskId1).getStatus(), "Статус новой подзадачи не NEW");
        assertEquals(epic, taskManager.getSubtask(subtaskId1).getEpic(), "Созданный и сохранённый эпики не совпадают");

        subtask = new Subtask("2nd test subtask", "2nd test description", epic);
        epic.addNewSubtask(subtask);
        int subtaskId2 = taskManager.addNewSubtask(subtask);
        taskManager.updateEpic(epic);

        assertNotEquals(subtaskId1, subtaskId2, "Id разных задач совпадают");
        assertEquals(2, taskManager.getSubtasks().size(), "Неожиданное количество подзадач в TaskManager");
        assertEquals(2, taskManager.getEpic(epic.getId()).getSubtasks().size(), "Неожиданное количество задач в эпике");

        String expectedName = "Modified name";
        String expectedDescription = "Modified description";
        StatusTypes expectedStatus = StatusTypes.IN_PROGRESS;
        subtask = taskManager.getSubtask(subtaskId1);
        subtask.setName(expectedName);
        subtask.setDescription(expectedDescription);
        subtask.setStatus(expectedStatus);
        taskManager.updateSubtask(subtask);

        assertEquals(expectedName, taskManager.getSubtask(subtask.getId()).getName(),
                "Изменённое наименование не сохранилось");
        assertEquals(expectedDescription, taskManager.getSubtask(subtask.getId()).getDescription(),
                "Изменённое описание не сохранилось");
        assertEquals(expectedStatus, taskManager.getSubtask(subtask.getId()).getStatus(),
                "Изменённый статус не сохранился");
        assertEquals(expectedStatus, taskManager.getEpic(epic.getId()).getStatus(),
                "Статус эпика не пересчитался после изменения подзадачи");

        taskManager.deleteSubtask(subtaskId2);

        subtask = taskManager.getSubtask(subtaskId2);

        assertNull(subtask, "Подзадача не удалена");

        taskManager.deleteTasks();

        assertEquals(0, taskManager.getTasks().size(), "Неожиданное количество подзадач");
    }

    @DisplayName("Операции с Epic")
    @Test
    void epicCrud() {
        List<Subtask> subtasks = new ArrayList<>();

        Epic epic1 = new Epic("Epic name", "Epic description");
        Subtask subtask = new Subtask("Test subtask", "Test subtask description", epic1);
        int epicId1 = taskManager.addNewEpic(epic1);
        int subtaskId1 = taskManager.addNewSubtask(subtask);
        epic1.addNewSubtask(subtask);
        taskManager.updateEpic(epic1);
        subtasks.add(subtask);

        assertEquals(epic1, taskManager.getEpic(epicId1), "Созданный и сохранённый эпики не совпадают");

        Epic epic2 = new Epic("Second epic name", "Second epic description");
        int epicId2 = taskManager.addNewEpic(epic2);

        assertNotEquals(epicId1, epicId2, "Id разных эпиков совпадают");
        assertNotEquals(taskManager.getEpic(epicId1), taskManager.getEpic(epicId2), "Разные эпики совпадают");
        assertEquals(2, taskManager.getEpics().size(), "Неожиданное количество эпиков");

        subtask = new Subtask("2nd test subtask", "2nd test description", epic1);
        epic1.addNewSubtask(subtask);
        taskManager.updateEpic(epic1);
        subtasks.add(subtask);

        assertEquals(2, taskManager.getEpics().size(), "Неожиданное количество эпиков в TaskManager");
        assertEquals(2, taskManager.getEpic(epic1.getId()).getSubtasks().size(),
                "Неожиданное количество задач в эпике");
        assertEquals(subtasks, taskManager.getEpic(epicId1).getSubtasks(), "Неожиданное количество подзадач");

        String expectedName = "Modified name";
        String expectedDescription = "Modified description";
        StatusTypes expectedStatus = StatusTypes.IN_PROGRESS;
        subtask = taskManager.getSubtask(subtaskId1);
        subtask.setStatus(expectedStatus);
        epic1.setName(expectedName);
        epic1.setDescription(expectedDescription);
        taskManager.updateSubtask(subtask);
        taskManager.updateEpic(epic1);

        assertEquals(expectedStatus, taskManager.getSubtask(subtaskId1).getStatus(),
                "Изменённый статус подзадачи не сохранился");
        assertEquals(expectedStatus, taskManager.getEpic(epicId1).getStatus(),
                "Статус эпика не пересчитался после изменения подзадачи");

        taskManager.deleteEpic(epicId1);

        epic1 = taskManager.getEpic(epicId1);

        assertNull(epic1, "Эпик не удалён");

        taskManager.deleteEpics();

        assertEquals(0, taskManager.getEpics().size(), "Неожиданное количество эпиков'");
    }
}