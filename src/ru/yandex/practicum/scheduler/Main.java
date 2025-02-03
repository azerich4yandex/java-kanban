package ru.yandex.practicum.scheduler;

import java.util.HashMap;
import java.util.Scanner;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.SubTask;
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;
import ru.yandex.practicum.scheduler.utils.TaskManager;

public class Main {

    static TaskManager taskManager;
    static Scanner scanner;

    public static void main(String[] args) {
        taskManager = new TaskManager();
        scanner = new Scanner(System.in);
        taskManager.setTasks(getTestData());

        while (true) {
            printMainMenu();

            String command = scanner.nextLine();
            switch (command) {
                case "1" -> printTasks();
                case "2" -> clearTasks();
                case "3" -> addTask();
                case "4" -> updateTask();
                case "5" -> removeTask();
                case "6" -> epicManagement();
                case "7" -> {
                    return;
                }
                default -> System.out.println("Неизвестная команда");
            }
        }

    }

    static void epicManagement() {
        System.out.println("Введите идентификатор задачи для управления ею");
        int id = 0;
        if (scanner.hasNextInt()) {
            id = scanner.nextInt();
        }

        if (!taskManager.getTasks().containsKey(id)) {
            System.out.println("Задача не найдена");
        } else {
            while (true) {
                printEpicMenu();

                String command = scanner.nextLine();
                switch (command) {
                    case "1" -> printEpics(id);
                    case "2" -> clearEpics(id);
                    case "3" -> addEpic(id);
                    case "4" -> updateEpic(id);
                    case "5" -> removeEpic(id);
                    case "6" -> subTaskManagement(id);
                    case "7" -> {
                        return;
                    }
                    default -> System.out.println("Неизвестная команда");
                }
            }
        }
    }

    static void subTaskManagement(int taskId) {
        System.out.println("Введите идентификатор эпика для управления им:");

        int epicId = 0;
        if (scanner.hasNextInt()) {
            epicId = scanner.nextInt();
        }

        if (!taskManager.getTask(taskId).getEpics().containsKey(epicId)) {
            System.out.println("Эпик не найден");
        } else {
            while (true) {
                printSubTaskMenu();

                String command = scanner.nextLine();

                switch (command) {
                    case "1" -> printSubTasks(taskId, epicId);
                    case "2" -> clearSubTasks(taskId, epicId);
                    case "3" -> addSubTask(taskId, epicId);
                    case "4" -> updateSubTask(taskId, epicId);
                    case "5" -> removeSubTask(taskId, epicId);
                    case "6" -> {
                        return;
                    }
                    default -> System.out.println("Неизвестная команда");
                }
            }
        }
    }

    static void removeSubTask(int taskId, int epicId) {
        System.out.println("Введите идентификатор подзадачи:");
        int subTaskId = 0;

        if (scanner.hasNextInt()) {
            subTaskId = scanner.nextInt();
        }

        if (!taskManager.getTask(taskId).getEpic(epicId).getSubTasks().containsKey(subTaskId)) {
            System.out.println("Подзадача не найдена");
        } else {
            taskManager.getTask(taskId).getEpic(epicId).removeSubtask(subTaskId);
            System.out.println("Подзадача удалена");
        }
    }

    static void updateSubTask(int taskId, int epicId) {
        System.out.println("Введите идентификатор подзадачи");
        int subTaskId = 0;
        if (scanner.hasNextInt()) {
            subTaskId = scanner.nextInt();
        }

        if (!taskManager.getTask(taskId).getEpic(epicId).getSubTasks().containsKey(subTaskId)) {
            System.out.println("Введён некорректный номер подзадачи");
        } else {
            SubTask subTask = taskManager.getTask(taskId).getEpic(epicId).getSubTask(subTaskId);

            System.out.println("Введите новое наименование (пусто - оставить без изменений)");
            String newName = scanner.nextLine();
            System.out.println("Введите новое описание (пусто - оставить без изменений)");
            String newDescription = scanner.nextLine();
            System.out.println(
                    "Выберите новый статус (1 - Новая, 2 - В процессе, 3 - Готово, Пусто - оставить без изменений");
            String newStatus = scanner.nextLine();

            if (!newName.isEmpty()) {
                subTask.setName(newName);
                System.out.println("Наименование изменено");
            }

            if (!newDescription.isEmpty()) {
                subTask.setDescription(newDescription);
                System.out.println("Описание изменено");
            }

            if (!newStatus.isEmpty()) {
                switch (newStatus) {
                    case "1" -> subTask.setStatus(StatusTypes.NEW);
                    case "2" -> subTask.setStatus(StatusTypes.IN_PROGRESS);
                    case "3" -> subTask.setStatus(StatusTypes.DONE);
                    default -> {
                    }
                }
                System.out.println("Статус изменён");
            }
        }
    }

