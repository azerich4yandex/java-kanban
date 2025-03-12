package ru.yandex.practicum.scheduler;

import ru.yandex.practicum.scheduler.managers.InMemoryTaskManager;
import ru.yandex.practicum.scheduler.managers.Managers;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;

public class Main {

    private static TaskManager taskManager;

    public static void main(String[] args) {
        HistoryManager historyManager = Managers.getDefaultHistory();
        taskManager = new InMemoryTaskManager(historyManager);

        // Дополнительное задание:
        // 1. Создайте две задачи,
        Task firstTask = new Task("First Task", "First task description");
        int firstTaskId = taskManager.addNewTask(firstTask);

        Task secondTask = new Task("Second Task", "Second task description");
        int secondTaskId = taskManager.addNewTask(secondTask);

        // эпик
        Epic firstEpic = new Epic("First Epic", "First epic description");
        int firstEpicId = taskManager.addNewEpic(firstEpic);

        // с тремя подзадачами
        Subtask firstSubtask = new Subtask("First subtask", "First subtask description", firstEpic);
        int firstSubtaskId = taskManager.addNewSubtask(firstSubtask);
        Subtask secondSubtask = new Subtask("Second subtask", "Second subtask description", firstEpic);
        taskManager.addNewSubtask(secondSubtask);
        Subtask thirdSubtask = new Subtask("Third subtask", "Third subtask description", firstEpic);
        taskManager.addNewSubtask(thirdSubtask);
        taskManager.updateEpic(firstEpic);

        // и эпик без подзадач.
        Epic secondEpic = new Epic("Second Epic", "Second epic description");
        int secondEpicId = taskManager.addNewEpic(secondEpic);

        // 2. Запросите несколько раз созданных задачи в разном порядке
        secondTask = taskManager.getTask(secondTaskId);
        // 3. После каждого запроса выведите историю и убедитесь, что в ней нет повторов
        System.out.println("New state of history (second task id = " + secondTask.getId() + ")");
        printHistory();
        System.out.println("-----");

        firstTask = taskManager.getTask(firstTaskId);
        System.out.println("New state of history (first task id = " + firstTask.getId() + ")");
        printHistory();
        System.out.println("-----");

        secondEpic = taskManager.getEpic(secondEpicId);
        System.out.println("New state of history (second epic id = " + secondEpic.getId() + ")");
        printHistory();
        System.out.println("-----");

        firstSubtask = taskManager.getSubtask(firstSubtaskId);
        System.out.println("New state of history (first subtask id = " + firstSubtask.getId() + ")");
        printHistory();
        System.out.println("-----");

        // 4. Удалите задачу, которая есть в истории,
        taskManager.deleteTask(firstTask.getId());
        // и проверьте, то при печати она не будет выводится
        System.out.println("New state of history (after deleting first task with id " + firstTaskId + ")");
        printHistory();
        System.out.println("-----");

        firstEpic = taskManager.getEpic(firstEpicId);
        System.out.println("New state of history (first epic id = " + firstEpic.getId() + ")");
        printHistory();
        System.out.println("-----");

        // 5. Удалите эпик с тремя подзадачами
        taskManager.deleteEpic(firstEpic.getId());
        System.out.println("New state of history (after deleting first epic with id " + firstEpicId + ")");
        printHistory();
        System.out.println("-----");

        printAll();
    }

    static void printAll() {
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }

        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
            for (Subtask subtask : taskManager.getEpic(epic.getId()).getSubtasks()) {
                System.out.println(subtask);
            }
        }

        printHistory();
    }

    static void printHistory() {
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}
