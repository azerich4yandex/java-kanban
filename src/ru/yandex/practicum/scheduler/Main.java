package ru.yandex.practicum.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
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
        Task firstTask = new Task("First Task", "First task description", LocalDateTime.now(), Duration.ofMinutes(30));
        int firstTaskId = taskManager.createTask(firstTask);

        Task secondTask = new Task("Second Task", "Second task description", firstTask.getEndTime().plusMinutes(1),
                firstTask.getDuration());
        int secondTaskId = taskManager.createTask(secondTask);

        // эпик
        Epic firstEpic = new Epic("First Epic", "First epic description");
        int firstEpicId = taskManager.createEpic(firstEpic);

        // с тремя подзадачами
        Subtask firstSubtask = new Subtask("First subtask", "First subtask description",
                secondTask.getEndTime().plusMinutes(1), secondTask.getDuration(), firstEpic.getId());
        int firstSubtaskId = taskManager.createSubtask(firstSubtask);
        Subtask secondSubtask = new Subtask("Second subtask", "Second subtask description",
                firstSubtask.getEndTime().plusMinutes(1), firstSubtask.getDuration(), firstEpic.getId());
        taskManager.createSubtask(secondSubtask);
        Subtask thirdSubtask = new Subtask("Third subtask", "Third subtask description",
                secondSubtask.getEndTime().plusMinutes(1), secondSubtask.getDuration(), firstEpic.getId());
        taskManager.createSubtask(thirdSubtask);
        taskManager.updateEpic(firstEpic);

        // и эпик без подзадач.
        Epic secondEpic = new Epic("Second Epic", "Second epic description");
        int secondEpicId = taskManager.createEpic(secondEpic);

        // 2. Запросите несколько раз созданных задачи в разном порядке
        Optional<Task> secondTaskOpt = taskManager.getTaskById(secondTaskId);
        if (secondTaskOpt.isPresent()) {
            secondTask = secondTaskOpt.get();
        }
        // 3. После каждого запроса выведите историю и убедитесь, что в ней нет повторов
        System.out.println("New state of history (second task rowId = " + secondTask.getId() + ")");
        printHistory();
        System.out.println("-----");

        Optional<Task> firstTaskOpt = taskManager.getTaskById(firstTaskId);
        if (firstTaskOpt.isPresent()) {
            firstTask = firstTaskOpt.get();
        }

        System.out.println("New state of history (first task rowId = " + firstTask.getId() + ")");
        printHistory();
        System.out.println("-----");

        Optional<Epic> secondEpicOpt = taskManager.getEpicById(secondEpicId);
        if (secondEpicOpt.isPresent()) {
            secondEpic = secondEpicOpt.get();
        }
        System.out.println("New state of history (second epic rowId = " + secondEpic.getId() + ")");
        printHistory();
        System.out.println("-----");

        Optional<Subtask> firstSubtaskOpt = taskManager.getSubtaskById(firstSubtaskId);
        if (firstSubtaskOpt.isPresent()) {
            firstSubtask = firstSubtaskOpt.get();
        }
        System.out.println("New state of history (first subtask rowId = " + firstSubtask.getId() + ")");
        printHistory();
        System.out.println("-----");

        // 4. Удалите задачу, которая есть в истории,
        taskManager.deleteTask(firstTask.getId());
        // и проверьте, то при печати она не будет выводится
        System.out.println("New state of history (after deleting first task with rowId " + firstTaskId + ")");
        printHistory();
        System.out.println("-----");

        Optional<Epic> firstEpicOpt = taskManager.getEpicById(firstEpicId);
        if (firstEpicOpt.isPresent()) {
            firstEpic = firstEpicOpt.get();
        }
        System.out.println("New state of history (first epic rowId = " + firstEpic.getId() + ")");
        printHistory();
        System.out.println("-----");

        // 5. Удалите эпик с тремя подзадачами
        taskManager.deleteEpic(firstEpic.getId());
        System.out.println("New state of history (after deleting first epic with rowId " + firstEpicId + ")");
        printHistory();
        System.out.println("-----");

        printAll();
    }

    static void printAll() {
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("Хранилище:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
            for (Subtask subtask : taskManager.getEpicSubtasks(epic.getId())) {
                System.out.println(subtask);
            }
        }

        System.out.println("История:");
        printHistory();
    }

    static void printHistory() {
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}
