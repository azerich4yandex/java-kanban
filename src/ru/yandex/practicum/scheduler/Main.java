package ru.yandex.practicum.scheduler;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import ru.yandex.practicum.scheduler.managers.Managers;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.Epic;
import ru.yandex.practicum.scheduler.models.Subtask;
import ru.yandex.practicum.scheduler.models.Task;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;

public class Main {

    private static TaskManager taskManager;

    public static void main(String[] args) {
        taskManager = Managers.getDefault();
        doSomeActions();
    }

    private static void doSomeActions() {
        Task firstTask = new Task("Первая задача", "Описание первой задачи");
        int firstTaskId = taskManager.addNewTask(firstTask);

        Task secondTask = new Task("Вторая задача", "Описание второй задачи");
        int secondTaskId = taskManager.addNewTask(secondTask);

        for (Task task : taskManager.getTasks()) {
            System.out.println(task.toString());
        }

        System.out.println(Stream.generate(() -> "-").limit(10).collect(Collectors.joining()));

        firstTask = taskManager.getTask(firstTaskId);
        firstTask.setStatus(StatusTypes.IN_PROGRESS);
        taskManager.updateTask(firstTask);
        secondTask = taskManager.getTask(secondTaskId);
        secondTask.setStatus(StatusTypes.DONE);

        for (Task task : taskManager.getTasks()) {
            System.out.println(task.toString());
        }

        System.out.println(Stream.generate(() -> "=").limit(10).collect(Collectors.joining()));

        Epic firstEpic = new Epic("Первый эпик", "Описание первого эпика");
        int firstEpicId = taskManager.addNewEpic(firstEpic);

        Epic secondEpic = new Epic("Второй эпик", "Описание второго эпика");
        int secondEpicId = taskManager.addNewEpic(secondEpic);

        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic.toString());
        }

        System.out.println(Stream.generate(() -> "-").limit(10).collect(Collectors.joining()));

        firstEpic = taskManager.getEpic(firstEpicId);
        firstEpic.setDescription("Новое описание первого эпика");
        taskManager.updateEpic(firstEpic);
        secondEpic = taskManager.getEpic(secondEpicId);
        secondEpic.setDescription("Новое описание второго эпика");
        taskManager.updateEpic(secondEpic);

        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic.toString());
        }

        System.out.println(Stream.generate(() -> "=").limit(10).collect(Collectors.joining()));

        Subtask firstSubtask = new Subtask("Первая подзадача","Описание первой подзадачи", taskManager.getEpic(firstEpicId));
        int firstSubtaskId = taskManager.addNewSubtask(firstSubtask);
        System.out.println("First subtask id " + firstSubtaskId);

        Subtask secondSubtask = new Subtask("Вторая подзадача", "Описание второй подзадачи", taskManager.getEpic(firstEpicId));
        int secondSubtaskId = taskManager.addNewSubtask(secondSubtask);
        System.out.println("Second subtask id: " + secondSubtaskId);

        System.out.println("From taskManager (way 1):");
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask.toString() + "; Epic: " + subtask.getEpic().getId() + "; size: " + subtask.getEpic().getSubtasks().size());
        }

        System.out.println(Stream.generate(() -> "-").limit(10).collect(Collectors.joining()));

        System.out.println("From taskManager (way 2):");
        for (Subtask subtask : taskManager.getEpicSubtasks(firstEpicId)) {
            System.out.println(subtask.toString());
        }

        firstEpic = taskManager.getEpic(firstEpicId);
        System.out.println("From epic: " + firstEpic.toString());
        for (Subtask subtask : firstEpic.getSubtasks()) {
            System.out.println(subtask.toString());
        }

        System.out.println(Stream.generate(() -> "-").limit(10).collect(Collectors.joining()));

        secondEpic = taskManager.getEpic(secondEpicId);
        Subtask thirdSubtask = new Subtask("Третья подзадача", "Описание третьей подзадачи", secondEpic);
        int thirdSubtaskId = taskManager.addNewSubtask(thirdSubtask);

        Subtask fourthSubtask = new Subtask("Четвертая подзадача", "Описание четвертой подзадачи", secondEpic);
        int fourthSubtaskId = taskManager.addNewSubtask(fourthSubtask);

        System.out.println(secondEpic.toString());

        thirdSubtask = taskManager.getSubtask(thirdSubtaskId);
        thirdSubtask.setStatus(StatusTypes.IN_PROGRESS);
        taskManager.updateSubtask(thirdSubtask);

        secondEpic.calculateStatus();
        System.out.println(secondEpic + "; size: " + secondEpic.getSubtasks().size());

        thirdSubtask = taskManager.getSubtask(thirdSubtaskId);
        thirdSubtask.setStatus(StatusTypes.DONE);
        taskManager.updateSubtask(thirdSubtask);

        fourthSubtask = taskManager.getSubtask(fourthSubtaskId);
        fourthSubtask.setStatus(StatusTypes.DONE);
        taskManager.updateSubtask(fourthSubtask);

        secondEpic.calculateStatus();
        System.out.println(secondEpic + "; size: " + secondEpic.getSubtasks().size());

        taskManager.deleteSubtask(thirdSubtaskId);
        taskManager.deleteSubtask(fourthSubtaskId);

        secondEpic.calculateStatus();
        System.out.println(secondEpic + "; size: " + secondEpic.getSubtasks().size());

        System.out.println(firstEpic + "; size: " + firstEpic.getSubtasks().size());
        taskManager.deleteSubtasks();
        System.out.println(firstEpic + "; size: " + firstEpic.getSubtasks().size());

        System.out.println(taskManager.getEpics().size());
        taskManager.deleteEpic(firstEpicId);
        System.out.println(taskManager.getEpics().size());
        taskManager.deleteEpics();
        System.out.println(taskManager.getEpics().size());

        System.out.println(taskManager.getTasks().size());
        taskManager.deleteTask(firstTaskId);
        System.out.println(taskManager.getTasks().size());
        taskManager.deleteTasks();
        System.out.println(taskManager.getTasks().size());

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

        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}
