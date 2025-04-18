package ru.yandex.practicum.scheduler.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

public class InMemoryTaskManager implements TaskManager {

    protected final HistoryManager historyManager;
    protected int generatedId = 0;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>();

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    protected int getNextId() {
        return ++generatedId;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
    }

    private boolean checkIntersectionByTimeBetweenTwoTasks(Task firstTask, Task secondTask) {
        // Проверяем пересечение времени старта и окончания задач.
        // Если время начала или окончания обеих задач пустые
        if (firstTask.getStartTime() == null || firstTask.getEndTime() == null || secondTask.getStartTime() == null
                || secondTask.getEndTime() == null) {
            throw new IllegalArgumentException("Дата начала и длительность не указаны");
        } else {
            // Иначе возвращаем результат проверки пересечения
            return firstTask.getEndTime().isAfter(secondTask.getStartTime()) &&
                    firstTask.getStartTime().isBefore(secondTask.getEndTime());
        }
    }

    protected void checkIntersectionByTimeBetweenTaskAndStorage(Task task) {
        boolean result = getPrioritizedTasks().stream()
                .filter(existinfTask -> !existinfTask.equals(task))
                .anyMatch(existingTask -> checkIntersectionByTimeBetweenTwoTasks(task, existingTask));
        if (result) {
            throw new IllegalArgumentException("Найдено пересечение по времени выполнения");
        }
    }

    protected void addPrioritizedTask(Task task) {
        // Проверяем пересечение с другими задачами
        checkIntersectionByTimeBetweenTaskAndStorage(task);
        // и добавляем в список приоритетов
        prioritizedTasks.add(task);

    }

    protected void deletePrioritizedTask(Task task) {
        // Удаляем задачу из списка приоритетов
        prioritizedTasks.remove(task);
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.addToHistory(task);
        return task;
    }

    @Override
    public int addNewTask(Task task) {
        // Проверяем задачу на пересечение.
        checkIntersectionByTimeBetweenTaskAndStorage(task);
        // Получаем и устанавливаем следующий rowId
        task.setId(getNextId());
        // Добавляем задачу в список приоритетов
        addPrioritizedTask(task);
        // и в хранилище.
        tasks.put(task.getId(), task);
        // Возвращаем ИД добавленной задачи
        return task.getId();
    }

    @Override
    public void updateTask(Task task) {
        // Проверяем задачу на заполненность.
        if (task != null) {
            // Проверяем задачу на пересечение.
            checkIntersectionByTimeBetweenTaskAndStorage(task);
            // Удаляем задачу из списка приоритетов если она есть,
            // чтобы не искать лишний раз её в списке приоритетов.
            deletePrioritizedTask(task);
            // Снова добавляем задачу в список приоритетов.
            addPrioritizedTask(task);
            // Обновляем задачу, если она уже есть в хранилище.
            if (tasks.containsKey(task.getId())) {
                tasks.put(task.getId(), task);
            }
        } else {
            // Иначе выбрасываем исключение
            throw new IllegalArgumentException("Передана пустая задача");
        }
    }

    @Override
    public void deleteTask(int id) {
        // Получаем задачу из хранилища
        Task task = tasks.get(id);
        // Удаляем задачу из списка приоритетов,
        deletePrioritizedTask(task);
        // истории
        historyManager.remove(id);
        // и хранилища
        tasks.remove(id);
    }

    @Override
    public void deleteTasks() {
        for (Task task : getTasks()) {
            deleteTask(task.getId());
        }
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = getEpicInternal(id);
        historyManager.addToHistory(epic);
        return epic;
    }

    private Epic getEpicInternal(int id) {
        return epics.get(id);
    }

    @Override
    public int addNewEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic tempEpic = getEpicInternal(epic.getId());

            tempEpic.setName(epic.getName());
            tempEpic.setDescription(epic.getDescription());

