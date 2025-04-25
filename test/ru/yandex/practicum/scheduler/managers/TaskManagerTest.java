package ru.yandex.practicum.scheduler.managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    protected Task task;
    protected Epic epic;
    protected Subtask subtask;

    // Абстрактный метод для инициализации taskManager в подклассах
    protected abstract T createTaskManager();

    @BeforeEach
    void init() {
        taskManager = createTaskManager();

        task = new Task("First task", "First task description", LocalDateTime.now(), Duration.ofMinutes(60));
        taskManager.createTask(task);

        epic = new Epic("First epic", "First epic description");
        taskManager.createEpic(epic);

        subtask = new Subtask("First subtask", "First subtask description", task.getEndTime().plusMinutes(1),
                Duration.ofMinutes(30),
                epic.getId());
        taskManager.createSubtask(subtask);
    }

    @AfterEach
    void halt() {
        taskManager.deleteTasks();
        taskManager.deleteSubtasks();
        taskManager.deleteEpics();
    }

    @DisplayName("Операции с Task: Добавление")
    @Test
    void testCreateTask() {
        Optional<Task> retrievedTask = taskManager.getTaskById(task.getId());

        assertTrue(retrievedTask.isPresent(), "Задача не добавлена");
        assertEquals(task, retrievedTask.get(), "Сохранённая и полученная задачи не совпадают");
    }

    @DisplayName("Операции с Task: Получение")
    @Test
    void testGetTaskById() {
        Optional<Task> retrievedTaskOpt = taskManager.getTaskById(task.getId());

        assertTrue(retrievedTaskOpt.isPresent(), "Задача не получена");

        Task retrievedTask = retrievedTaskOpt.get();

        assertEquals(task, retrievedTask, "ИД задач не совпадают");
        assertEquals(task.getName(), retrievedTask.getName(), "Наименования задач не совпадают");
        assertEquals(task.getDescription(), retrievedTask.getDescription(), "Описания задач не совпадают");
        assertEquals(task.getStatus(), retrievedTask.getStatus(), "Статусы задач не совпадают");
        assertEquals(task.getType(), retrievedTask.getType(), "Типы задач не совпадают");
        assertEquals(task.getStartTime(), retrievedTask.getStartTime(), "Время начала задач не совпадает");
        assertEquals(task.getDuration(), retrievedTask.getDuration(), "Длительность задач не совпадает");
        assertEquals(task.getEndTime(), retrievedTask.getEndTime(), "Время окончания задач не совпадает");
    }

    @DisplayName("Операции с Task: Получение списка")
    @Test
    void testGetTasksById() {
        List<Task> tasks = taskManager.getTasks();

        assertEquals(1, tasks.size(), "Не вернулось ожидаемое количество задач");
        assertEquals(task, tasks.getFirst(), "Возвращённая и сохраненная задачи не совпадают");

        taskManager.deleteTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Хранилище задач не очищено");
    }

    @DisplayName("Операции с Task: Изменение")
    @Test
    void testUpdateTask() {
        Task updatedTask = new Task("Updated Task", "Updated Description", LocalDateTime.now(), Duration.ofMinutes(60));
        updatedTask.setId(task.getId());

        taskManager.updateTask(updatedTask);

        Optional<Task> retrievedTask = taskManager.getTaskById(task.getId());

        assertTrue(retrievedTask.isPresent(), "Задача не обновлена");
        assertEquals(updatedTask, retrievedTask.get(), "Задача не обновлена новыми значениями");
    }

    @DisplayName("Операции с Task: Удаление")
    @Test
    void testDeleteTask() {
        Task task1 = new Task("Second task", "Task description", subtask.getEndTime().plusMinutes(1),
                task.getDuration());
        taskManager.createTask(task1);
        taskManager.deleteTask(task1.getId());
        Optional<Task> retrievedTask = taskManager.getTaskById(task1.getId());

        assertFalse(retrievedTask.isPresent(), "Задача не удалена");
        assertEquals(1, taskManager.getTasks().size(), "После удаления количество задач не изменилось");
    }

    @DisplayName("Операции с Task: Очистка хранилища")
    @Test
    void testClearTasks() {
        Task task1 = new Task("Second task", "Task description", subtask.getEndTime().plusMinutes(1),
                task.getDuration());
        taskManager.createTask(task1);
        taskManager.deleteTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Хранилище задач не очищено");
    }

    @DisplayName("Операции с Epic: Добавление")
    @Test
    void testCreateEpic() {
        Optional<Epic> retrievedEpic = taskManager.getEpicById(epic.getId());

        assertTrue(retrievedEpic.isPresent(), "Эпик не добавлен");
        assertEquals(epic, retrievedEpic.get(), "Сохранённый и полученный эпики не совпадают");
    }

    @DisplayName("Операции с Epic: Получение")
    @Test
    void testGetEpic() {
        Optional<Epic> retrievedEpicOpt = taskManager.getEpicById(epic.getId());

        assertTrue(retrievedEpicOpt.isPresent(), "Эпик не получен");

        Epic retrievedEpic = retrievedEpicOpt.get();

        assertEquals(epic, retrievedEpic, "ИД эпиков не совпадают");
        assertEquals(epic.getName(), retrievedEpic.getName(), "Наименование эпиков не совпадают");
        assertEquals(epic.getDescription(), retrievedEpic.getDescription(), "Описания эпиков не совпадают");
        assertEquals(epic.getType(), retrievedEpic.getType(), "Типы эпиков не совпадают");
        assertEquals(epic.getStatus(), retrievedEpic.getStatus(), "Статусы эпиков не совпадают");
        assertEquals(epic.getSubtaskIds(), retrievedEpic.getSubtaskIds(), "Подзадачи эпиков не совпадают");
        assertEquals(epic.getStartTime(), retrievedEpic.getStartTime(), "Время начала эпиков не совпадает");
        assertEquals(epic.getDuration(), retrievedEpic.getDuration(), "Длительность эпиков не совпадает");
        assertEquals(epic.getEndTime(), retrievedEpic.getEndTime(), "Время окончания эпиков не совпадает");
    }

    @DisplayName("Операции с Epic: Получение списка")
    @Test
    void testGetEpics() {
        List<Epic> epics = taskManager.getEpics();
        assertEquals(1, epics.size(), "Не вернулось ожидаемое количество эпиков");
        assertEquals(epic, epics.getFirst(), "Возвращённый и сохраненный эпики не совпадают");

        taskManager.deleteEpics();
        assertTrue(taskManager.getEpics().isEmpty(), "Хранилище эпиков не очищено");
    }

    @DisplayName("Операции с Epic: Получение списка подзадач")
    @Test
    void testGetSubtaskByIdByEpic() {
        List<Subtask> subtasks = taskManager.getEpicSubtasks(epic.getId());

        assertEquals(1, subtasks.size(), "Не вернулось ожидаемое количество подзадач");
        assertEquals(subtask, subtasks.getFirst(), "Сохранённая и полученная подзадачи не совпадают");
    }


    @DisplayName("Операции с Epic: Изменение")
    @Test
    void testUpdateEpic() {
        Epic updatedEpic = new Epic("Updated Epic", "Updated Description");
        updatedEpic.setId(epic.getId());

        taskManager.updateEpic(updatedEpic);

        Optional<Epic> retrievedEpicOpt = taskManager.getEpicById(epic.getId());

        assertTrue(retrievedEpicOpt.isPresent(), "Эпик не обновлён");

        Epic retrievedEpic = retrievedEpicOpt.get();

        assertEquals(updatedEpic.getName(), retrievedEpic.getName(), "Наименование эпика не обновилось");
        assertEquals(updatedEpic.getDescription(), retrievedEpic.getDescription(), "Описание эпика не обновилось");
    }

    @DisplayName("Операции с Epic: Удаление")
    @Test
    void testDeleteEpic() {
        Epic epic1 = new Epic("Second epic", "Second epic description");

        taskManager.createEpic(epic1);
        taskManager.deleteEpic(epic1.getId());

        Optional<Epic> retrievedEpic = taskManager.getEpicById(epic1.getId());

        assertFalse(retrievedEpic.isPresent(), "Эпик не удалён");
        assertEquals(1, taskManager.getEpics().size(), "После удаления количество эпиков не изменилось");
    }

    @DisplayName("Операции с Epic: Очистка хранилища")
    @Test
    void testClearEpics() {
        taskManager.deleteEpics();

        assertTrue(taskManager.getEpics().isEmpty(), "Хранилище эпиков не очищено");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Хранилище подзадач не очищено вместе с эпиками");
    }

    @DisplayName("Операции с Subtask: Добавление")
    @Test
    void testCreateSubtask() {
        Optional<Subtask> retrievedSubTaskOpt = taskManager.getSubtaskById(subtask.getId());

        assertTrue(retrievedSubTaskOpt.isPresent(), "SubTask should be added");

        Subtask retrievedSubTask = retrievedSubTaskOpt.get();

        assertEquals(subtask, retrievedSubTask, "Retrieved subtask should match the added subtask");
        assertEquals(epic.getId(), retrievedSubTask.getEpicId(), "SubTask should be linked to the correct Epic");
    }

    @DisplayName("Операции с Subtask: Получение")
    @Test
    void testGetSubtaskById() {
        Optional<Subtask> retrievedSubtaskOpt = taskManager.getSubtaskById(subtask.getId());

        assertTrue(retrievedSubtaskOpt.isPresent(), "Подзадача не получена");

        Subtask retrievedSubtask = retrievedSubtaskOpt.get();

        assertEquals(subtask, retrievedSubtask, "ИД подзадач не совпадают");
        assertEquals(subtask.getName(), retrievedSubtask.getName(), "Наименования подзадач не совпадают");
        assertEquals(subtask.getDescription(), retrievedSubtask.getDescription(), "Описания подзадач не совпадают");
        assertEquals(subtask.getType(), retrievedSubtask.getType(), "Типы подзадач не совпадают");
        assertEquals(subtask.getStatus(), retrievedSubtask.getStatus(), "Статусы подзадач не совпадают");
        assertEquals(subtask.getStartTime(), retrievedSubtask.getStartTime(), "Время начала подзадач не совпадает");
        assertEquals(subtask.getDuration(), retrievedSubtask.getDuration(), "Длительность подзадач не совпадает");
        assertEquals(subtask.getEndTime(), retrievedSubtask.getEndTime(), "Время окончания подзадач не совпадает");
    }

    @DisplayName("Операции с Subtask: Получение списка")
    @Test
    void testGetSubtasksById() {
        List<Subtask> subtasks = taskManager.getSubtasks();
        assertEquals(1, subtasks.size(), "Не вернулось ожидаемое количество подзадач");
        assertEquals(subtask, subtasks.getFirst(), "Сохранённая и возвращённая подзадачи не совпадают");

        taskManager.deleteSubtasks();
        assertTrue(taskManager.getSubtasks().isEmpty(), "Хранилище подзадач не очищено");
    }

    @DisplayName("Операции с Subtask: Изменение")
    @Test
    void testUpdateSubtask() {
        Subtask updatedSubTask = new Subtask("Updated SubTask", "Updated Description",
                subtask.getEndTime().plusMinutes(1),
                Duration.ofMinutes(60), epic.getId());
        updatedSubTask.setStatus(StatusTypes.DONE);
        updatedSubTask.setId(subtask.getId());
        taskManager.updateSubtask(updatedSubTask);

        Optional<Subtask> retrievedSubTaskOpt = taskManager.getSubtaskById(subtask.getId());

        assertTrue(retrievedSubTaskOpt.isPresent(), "Подзадача не обновлена");

        Subtask retrievedSubTask = retrievedSubTaskOpt.get();

        assertEquals(updatedSubTask.getName(), retrievedSubTask.getName(), "Наименование подзадачи не обновилось");
        assertEquals(updatedSubTask.getDescription(), retrievedSubTask.getDescription(),
                "Описание подзадачи не обновилось");
    }

    @DisplayName("Операции с Subtask: Удаление")
    @Test
    void testDeleteSubtask() {
        Subtask subtask1 = new Subtask("Second subtask", "Second description", subtask.getEndTime().plusMinutes(1),
                subtask.getDuration(), epic.getId());
        taskManager.createSubtask(subtask1);

        taskManager.deleteSubtask(subtask.getId());
        Optional<Subtask> retrievedSubtask = taskManager.getSubtaskById(subtask.getId());

        assertFalse(retrievedSubtask.isPresent(), "Подзадача не удалена");
        assertEquals(1, taskManager.getSubtasks().size(), "После удаления количество подзадач не изменилось");
    }

    @DisplayName("Операции с Subtask: Очистка хранилища")
    @Test
    void testClearSubtask() {
        taskManager.deleteSubtasks();

        assertTrue(taskManager.getSubtasks().isEmpty(), "Хранилище подзадач не очищено");

        Optional<Epic> retrievedEpic = taskManager.getEpicById(epic.getId());

        assertTrue(retrievedEpic.isPresent(), "Родительский эпик удалён");
        assertTrue(retrievedEpic.get().getSubtaskIds().isEmpty(), "Список подзадач родительского эпика не очищен");
    }

    @DisplayName("Получение списка приоритетных задач")
    @Test
    void testGetPrioritizedTasks() {
        Task task2 = new Task("Second task", "Task description",
                task.getStartTime().minus(Duration.ofMinutes(30).plusMinutes(1)), Duration.ofMinutes(30));
        taskManager.createTask(task2);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(3, prioritizedTasks.size(), "Не вернулось ожидаемое количество задач");
        assertEquals(task2, prioritizedTasks.get(0), "Более ранняя задача должна возвращаться первой");
        assertEquals(task, prioritizedTasks.get(1), "Более поздняя задача должна возвращаться последней");
    }

    @DisplayName("Операции с историей просмотров: Получение списка")
    @Test
    void testGetHistory() {
        taskManager.getTaskById(task.getId());
        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size(), "Не вернулось ожидаемое количество задач");
        assertEquals(task, history.getFirst(), "История не содержит просмотренную задачу");
    }
}