    static void addSubTask(int taskId, int epicId) {
        System.out.println("Введите наименование подзадачи:");
        String name = scanner.nextLine();
        System.out.println("Введите описание подзадачи:");
        String description = scanner.nextLine();

        SubTask subTask = new SubTask(taskManager.getTask(taskId).getEpic(epicId));
        subTask.setName(name);
        subTask.setDescription(description);

        taskManager.getTask(taskId).getEpic(epicId).addSubTask(subTask);
        System.out.println("Подзадача добавлена");
    }

    static void clearSubTasks(int taskId, int epicId) {
        taskManager.getTask(taskId).getEpic(epicId).clearSubTasks();
    }

    static void printSubTasks(int taskId, int epicId) {
        System.out.println("Задача: " + taskManager.getTask(taskId).toString());
        System.out.println("|-> Эпик:" + taskManager.getTask(taskId).getEpic(epicId).toString());

        if (taskManager.getTask(taskId).getEpic(epicId).getSubTasks().isEmpty()) {
            System.out.println("Список пуст");
        } else {
            for (SubTask subTask : taskManager.getTask(taskId).getEpic(epicId).getSubTasks().values()) {
                System.out.println(" |-> Подзадача: " + subTask.toString());
            }
        }
    }

    static void printSubTaskMenu() {
        System.out.println("Управление подзадачами");
        System.out.println("Доступные команды:");
        System.out.println("1 - Вывести список подзадач");
        System.out.println("2 - Очистить список подзадач");
        System.out.println("3 - Добавить новую подзадачу");
        System.out.println("4 - Изменить подзадачу по идентификатору");
        System.out.println("5 - Удалить подзадачу");
        System.out.println("6 - Выход из модуля управления подзадачами");
    }

    static void removeEpic(int id) {
        System.out.println("Введите идентификатор эпика");
        int epicId = 0;

        if (scanner.hasNextInt()) {
            epicId = scanner.nextInt();
        }

        if (taskManager.getTask(id).getEpics().containsKey(epicId)) {
            Epic epic = taskManager.getTask(id).getEpics().get(epicId);
            taskManager.getTask(id).removeEpic(epic);
            System.out.println("Эпик удалён");
        }
    }

    static void updateEpic(int id) {

        System.out.println("Введите идентификатор эпика");
        int epicId = 0;
        if (scanner.hasNextInt()) {
            epicId = scanner.nextInt();
        }

        if (!taskManager.getTask(id).getEpics().containsKey(epicId)) {
            System.out.println("Введён некорректный номер эпика");
        } else {
            Epic epic = taskManager.getTask(id).getEpics().get(epicId);

            System.out.println("Введите новое наименование (пусто - оставить без изменений)");
            String newName = scanner.nextLine();
            System.out.println("Введите новое описание (пусто - оставить без изменений)");
            String newDescription = scanner.nextLine();

            if (!newName.isEmpty()) {
                System.out.println("Наименование изменено");
                epic.setName(newName);
            }

            if (!newDescription.isEmpty()) {
                System.out.println("Описание изменено");
                epic.setDescription(newDescription);
            }
            System.out.println("Эпик изменён");
        }
    }

    static void addEpic(int id) {

        System.out.println("Введите наименование эпика':");
        String name = scanner.nextLine();
        System.out.println("Введите описание эпика:");
        String description = scanner.nextLine();

        Epic epic = new Epic(taskManager.getTask(id));
        epic.setName(name);
        epic.setDescription(description);

        taskManager.getTask(id).addEpic(epic);
        System.out.println("Эпик добавлен");
    }

    static void clearEpics(int id) {
        taskManager.getTask(id).clearEpics();
    }

    static void printEpics(int id) {
        System.out.println("Задача: " + taskManager.getTask(id).toString());

        if (taskManager.getTask(id).getEpics().isEmpty()) {
            System.out.println("Список пуст");
        } else {
            for (Epic epic : taskManager.getTask(id).getEpics().values()) {
                System.out.println("|-> Эпик" + epic.toString());
            }
        }
    }

    static void printEpicMenu() {
        System.out.println("Управление эпиками и подзадачами");
        System.out.println("Доступные команды:");
        System.out.println("1 - Вывести список эпиков");
        System.out.println("2 - Очистить список эпиков");
        System.out.println("3 - Добавить новый эпик");
        System.out.println("4 - Изменить эпик по идентификатору");
        System.out.println("5 - Удалить эпик");
        System.out.println("6 - Управлять подзадачами");
        System.out.println("7 - Выход из модуля управления эпиками");
    }