            epics.put(tempEpic.getId(), tempEpic);
        }
    }

    @Override
    public void deleteEpic(int id) {
        // Получаем эпик
        Epic epic = epics.get(id);
        for (Subtask subtask : epic.getSubtasks()) {
            // Удаляем дочерние подзадачи из списка приоритетов,
            deletePrioritizedTask(subtask);
            // из истории обращений
            historyManager.remove(subtask.getId());
            // и из хранилища
            subtasks.remove(subtask.getId());
        }
        // Удаляем эпик из истории
        historyManager.remove(id);
        // и из хранилища
        epics.remove(id);
    }

    @Override
    public void deleteEpics() {
        for (Epic epic : getEpics()) {
            deleteEpic(epic.getId());
        }
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = getSubtaskInternal(id);
        historyManager.addToHistory(subtask);
        return subtask;
    }

    private Subtask getSubtaskInternal(int id) {
        return subtasks.get(id);
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = getEpicInternal(epicId);

        if (epic != null) {
            return epic.getSubtasks();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        if (subtask != null) {
            // Проверяем подзадачу на пересечение.
            checkIntersectionByTimeBetweenTaskAndStorage(subtask);
            // Получаем эпик.
            Epic epic = subtask.getEpic();

            // Если ИД полученного эпика не null,
            if (getEpicInternal(epic.getId()) != null) {
                // то добавляем подзадачу к эпику,
                epic.addNewSubtask(subtask);
                // устанавливаем ИД подзадачи,
                subtask.setId(getNextId());
                // добавляем подзадачу в список приоритетов,
                addPrioritizedTask(subtask);
                // добавляем подзадачу в хранилище,
                subtasks.put(subtask.getId(), subtask);
                // и пересчитываем поля эпика.
                epic.calculateFields();
                // Возвращаем ИД подзадачи
                return subtask.getId();
            } else {
                // Иначе возвращаем 0
                throw new IllegalArgumentException("Эпик с id " + epic.getId() + " не найден в хранилище");
            }
        } else {
            throw new IllegalArgumentException("Передана пустая подзадача");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask != null) {
            // Если подзадача есть в хранилище,
            if (subtasks.containsKey(subtask.getId())) {
                // то проверяем подзадачу на пересечение,
                checkIntersectionByTimeBetweenTaskAndStorage(subtask);
                // обновляем подзадачу,
                subtasks.put(subtask.getId(), subtask);
                // получаем эпик,
                Epic epic = getEpicInternal(subtask.getEpic().getId());
                // обновляем подзадачу в эпике
                epic.updateSubtask(subtask);
                // удаляем подзадачу из списка приоритетов, если она есть
                deletePrioritizedTask(subtask);
                // и добавляем в список приоритетов снова.
                addPrioritizedTask(subtask);
                // Пересчитываем поля эпика
                epic.calculateFields();
            }
        } else {
            throw new IllegalArgumentException("Передана пустая подзадача");
        }
    }

    @Override
    public void deleteSubtask(int id) {
        // Находим подзадачу
        Subtask subtask = subtasks.get(id);

        // Если подзадача найдена
        if (subtask != null) {
            // Получаем родительский эпик.
            Epic epic = getEpicInternal(subtask.getEpic().getId());
            // Удаляем подзадачу из хранилища.
            subtasks.remove(id);
            // Удаляем подзадачу из списка приоритетов, если она есть.
            deletePrioritizedTask(subtask);
            // Удаляем подзадачу из истории.
            historyManager.remove(id);
            // Удаляем подзадачу из эпика.
            epic.deleteSubtask(subtask);
            // Пересчитываем поля эпика
            epic.calculateFields();
        }
    }

    @Override
    public void deleteSubtasks() {
        // Пройдёмся по всем задачам.
        for (Subtask subtask : getSubtasks()) {
            // Удалим их из списка приоритетов, если они есть.
            deletePrioritizedTask(subtask);
            // Удалим их из истории.
            historyManager.remove(subtask.getId());
        }

        // Для каждого эпика
        for (Epic epic : getEpics()) {
            // Удаляем подзадачи
            epic.clearSubtasks();
            // Пересчитаем поля эпика
            epic.calculateFields();
        }

        // Очистим хранилище подзадач
        subtasks.clear();
    }
}
