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

    private static TaskManager TaskManager;

    public static void main(String[] args) {
        TaskManager = Managers.getDefault();
        doSomeActions();
    }

    private static void doSomeActions() {
        Task firstTask = new Task("Первая задача", "Описание первой задачи");
        int firstTaskId = TaskManager.addNewTask(firstTask);

        Task secondTask = new Task("Вторая задача", "Описание второй задачи");
        int secondTaskId = TaskManager.addNewTask(secondTask);

        for (Task task : TaskManager.getTasks()) {
            System.out.println(task.toString());
        }

        System.out.println(Stream.generate(() -> "-").limit(10).collect(Collectors.joining()));

        firstTask = TaskManager.getTask(firstTaskId);
        firstTask.setStatus(StatusTypes.IN_PROGRESS);
        TaskManager.updateTask(firstTask);
        secondTask = TaskManager.getTask(secondTaskId);
        secondTask.setStatus(StatusTypes.DONE);

        for (Task task : TaskManager.getTasks()) {
            System.out.println(task.toString());
        }

        System.out.println(Stream.generate(() -> "=").limit(10).collect(Collectors.joining()));

        Epic firstEpic = new Epic("Первый эпик", "Описание первого эпика");
        int firstEpicId = TaskManager.addNewEpic(firstEpic);

        Epic secondEpic = new Epic("Второй эпик", "Описание второго эпика");
        int secondEpicId = TaskManager.addNewEpic(secondEpic);

        for (Epic epic : TaskManager.getEpics()) {
            System.out.println(epic.toString());
        }

        System.out.println(Stream.generate(() -> "-").limit(10).collect(Collectors.joining()));

        firstEpic = TaskManager.getEpic(firstEpicId);
        firstEpic.setDescription("Новое описание первого эпика");
        TaskManager.updateEpic(firstEpic);
        secondEpic = TaskManager.getEpic(secondEpicId);
        secondEpic.setDescription("Новое описание второго эпика");
        TaskManager.updateEpic(secondEpic);

        for (Epic epic : TaskManager.getEpics()) {
            System.out.println(epic.toString());
        }

        System.out.println(Stream.generate(() -> "=").limit(10).collect(Collectors.joining()));

        Subtask firstSubtask = new Subtask("Первая подзадача","Описание первой подзадачи", TaskManager.getEpic(firstEpicId));
        int firstSubtaskId = TaskManager.addNewSubtask(firstSubtask);
        System.out.println("First subtask id " + firstSubtaskId);

        Subtask secondSubtask = new Subtask("Вторая подзадача", "Описание второй подзадачи", TaskManager.getEpic(firstEpicId));
        int secondSubtaskId = TaskManager.addNewSubtask(secondSubtask);
        System.out.println("Second subtask id: " + secondSubtaskId);

        System.out.println("From TaskManager (way 1):");
        for (Subtask subtask : TaskManager.getSubtasks()) {
            System.out.println(subtask.toString() + "; Epic: " + subtask.getEpic().getId() + "; size: " + subtask.getEpic().getSubtasks().size());
        }

        System.out.println(Stream.generate(() -> "-").limit(10).collect(Collectors.joining()));

        System.out.println("From TaskManager (way 2):");
        for (Subtask subtask : TaskManager.getEpicSubtasks(firstEpicId)) {
            System.out.println(subtask.toString());
        }

        firstEpic = TaskManager.getEpic(firstEpicId);
        System.out.println("From epic: " + firstEpic.toString());
        for (Subtask subtask : firstEpic.getSubtasks()) {
            System.out.println(subtask.toString());
        }

        System.out.println(Stream.generate(() -> "-").limit(10).collect(Collectors.joining()));

        secondEpic = TaskManager.getEpic(secondEpicId);
        Subtask thirdSubtask = new Subtask("Третья подзадача", "Описание третьей подзадачи", secondEpic);
        int thirdSubtaskId = TaskManager.addNewSubtask(thirdSubtask);

        Subtask fourthSubtask = new Subtask("Четвертая подзадача", "Описание четвертой подзадачи", secondEpic);
        int fourthSubtaskId = TaskManager.addNewSubtask(fourthSubtask);

        System.out.println(secondEpic.toString());

        thirdSubtask = TaskManager.getSubtask(thirdSubtaskId);
        thirdSubtask.setStatus(StatusTypes.IN_PROGRESS);
        TaskManager.updateSubtask(thirdSubtask);

        secondEpic.calculateStatus();
        System.out.println(secondEpic + "; size: " + secondEpic.getSubtasks().size());

        thirdSubtask = TaskManager.getSubtask(thirdSubtaskId);
        thirdSubtask.setStatus(StatusTypes.DONE);
        TaskManager.updateSubtask(thirdSubtask);

        fourthSubtask = TaskManager.getSubtask(fourthSubtaskId);
        fourthSubtask.setStatus(StatusTypes.DONE);
        TaskManager.updateSubtask(fourthSubtask);

        secondEpic.calculateStatus();
        System.out.println(secondEpic + "; size: " + secondEpic.getSubtasks().size());

        TaskManager.deleteSubtask(thirdSubtaskId);
        TaskManager.deleteSubtask(fourthSubtaskId);

        secondEpic.calculateStatus();
        System.out.println(secondEpic + "; size: " + secondEpic.getSubtasks().size());

        System.out.println(firstEpic + "; size: " + firstEpic.getSubtasks().size());
        TaskManager.deleteSubtasks();
        System.out.println(firstEpic + "; size: " + firstEpic.getSubtasks().size());

        System.out.println(TaskManager.getEpics().size());
        TaskManager.deleteEpic(firstEpicId);
        System.out.println(TaskManager.getEpics().size());
        TaskManager.deleteEpics();
        System.out.println(TaskManager.getEpics().size());

        System.out.println(TaskManager.getTasks().size());
        TaskManager.deleteTask(firstTaskId);
        System.out.println(TaskManager.getTasks().size());
        TaskManager.deleteTasks();
        System.out.println(TaskManager.getTasks().size());
    }
}
