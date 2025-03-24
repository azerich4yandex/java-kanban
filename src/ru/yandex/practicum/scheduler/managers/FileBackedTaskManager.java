package ru.yandex.practicum.scheduler.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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

    public static void main(String[] args) throws ManagerSaveException {
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

        FileBackedTaskManager secondTaskManager = new FileBackedTaskManager(historyManager, tempFile.toFile());
        secondTaskManager.loadFromFile(tempFile.toFile());
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

    public void save() throws ManagerSaveException {
        // try with resources
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            // Пишем первую строчку
            fw.write(FILE_HEADER + "\n");
            // Если в хранилище нет данных
            if (super.getAllTasks().isEmpty()) {
                // пишем пустую строку.
                fw.write("");
            } else {
                // Иначе получаем все сущности
                for (Task task : super.getAllTasks()) {
                    // и записываем их в файл
                    fw.write(task.toString() + "\n");
                }
            }
        } catch (IOException e) {
            // При проблемах с файлом выбрасываем своё исключение
            throw new ManagerSaveException(e);
        }
    }

    public void loadFromFile(File file) throws ManagerSaveException {
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
                Task task = fromString(line);
                // В зависимости от пита объекта добавляем значение в хранилище
                switch (task.getType()) {
                    case TASK -> addNewTask(task);
                    case EPIC -> {
                        Epic epic = new Epic(task.getName(), task.getDescription());
                        epic.setId(task.getId());
                        epic.setStatus(task.getStatus());
                        addNewEpic(epic);
                    }
                    case SUBTASK -> {
                        Epic epic = getEpic(Integer.parseInt(line.split(",")[5]));
                        Subtask subtask = new Subtask(task.getName(), task.getDescription(), epic);
                        subtask.setId(task.getId());
                        subtask.setStatus(task.getStatus());
                        addNewSubtask(subtask);
                    }
                }
            }
        } catch (IOException e) {
            // При проблемах с файлом выбрасываем своё исключение
            throw new ManagerSaveException(e);
        }
    }

    private Task fromString(String line) {
        // Создаём массив из строки
        String[] paramsArray = line.split(",");
        // Создаём пустую задачу
        Task task = new Task("", "");
        // Устанавливаем ИД
        task.setId(Integer.parseInt(paramsArray[0]));
        // тип сущности
        task.setType(TaskTypes.fromString(paramsArray[1]));
        // наименование
        task.setName(paramsArray[2]);
        // статус
        task.setStatus(StatusTypes.fromString(paramsArray[3]));
        // и описание
        task.setDescription(paramsArray[4]);

        // Возвращаем созданный объект
        return task;
    }

    @Override
    public int addNewTask(Task task) {
        int result = super.addNewTask(task);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int addNewEpic(Epic epic) {
        int result = super.addNewEpic(epic);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int result = super.addNewSubtask(subtask);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();

        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }
}
