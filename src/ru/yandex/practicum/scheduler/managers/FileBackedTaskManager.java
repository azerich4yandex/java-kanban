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

    private static final String FILE_HEADER = "ID,TYPE,NAME,STATUS,DESCRIPTION,EPIC";
    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    public static void main(String[] args) {
        Path tempFile;

        try {
            tempFile = File.createTempFile("database", ".csv").toPath();
            tempFile.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new ManagerSaveException(e);
        }

        HistoryManager historyManager = Managers.getDefaultHistory();
        FileBackedTaskManager firstTaskManager = new FileBackedTaskManager(historyManager, tempFile.toFile());

        Task firstTask = new Task("First task name", "First task description");
        firstTaskManager.addNewTask(firstTask);
        Epic firstEpic = new Epic("First epic name", "First epic description");
        firstTaskManager.addNewEpic(firstEpic);
        Subtask firstSubtask = new Subtask("First subtask name", "First subtask description", firstEpic);
        firstTaskManager.addNewSubtask(firstSubtask);
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
            throw new ManagerSaveException(e);
        }
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
                    case TASK -> fileTaskManager.tasks.put(task.getId(), task);
                    case EPIC -> fileTaskManager.epics.put(task.getId(), (Epic) task);
                    case SUBTASK -> fileTaskManager.subtasks.put(task.getId(), (Subtask) task);
                }
            }
        } catch (IOException e) {
            // При проблемах с файлом выбрасываем своё исключение
            throw new ManagerSaveException(e);
        }

        return fileTaskManager;
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
                Epic epic = epics.get(Integer.parseInt(paramsArray[5]));
                Subtask subtask = new Subtask(Integer.parseInt(paramsArray[0]), StatusTypes.valueOf(paramsArray[3]),
                        paramsArray[2], paramsArray[4], startTime, duration, epic);
                epic.addNewSubtask(subtask);
                // Пересчитываем поля эпика
                epic.calculateFields();
                return subtask;
            }
        }
        // В противном случае возвращаем null
        return null;
    }

    @Override
    public int addNewTask(Task task) {
        super.addNewTask(task);
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
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public int addNewEpic(Epic epic) {
        super.addNewEpic(epic);
        save();
        return epic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
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
    public int addNewSubtask(Subtask subtask) {
        super.addNewSubtask(subtask);
        save();
        return subtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
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