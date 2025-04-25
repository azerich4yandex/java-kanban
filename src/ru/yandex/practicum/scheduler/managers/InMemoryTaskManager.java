package ru.yandex.practicum.scheduler.managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import ru.yandex.practicum.scheduler.exceptions.NotAcceptableException;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;

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
            return !(firstTask.getEndTime().isBefore(secondTask.getStartTime())
                    || firstTask.getStartTime().isAfter(secondTask.getEndTime()));
        }
    }

    protected void checkIntersectionByTimeBetweenTaskAndStorage(Task task) {
        boolean result = getPrioritizedTasks().stream()
                .filter(existingTask -> !existingTask.getId().equals(task.getId()))
                .anyMatch(existingTask -> checkIntersectionByTimeBetweenTwoTasks(task, existingTask));
        if (result) {
            throw new NotAcceptableException("Найдено пересечение по времени выполнения");
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
    public Optional<Task> getTaskById(Integer id) {
        Task task = tasks.get(id);

        if (task != null) {
            historyManager.addToHistory(task);
        }

        return Optional.ofNullable(task);
    }

    @Override
    public Integer createTask(Task task) {
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
    public void deleteTask(Integer id) {
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
    public Optional<Epic> getEpicById(Integer id) {
        Epic epic = getEpicInternal(id);
        if (epic != null) {
            historyManager.addToHistory(epic);
        }
        return Optional.ofNullable(epic);
    }

    protected Epic getEpicInternal(int id) {
        return epics.get(id);
    }

    @Override
    public Integer createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic tempEpic = getEpicInternal(epic.getId());

            calculateEpicFields(tempEpic);
            tempEpic.setName(epic.getName());
            tempEpic.setDescription(epic.getDescription());

            epics.put(tempEpic.getId(), tempEpic);
        }
    }

    @Override
    public void deleteEpic(Integer id) {
        if (id != null) {
            // Получим все дочерние подзадачи эпика
            for (Subtask subtask : getEpicSubtasks(id)) {
                // и удалим каждую из списка приоритетов,
                deletePrioritizedTask(subtask);
                // истории обращений
                historyManager.remove(subtask.getId());
                // и хранилища.
                subtasks.remove(subtask.getId());
            }
            // Удаляем эпик из истории
            historyManager.remove(id);
            // и из хранилища
            epics.remove(id);
        }
    }

    @Override
    public void deleteEpics() {
        // Получим все эпики из хранилища
        for (Epic epic : getEpics()) {
            // и удалим каждый
            deleteEpic(epic.getId());
        }
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Optional<Subtask> getSubtaskById(Integer id) {
        Subtask subtask = getSubtaskInternal(id);
        if (subtask != null) {
            historyManager.addToHistory(subtask);
        }
        return Optional.ofNullable(subtask);
    }

    private Subtask getSubtaskInternal(int id) {
        return subtasks.get(id);
    }

    @Override
    public List<Subtask> getEpicSubtasks(Integer epicId) {
        Epic epic = getEpicInternal(epicId);

        if (epic != null) {
            return subtasks.values().stream()
                    .filter(Objects::nonNull)
                    .filter(subtask -> subtask.getEpicId().equals(epic.getId()))
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        if (subtask != null) {
            // Проверяем подзадачу на пересечение.
            checkIntersectionByTimeBetweenTaskAndStorage(subtask);
            // Получаем эпик.
            Epic epic = getEpicInternal(subtask.getEpicId());

            // Если ИД полученного эпика не null,
            if (epic != null) {
                // то добавляем подзадачу к эпику,
                epic.addSubtask(subtask.getId());
                // устанавливаем ИД подзадачи,
                subtask.setId(getNextId());
                // добавляем подзадачу в список приоритетов,
                addPrioritizedTask(subtask);
                // добавляем подзадачу в хранилище,
                subtasks.put(subtask.getId(), subtask);
                // и пересчитываем поля эпика.
                calculateEpicFields(epic);
                // Возвращаем ИД подзадачи
                return subtask.getId();
            } else {
                // Иначе возвращаем 0
                throw new IllegalArgumentException("Эпик с id " + subtask.getEpicId() + " не найден в хранилище");
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
                Epic epic = getEpicInternal(subtask.getEpicId());
                // обновляем подзадачу в эпике
                epic.updateSubtask(subtask.getId());
                // удаляем подзадачу из списка приоритетов, если она есть
                deletePrioritizedTask(subtask);
                // и добавляем в список приоритетов снова.
                addPrioritizedTask(subtask);
                // Пересчитываем поля эпика
                calculateEpicFields(epic);
            }
        } else {
            throw new IllegalArgumentException("Передана пустая подзадача");
        }
    }

    @Override
    public void deleteSubtask(Integer id) {
        // Находим подзадачу
        Subtask subtask = subtasks.get(id);

        // Если подзадача найдена
        if (subtask != null) {
            // Получаем родительский эпик.
            Epic epic = getEpicInternal(subtask.getEpicId());
            // Удаляем подзадачу из хранилища.
            subtasks.remove(id);
            // Удаляем подзадачу из списка приоритетов, если она есть.
            deletePrioritizedTask(subtask);
            // Удаляем подзадачу из истории.
            historyManager.remove(id);
            // Удаляем подзадачу из эпика.
            epic.deleteSubtask(subtask);
            // Пересчитываем поля эпика
            calculateEpicFields(epic);
        }
    }

    @Override
    public void deleteSubtasks() {
        // Пройдёмся по всем задачам.
        for (Subtask subtask : getSubtasks()) {
            // и удалим каждую из списка приоритетов
            deletePrioritizedTask(subtask);
            // и из истории
            historyManager.remove(subtask.getId());
        }
        // Очистим хранилище подзадач
        subtasks.clear();

        // Для каждого эпика
        for (Epic epic : getEpics()) {
            // Очищаем подзадачи
            epic.clearSubtasks();
            // и пересчитаем поля эпика
            calculateEpicFields(epic);
        }

    }

    private void calculateEpicStatusField(Epic epic) {
        StatusTypes resultStatus = StatusTypes.IN_PROGRESS;
        long newQuantity = subtasks.values().stream()
                .filter(Objects::nonNull)
                .filter(subtask -> subtask.getEpicId().equals(epic.getId()))
                .filter(subtask -> subtask.getStatus().equals(StatusTypes.NEW))
                .count();
        long doneQuantity = subtasks.values().stream()
                .filter(Objects::nonNull)
                .filter(subtask -> subtask.getEpicId().equals(epic.getId()))
                .filter(subtask -> subtask.getStatus().equals(StatusTypes.DONE))
                .count();

        List<Subtask> receivedSubtasks = getEpicSubtasks(epic.getId());

        if (receivedSubtasks.isEmpty()) {
            resultStatus = StatusTypes.NEW;
        } else if (newQuantity == receivedSubtasks.size()) {
            resultStatus = StatusTypes.NEW;
        } else if (doneQuantity == receivedSubtasks.size()) {
            resultStatus = StatusTypes.DONE;
        }

        epic.setStatus(resultStatus);
    }

    private void calculateEpicTimeFields(Epic epic) {
        // Получаем эпик из хранилища
        Epic receivedEpic = getEpicInternal(epic.getId());

        if (receivedEpic != null) {
            // Получаем подзадачи из полученного эпика
            LocalDateTime startTime = subtasks.values().stream()
                    .filter(subtask -> subtask.getEpicId().equals(epic.getId()))
                    .filter(subtask -> subtask.getStartTime() != null)
                    .min(Comparator.comparing(Task::getStartTime))
                    .map(Task::getStartTime)
                    .orElse(null);

            LocalDateTime endTime = subtasks.values().stream()
                    .filter(subtask -> subtask.getEpicId().equals(epic.getId()))
                    .filter(subtask -> subtask.getStartTime() != null)
                    .min(Comparator.comparing(Task::getStartTime))
                    .map(Task::getStartTime)
                    .orElse(null);

            Duration duration = subtasks.values().stream()
                    .filter(subtask -> subtask.getEpicId().equals(epic.getId()))
                    .map(Task::getDuration)
                    .filter(Objects::nonNull)
                    .reduce(Duration.ZERO, Duration::plus);

            // Устанавливаем полученные значения
            epic.setStartTime(startTime);
            epic.setEndTime(endTime);
            epic.setDuration(duration);
        }
    }

    private void calculateEpicFields(Epic epic) {
        calculateEpicStatusField(epic);
        calculateEpicTimeFields(epic);
    }
}
