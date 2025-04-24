package ru.yandex.practicum.scheduler.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import ru.yandex.practicum.scheduler.exceptions.ManagerSaveException;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;
import ru.yandex.practicum.scheduler.models.enums.TaskTypes;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String FILE_HEADER = "ID,TYPE,NAME,STATUS,DESCRIPTION,EPIC,START_TIME,DURATION";
    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    public static void main(String[] args) {
        Path tempFile = null;

        try {
            tempFile = File.createTempFile("database", ".csv").toPath();
            tempFile.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при создании временного файла " + tempFile.toFile().getPath(), e);
        }

        HistoryManager historyManager = Managers.getDefaultHistory();
        FileBackedTaskManager firstTaskManager = new FileBackedTaskManager(historyManager, tempFile.toFile());

        Task firstTask = new Task("First task name", "First task description", LocalDateTime.now(),
                Duration.ofMinutes(10));
        firstTaskManager.createTask(firstTask);
        Epic firstEpic = new Epic("First epic name", "First epic description");
        firstTaskManager.createEpic(firstEpic);
        Subtask firstSubtask = new Subtask("First subtask name", "First subtask description",
                firstTask.getEndTime().plusMinutes(1), firstTask.getDuration(), firstEpic.getId());
        firstTaskManager.createSubtask(firstSubtask);
        List<Task> expected = new ArrayList<>(firstTaskManager.getAllTasks());

        FileBackedTaskManager secondTaskManager = loadFromFile(tempFile.toFile());
        List<Task> result = new ArrayList<>(secondTaskManager.getAllTasks());

        for (Task task : expected) {
            if (!result.contains(task)) {
                throw new ManagerSaveException();
            }
        }

        if (expected.size() != result.size()) {
            throw new ManagerSaveException();
        }

    }

    protected List<Task> getAllTasks() {
        List<Task> result = new ArrayList<>();
        result.addAll(getTasks());
        result.addAll(getEpics());
        result.addAll(getSubtasks());
        return result;
    }

    static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileTaskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
        // try with resources
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            // Пока есть что читать
            while (br.ready()) {
                // получаем строку
                String line = br.readLine();
                // Если строка пустая или это заголовок файла
                if (line.isBlank() || line.equals(FILE_HEADER)) {
                    // пропускаем её
                    continue;
                }
                // Создаём объект из строки
                Task task = fileTaskManager.fromString(line);
                // Сравниваем полученный rowId и счётчик
                if (task.getId() > fileTaskManager.generatedId) {
                    fileTaskManager.generatedId = task.getId();
                }
                // В зависимости от типа объекта добавляем значение в хранилище
                switch (task.getType()) {
                    case TASK -> {
                        // Добавляем задачу в список приоритетов
                        fileTaskManager.addPrioritizedTask(task);
                        // Добавляем задачу в хранилище
                        fileTaskManager.tasks.put(task.getId(), task);
                    }
                    case EPIC -> fileTaskManager.epics.put(task.getId(), (Epic) task);
                    case SUBTASK -> {
                        Subtask subtask = (Subtask) task;
                        // Добавляем подзадачу в список приоритетов
                        fileTaskManager.addPrioritizedTask(task);
                        // Добавляем подзадачу в хранилище
                        fileTaskManager.subtasks.put(task.getId(), subtask);
                        // Получаем родительский эпик
                        Epic recevedEpic = fileTaskManager.getEpicInternal(subtask.getEpicId());
                        recevedEpic.addSubtask(subtask.getId());
                        fileTaskManager.calculateEpicFields(recevedEpic);
                        fileTaskManager.updateEpic(recevedEpic);
                    }
                }
            }
        } catch (IOException e) {
            // При проблемах с файлом выбрасываем своё исключение
            throw new ManagerSaveException("Ошибка при загрузке из файла " + file.getPath(), e);
        }

        return fileTaskManager;
    }

    private void save() {
        // try with resources
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            // Пишем первую строчку
            fw.write(FILE_HEADER + "\n");
            // Если в хранилище нет данных
            if (getAllTasks().isEmpty()) {
                // пишем пустую строку.
                fw.write("");
            } else {
                // Иначе получаем все сущности
                for (Task task : getAllTasks()) {
                    // и записываем их в файл
                    fw.write(task.toCSV() + "\n");
                }
            }
        } catch (IOException e) {
            // При проблемах с файлом выбрасываем своё исключение
            throw new ManagerSaveException("Ошибка при сохранении в файл " + file.getPath(), e);
        }
    }

    private Task fromString(String line) {
        // Создаём массив из строки
        String[] paramsArray = line.split(",");
        // Получаем тип сущности
        TaskTypes type = TaskTypes.valueOf(paramsArray[1]);
        // Получаем дату начала или null
        LocalDateTime startTime =
                paramsArray[6].isBlank() ? null : LocalDateTime.parse(paramsArray[6], Task.getFormatter());
        // Получаем длительность или null
        Duration duration =
                paramsArray[7].isBlank() ? null : Duration.of(Integer.parseInt(paramsArray[7]), ChronoUnit.MINUTES);

        // В зависимости от типа задачи
        switch (type) {
            case TASK -> {
                return new Task(Integer.parseInt(paramsArray[0]), StatusTypes.valueOf(paramsArray[3]),
                        paramsArray[2], paramsArray[4], startTime, duration);
            }
            case EPIC -> {
                return new Epic(Integer.parseInt(paramsArray[0]), StatusTypes.valueOf(paramsArray[3]), paramsArray[2],
                        paramsArray[4]);
            }
            case SUBTASK -> {
                // Получаем эпик из хранилища
                Epic epic = getEpicInternal(Integer.parseInt(paramsArray[5]));
                return new Subtask(Integer.parseInt(paramsArray[0]), StatusTypes.valueOf(paramsArray[3]),
                        paramsArray[2], paramsArray[4], startTime, duration, epic.getId());
            }
        }
        // В противном случае возвращаем null
        return null;
    }

    @Override
    public Integer createTask(Task task) {
        super.createTask(task);
        // Сохраняем состояние хранилища
        save();
        return task.getId();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(Integer id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public Integer createEpic(Epic epic) {
        super.createEpic(epic);
        save();
        return epic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(Integer id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteEpics() {
        for (Epic epic : epics.values()) {
            deleteEpic(epic.getId());
        }
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
        return subtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(Integer id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            deleteSubtask(subtask.getId());
        }
    }
}