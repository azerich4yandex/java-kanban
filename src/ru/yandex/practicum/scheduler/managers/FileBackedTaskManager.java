package ru.yandex.practicum.scheduler.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    protected int id = 0;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

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

        FileBackedTaskManager secondTaskManager = firstTaskManager.loadFromFile(tempFile.toFile());
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

    @Override
    protected int getNextId() {
        id++;
        return id;
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
                // В зависимости от типа объекта добавляем значение в хранилище
                switch (task.getType()) {
                    case TASK -> {
                        // Проверяем наличие id в строке
                        if (task.getId() == null) {
                            // Если пусто - получаем из счётчика
                            task.setId(fileTaskManager.getNextId());
                        } else {
                            // Если полученный id больше счётчика
                            if (task.getId() > fileTaskManager.id) {
                                // увеличиваем счётчик
                                fileTaskManager.id = task.getId();
                            }
                        }
                        // Добавляем задачу в хранилище
                        fileTaskManager.tasks.put(task.getId(), task);
                    }
                    case EPIC -> {
                        // Создаём эпик
                        Epic epic = new Epic(task.getName(), task.getDescription());
                        // Проверяем наличие id в строке
                        if (task.getId() == null) {
                            // Если пусто - получаем значение из счётчика
                            epic.setId(fileTaskManager.getNextId());
                        } else {
                            // Если полученный id больше счётчика
                            if (task.getId() > fileTaskManager.id) {
                                // увеличиваем счётчик
                                fileTaskManager.id = task.getId();
                            }
                            // Устанавливаем id эпику
                            epic.setId(task.getId());
                        }
                        // Устанавливаем статус
                        epic.setStatus(task.getStatus());
                        // Добавляем статус в хранилище
                        fileTaskManager.epics.put(epic.getId(), epic);
                    }
                    case SUBTASK -> {
                        // Получаем родительский эпик из хранилища
                        Epic epic = fileTaskManager.epics.get(Integer.parseInt(line.split(",")[5]));
                        // Создаём подзадачу
                        Subtask subtask = new Subtask(task.getName(), task.getDescription(), epic);
                        // Проверяем наличие id в строке
                        if (task.getId() == null) {
                            // Если пусто - получаем значение из счётчика
                            subtask.setId(fileTaskManager.getNextId());
                        } else {
                            // Если полученный id больше счётчика
                            if (task.getId() > fileTaskManager.id) {
                                // Увеличиваем счётчик
                                fileTaskManager.id = task.getId();
                            }
                            // Устанавливаем id
                            subtask.setId(task.getId());
                        }
                        // Устанавливаем статус
                        subtask.setStatus(task.getStatus());
                        // Добавляем подзадачу в хранилище
                        fileTaskManager.subtasks.put(subtask.getId(), subtask);
                        // Добавляем восстановленную подзадачу к эпику
                        epic.addNewSubtask(subtask);
                        // Пересчитываем статус родительского эпика
                        epic.calculateStatus();
                        // Сохраняем изменения эпика в хранилище
                        fileTaskManager.epics.put(epic.getId(), epic);
                    }
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
        TaskTypes type = TaskTypes.fromString(paramsArray[1]);

        // В зависимости от типа задачи
        switch (type) {
            case TASK -> {
                // Создаём пустую задачу
                Task task = new Task();
                // Устанавливаем id
                task.setId(Integer.parseInt(paramsArray[0]));
                // наименование
                task.setName(paramsArray[2]);
                // статус
                task.setStatus(StatusTypes.fromString(paramsArray[3]));
                // и описание
                task.setDescription(paramsArray[4]);

                // Возвращаем созданный объект
                return task;
            }
            case EPIC -> {
                // Создаём пустой эпик
                Epic epic = new Epic();
                // Устанавливаем id
                epic.setId(Integer.parseInt(paramsArray[0]));
                // наименование
                epic.setName(paramsArray[2]);
                // статус
                epic.setStatus(StatusTypes.fromString(paramsArray[3]));
                // и описание
                epic.setDescription(paramsArray[4]);
                // Возвращаем созданный объект
                return epic;
            }
            case SUBTASK -> {
                // Получаем эпик из хранилища
                Epic epic = epics.get(Integer.parseInt(paramsArray[5]));
                // Создаём пустую подзадачу
                Subtask subtask = new Subtask(epic);
                // Устанавливаем id
                subtask.setId(Integer.parseInt(paramsArray[0]));
                // наименование
                subtask.setName(paramsArray[2]);
                // статус
                subtask.setStatus(StatusTypes.fromString(paramsArray[3]));
                // и описание
                subtask.setDescription(paramsArray[4]);
                return subtask;
            }
            case null -> {
                // В противном случае возвращаем null
                return null;
            }
        }
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTask(int id) {
        return tasks.get(id);
    }

    @Override
    public int addNewTask(Task task) {
        // Проверяем наличие id у сущности
        // Если id пустой,
        if (task.getId() == null) {
            // то получаем новый id и присваиваем задаче.
            task.setId(getNextId());
        } else {
            // Иначе
            // если id переданной задачи больше текущего значения счётчика,
            if (task.getId() > id) {
                // то увеличиваем счётчик
                id = task.getId();
            }
        }
        // Добавляем задачу в хранилище
        tasks.put(task.getId(), task);

        // Сохраняем состояние хранилища
        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }

        return task.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTasks() {
        for (Task task : tasks.values()) {
            deleteTask(task.getId());
        }
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpic(int id) {
        return epics.get(id);
    }

    @Override
    public int addNewEpic(Epic epic) {
        if (epic.getId() == null) {
            epic.setId(getNextId());
        } else {
            if (epic.getId() > id) {
                id = epic.getId();
            }
        }
        epics.put(epic.getId(), epic);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }

        return epic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            epic.clearSubtasks();
            epics.remove(id);
        }

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEpics() {
        for (Epic epic : epics.values()) {
            deleteEpic(epic.getId());
        }
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        Epic epic = subtask.getEpic();
        if (subtask.getId() == null) {
            subtask.setId(getNextId());
        } else {
            if (subtask.getId() > id) {
                id = subtask.getId();
            }
        }
        subtasks.put(subtask.getId(), subtask);
        epic.calculateStatus();
        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }

        return subtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Epic epic = subtask.getEpic();
            subtasks.put(subtask.getId(), subtask);
            epic.calculateStatus();
        }

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSubtask(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            Epic epic = epics.get(subtask.getEpic().getId());
            epic.deleteSubtask(subtask);
            subtasks.remove(id);
            epic.calculateStatus();
        }

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            deleteSubtask(subtask.getId());
        }
    }
}