    static void removeTask() {
        System.out.println("Введите идентификатор задачи");
        int id = 0;
        if (scanner.hasNextInt()) {
            id = scanner.nextInt();
        }

        if (taskManager.getTasks().containsKey(id)) {
            taskManager.removeTask(id);
            System.out.println("Задача удалена");
        } else {
            System.out.println("Задача не найдена");
        }
    }

    static void updateTask() {
        System.out.println("Введите идентификатор задачи");
        int id = 0;
        if (scanner.hasNextInt()) {
            id = scanner.nextInt();
        }

        if (!taskManager.getTasks().containsKey(id)) {
            System.out.println("Введён некорректный номер задачи");
        } else {
            Task task = taskManager.getTask(id);

            System.out.println("Введите новое наименование (пусто - оставить без изменений)");
            String newName = scanner.nextLine();
            System.out.println("Введите новое описание (пусто - оставить без изменений)");
            String newDescription = scanner.nextLine();
            System.out.println(
                    "Выберите новый статус (1 - Новая, 2 - В процессе, 3 - Готово, Пусто - оставить без изменений");
            String newStatus = scanner.nextLine();

            if (!newName.isEmpty()) {
                task.setName(newName);
                System.out.println("Наименование изменено");
            }

            if (!newDescription.isEmpty()) {
                task.setDescription(newDescription);
                System.out.println("Описание изменено");
            }

            if (!newStatus.isEmpty()) {
                switch (newStatus) {
                    case "1" -> task.setStatus(StatusTypes.NEW);
                    case "2" -> task.setStatus(StatusTypes.IN_PROGRESS);
                    case "3" -> task.setStatus(StatusTypes.DONE);
                    default -> {
                    }
                }
                System.out.println("Статус изменён");
            }
        }
    }

    static void addTask() {
        System.out.println("Введите наименование задачи:");
        String name = scanner.nextLine();
        System.out.println("Введите описание задачи:");
        String description = scanner.nextLine();

        Task task = new Task();
        task.setName(name);
        task.setDescription(description);

        taskManager.addTask(task);
        System.out.println("Задача добавлена");
    }


    static void clearTasks() {
        taskManager.clearTasks();
    }

    static void printTasks() {
        if (taskManager.getTasks().isEmpty()) {
            System.out.println("Список пуст");
        } else {
            for (Task task : taskManager.getTasks().values()) {
                System.out.println("Задача: " + task.toString());
            }
        }
    }

    static void printMainMenu() {
        System.out.println("Доступные команды:");
        System.out.println("1 - Вывести список задач");
        System.out.println("2 - Очистить список задач");
        System.out.println("3 - Добавить новую задачу");
        System.out.println("4 - Изменить задачу по идентификатору");
        System.out.println("5 - Удалить задачу");
        System.out.println("6 - Управление эпиками и подзадачами");
        System.out.println("7 - Выход");
    }

    static HashMap<Integer, Task> getTestData() {
        HashMap<Integer, Task> result = new HashMap<>();

        Task firstTask = new Task();
        firstTask.setName("Успешно пройти четвёртый спринт");
        firstTask.setDescription("Изучить теорию и закрепить практикой знания, получаемые в 4-м спринте");

        Epic firstEpic = new Epic(firstTask);
        firstEpic.setName("Изучить теорию спринта");
        firstEpic.setDescription("Изучить теорию и выполнить практику");

        SubTask firstSubTask = new SubTask(firstEpic);
        firstSubTask.setName("Изучить раздел \"ООП.Инкапсуляция\"");
        firstSubTask.setDescription("Изучить теорию и выполнить практику");
        firstEpic.addSubTask(firstSubTask);

        SubTask secondSubTask = new SubTask(firstEpic);
        secondSubTask.setName("Изучить раздел \"ООП.Наследование\"");
        secondSubTask.setDescription("Изучить теорию и выполнить практику");
        firstEpic.addSubTask(secondSubTask);

        SubTask thirdSubTask = new SubTask(firstEpic);
        thirdSubTask.setName("Класс Object и его методы");
        thirdSubTask.setDescription("Изучить теорию и выполнить практику");
        firstEpic.addSubTask(thirdSubTask);

        SubTask fourthSubTask = new SubTask(firstEpic);
        fourthSubTask.setName("Статические поля и методы, константы и перечисления");
        fourthSubTask.setDescription("Изучить теорию и выполнить практику");
        firstEpic.addSubTask(fourthSubTask);

        SubTask fifthSubTask = new SubTask(firstEpic);
        fifthSubTask.setName("Финальный проект спринта №4");
        fifthSubTask.setDescription("Изучить теорию и выполнить практику, сдать финальный проект");
        firstEpic.addSubTask(fifthSubTask);

        firstTask.addEpic(firstEpic);
        result.put(firstTask.getId(), firstTask);

        System.out.println("Тестовые данные загружены");

        return result;
    }
}
