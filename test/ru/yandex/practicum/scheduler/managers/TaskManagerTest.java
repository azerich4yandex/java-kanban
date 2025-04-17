package ru.yandex.practicum.scheduler.managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

        epic = new Epic("First epic", "First epic description");

        subtask = new Subtask("First subtask", "First subtask description", task.getEndTime(), Duration.ofMinutes(30),
                epic);
    }

    @DisplayName("Операции с Task: Добавление")
    @Test
    void testAddTask() {
        taskManager.addNewTask(task);
        Optional<Task> retrievedTask = Optional.ofNullable(taskManager.getTask(task.getId()));

        assertTrue(retrievedTask.isPresent(), "Задача не добавлена");
        assertEquals(task, retrievedTask.get(), "Сохранённая и полученная задачи не совпадают");
    }

    @DisplayName("Операции с Task: Получение")
    @Test
    void testGetTask() {
        taskManager.addNewTask(task);
        Optional<Task> retrievedTask = Optional.ofNullable(taskManager.getTask(task.getId()));

        assertTrue(retrievedTask.isPresent(), "Задача не получена");
        assertEquals(task, retrievedTask.get(), "ИД задач не совпадают");
        assertEquals(task.getName(), retrievedTask.get().getName(), "Наименования задач не совпадают");
        assertEquals(task.getDescription(), retrievedTask.get().getDescription(), "Описания задач не совпадают");
        assertEquals(task.getStatus(), retrievedTask.get().getStatus(), "Статусы задач не совпадают");
        assertEquals(task.getType(), retrievedTask.get().getType(), "Типы задач не совпадают");
        assertEquals(task.getStartTime(), retrievedTask.get().getStartTime(), "Время начала задач не совпадает");
        assertEquals(task.getDuration(), retrievedTask.get().getDuration(), "Длительность задач не совпадает");
        assertEquals(task.getEndTime(), retrievedTask.get().getEndTime(), "Время окончания задач не совпадает");
    }

    @DisplayName("Операции с Task: Получение списка")
    @Test
    void testGetTasks() {
        taskManager.addNewTask(task);
        List<Task> tasks = taskManager.getTasks();

        assertEquals(1, tasks.size(), "Не вернулось ожидаемое количество задач");
        assertEquals(task, tasks.getFirst(), "Возвращённая и сохраненная задачи не совпадают");

        taskManager.deleteTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Хранилище задач не очищено");
    }

    @DisplayName("Операции с Task: Изменение")
    @Test
    void testUpdateTask() {
        taskManager.addNewTask(task);
        Task updatedTask = new Task("Updated Task", "Updated Description", LocalDateTime.now(), Duration.ofMinutes(60));
        updatedTask.setId(task.getId());
        taskManager.updateTask(updatedTask);
        Optional<Task> retrievedTask = Optional.ofNullable(taskManager.getTask(task.getId()));

        assertTrue(retrievedTask.isPresent(), "Задача не обновлена");
        assertEquals(updatedTask, retrievedTask.get(), "Задача не обновлена новыми значениями");
    }

    @DisplayName("Операции с Task: Удаление")
    @Test
    void testDeleteTask() {
        taskManager.addNewTask(task);
        Task task1 = new Task("Second task", "Task description", task.getEndTime().plusMinutes(1), task.getDuration());
        taskManager.addNewTask(task1);
        taskManager.deleteTask(task1.getId());
        Optional<Task> retrievedTask = Optional.ofNullable(taskManager.getTask(task1.getId()));

        assertFalse(retrievedTask.isPresent(), "Задача не удалена");
        assertEquals(1, taskManager.getTasks().size(), "После удаления количество задач не изменилось");
    }

    @DisplayName("Операции с Task: Очистка хранилища")
    @Test
    void testClearTasks() {
        taskManager.addNewTask(task);
        Task task1 = new Task("Second task", "Task description", task.getEndTime().plusMinutes(1), task.getDuration());
        taskManager.addNewTask(task1);
        taskManager.deleteTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "Хранилище задач не очищено");
    }

    @DisplayName("Операции с Epic: Добавление")
    @Test
    void testAddEpic() {
        taskManager.addNewEpic(epic);
        Optional<Epic> retrievedEpic = Optional.ofNullable(taskManager.getEpic(epic.getId()));

        assertTrue(retrievedEpic.isPresent(), "Эпик не добавлен");
        assertEquals(epic, retrievedEpic.get(), "Сохранённый и полученный эпики не совпадают");
    }

    @DisplayName("Операции с Epic: Получение")
    @Test
    void testGetEpic() {
        taskManager.addNewEpic(epic);
        subtask.setEpic(epic);
        taskManager.addNewSubtask(subtask);
        Optional<Epic> retrievedEpic = Optional.ofNullable(taskManager.getEpic(epic.getId()));

        assertTrue(retrievedEpic.isPresent(), "Эпик не получен");
        assertEquals(epic, retrievedEpic.get(), "ИД эпиков не совпадают");
        assertEquals(epic.getName(), retrievedEpic.get().getName(), "Наименование эпиков не совпадают");
        assertEquals(epic.getDescription(), retrievedEpic.get().getDescription(), "Описания эпиков не совпадают");
        assertEquals(epic.getType(), retrievedEpic.get().getType(), "Типы эпиков не совпадают");
        assertEquals(epic.getStatus(), retrievedEpic.get().getStatus(), "Статусы эпиков не совпадают");
        assertEquals(epic.getSubtasks(), retrievedEpic.get().getSubtasks(), "Подзадачи эпиков не совпадают");
        assertEquals(epic.getStartTime(), retrievedEpic.get().getStartTime(), "Время начала эпиков не совпадает");
        assertEquals(epic.getDuration(), retrievedEpic.get().getDuration(), "Длительность эпиков не совпадает");
        assertEquals(epic.getEndTime(), retrievedEpic.get().getEndTime(), "Время окончания эпиков не совпадает");
    }

    @DisplayName("Операции с Epic: Получение списка")
    @Test
    void testGetEpics() {
        taskManager.addNewEpic(epic);
        List<Epic> epics = taskManager.getEpics();
        assertEquals(1, epics.size(), "Не вернулось ожидаемое количество эпиков");
        assertEquals(epic, epics.getFirst(), "Возвращённый и сохраненный эпики не совпадают");

        taskManager.deleteEpics();
        assertTrue(taskManager.getEpics().isEmpty(), "Хранилище эпиков не очищено");
    }

    @DisplayName("Операции с Epic: Получение списка подзадач")
    @Test
    void testGetSubtaskByEpic() {
        taskManager.addNewEpic(epic);
        subtask.setEpic(epic);
        taskManager.addNewSubtask(subtask);
        List<Subtask> subtasks = taskManager.getEpicSubtasks(epic.getId());

        assertEquals(1, subtasks.size(), "Не вернулось ожидаемое количество подзадач");
        assertEquals(subtask, subtasks.getFirst(), "Сохранённая и полученная подзадачи не совпадают");
    }


    @DisplayName("Операции с Epic: Изменение")
    @Test
    void testUpdateEpic() {
        taskManager.addNewEpic(epic);
        Epic updatedEpic = new Epic("Updated Epic", "Updated Description");
        updatedEpic.setId(epic.getId());
        taskManager.updateEpic(updatedEpic);
        Optional<Epic> retrievedEpic = Optional.ofNullable(taskManager.getEpic(epic.getId()));

        assertTrue(retrievedEpic.isPresent(), "Эпик не обновлён");
        assertEquals(updatedEpic.getName(), retrievedEpic.get().getName(), "Наименование эпика не обновилось");
        assertEquals(updatedEpic.getDescription(), retrievedEpic.get().getDescription(),
                "Описание эпика не обновилось");
    }

    @DisplayName("Операции с Epic: Удаление")
    @Test
    void testDeleteEpic() {
        taskManager.addNewEpic(epic);

        Epic epic1 = new Epic("Second epic", "Second epic description");
        taskManager.addNewEpic(epic1);
        taskManager.deleteEpic(epic1.getId());
        Optional<Epic> retrievedEpic = Optional.ofNullable(taskManager.getEpic(epic1.getId()));

        assertFalse(retrievedEpic.isPresent(), "Эпик не удалён");
        assertEquals(1, taskManager.getEpics().size(), "После удаления количество эпиков не изменилось");
    }

    @DisplayName("Операции с Epic: Очистка хранилища")
    @Test
    void testClearEpics() {
        taskManager.addNewEpic(epic);
        subtask.setEpic(epic);
        taskManager.addNewSubtask(subtask);
        taskManager.deleteEpics();

        assertTrue(taskManager.getEpics().isEmpty(), "Хранилище эпиков не очищено");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Хранилище подзадач не очищено вместе с эпиками");
    }

    @DisplayName("Операции с Subtask: Добавление")
    @Test
    void testAddSubtask() {
        taskManager.addNewEpic(epic);
        subtask.setEpic(epic);
        taskManager.addNewSubtask(subtask);
        Optional<Subtask> retrievedSubTask = Optional.ofNullable(taskManager.getSubtask(subtask.getId()));
        assertTrue(retrievedSubTask.isPresent(), "SubTask should be added");
        assertEquals(subtask, retrievedSubTask.get(), "Retrieved subtask should match the added subtask");
        assertEquals(epic.getId(), retrievedSubTask.get().getEpic().getId(),
                "SubTask should be linked to the correct Epic");
    }

    @DisplayName("Операции с Subtask: Получение")
    @Test
    void testGetSubtask() {
        taskManager.addNewEpic(epic);
        subtask.setEpic(epic);
        taskManager.addNewSubtask(subtask);
        Optional<Subtask> retrievedSubtask = Optional.ofNullable(taskManager.getSubtask(subtask.getId()));

        assertTrue(retrievedSubtask.isPresent(), "Подзадача не получена");
        assertEquals(subtask, retrievedSubtask.get(), "ИД подзадач не совпадают");
        assertEquals(subtask.getName(), retrievedSubtask.get().getName(), "Наименования подзадач не совпадают");
        assertEquals(subtask.getDescription(), retrievedSubtask.get().getDescription(),
                "Описания подзадач не совпадают");
        assertEquals(subtask.getType(), retrievedSubtask.get().getType(), "Типы подзадач не совпадают");
        assertEquals(subtask.getStatus(), retrievedSubtask.get().getStatus(), "Статусы подзадач не совпадают");
        assertEquals(subtask.getStartTime(), retrievedSubtask.get().getStartTime(),
                "Время начала подзадач не совпадает");
        assertEquals(subtask.getDuration(), retrievedSubtask.get().getDuration(), "Длительность подзадач не совпадает");
        assertEquals(subtask.getEndTime(), retrievedSubtask.get().getEndTime(),
                "Время окончания подзадач не совпадает");
    }

    @DisplayName("Операции с Subtask: Получение списка")
    @Test
    void testGetSubtasks() {
        taskManager.addNewEpic(epic);
        subtask.setEpic(epic);
        taskManager.addNewSubtask(subtask);
        List<Subtask> subtasks = taskManager.getSubtasks();
        assertEquals(1, subtasks.size(), "Не вернулось ожидаемое количество подзадач");
        assertEquals(subtask, subtasks.getFirst(), "Сохранённая и возвращённая подзадачи не совпадают");

        taskManager.deleteSubtasks();
        assertTrue(taskManager.getSubtasks().isEmpty(), "Хранилище подзадач не очищено");
    }

    @DisplayName("Операции с Subtask: Изменение")
    @Test
    void testUpdateSubtask() {
        taskManager.addNewEpic(epic);
        subtask.setEpic(epic);
        taskManager.addNewSubtask(subtask);
        Subtask updatedSubTask = new Subtask("Updated SubTask", "Updated Description", LocalDateTime.now(),
                Duration.ofMinutes(60), epic);
        updatedSubTask.setStatus(StatusTypes.DONE);
        updatedSubTask.setId(subtask.getId());
        taskManager.updateSubtask(updatedSubTask);
        Optional<Subtask> retrievedSubTask = Optional.ofNullable(taskManager.getSubtask(subtask.getId()));

        assertTrue(retrievedSubTask.isPresent(), "Подзадача не обновлена");
        assertEquals(updatedSubTask.getName(), retrievedSubTask.get().getName(),
                "Наименование подзадачи не обновилось");
        assertEquals(updatedSubTask.getDescription(), retrievedSubTask.get().getDescription(),
                "Описание подзадачи не обновилось");
    }

    @DisplayName("Операции с Subtask: Удаление")
    @Test
    void testDeleteSubtask() {
        taskManager.addNewEpic(epic);
        subtask.setEpic(epic);
        taskManager.addNewSubtask(subtask);
        Subtask subtask1 = new Subtask("Second subtask", "Second description", subtask.getEndTime(),
                subtask.getDuration(), epic);
        taskManager.addNewSubtask(subtask1);
        taskManager.deleteSubtask(subtask.getId());
        Optional<Subtask> retrievedSubtask = Optional.ofNullable(taskManager.getSubtask(subtask.getId()));

        assertFalse(retrievedSubtask.isPresent(), "Подзадача не удалена");
        assertEquals(1, taskManager.getSubtasks().size(), "После удаления количество подзадач не изменилось");
    }

    @DisplayName("Операции с Subtask: Очистка хранилища")
    @Test
    void testClearSubtask() {
        taskManager.addNewEpic(epic);
        subtask.setEpic(epic);
        taskManager.addNewSubtask(subtask);
        taskManager.deleteSubtasks();

        assertTrue(taskManager.getSubtasks().isEmpty(), "Хранилище подзадач не очищено");

        Optional<Epic> retrievedEpic = Optional.ofNullable(taskManager.getEpic(epic.getId()));

        assertTrue(retrievedEpic.isPresent(), "Родительский эпик удалён");
        assertTrue(retrievedEpic.get().getSubtasks().isEmpty(), "Список подзадач родительского эпика не очищен");
    }

    @DisplayName("Получение списка приоритетных задач")
    @Test
    void testGetPrioritizedTasks() {
        Task task2 = new Task("Second task", "Task description",
                task.getStartTime().minus(Duration.ofMinutes(30).plusMinutes(1)),
                Duration.ofMinutes(30));
        taskManager.addNewTask(task);
        taskManager.addNewTask(task2);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size(), "Не вернулось ожидаемое количество задач");
        assertEquals(task2, prioritizedTasks.get(0), "Более ранняя задача должна возвращаться первой");
        assertEquals(task, prioritizedTasks.get(1), "Более поздняя задача должна возвращаться последней");
    }

    @DisplayName("Операции с историей просмотров: Получение списка")
    @Test
    void testGetHistory() {
        taskManager.addNewTask(task);
        taskManager.getTask(task.getId());
        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size(), "Не вернулось ожидаемое количество задач");
        assertEquals(task, history.getFirst(), "История не содержит просмотренную задачу");
    }
}