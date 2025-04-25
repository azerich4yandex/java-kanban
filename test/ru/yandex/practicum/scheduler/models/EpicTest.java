package ru.yandex.practicum.scheduler.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.Managers;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicTest {

    private TaskManager taskManager;
    private Epic epic1;
    private Epic epic2;
    private Subtask subtask1;
    private Subtask subtask2;
    private Subtask subtask3;


    @BeforeEach
    void createEntities() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(5);

        taskManager = Managers.getDefault();

        epic1 = new Epic("First epic", "First epic description");
        taskManager.createEpic(epic1);

        epic2 = new Epic("Second epic", "Second epic description");
        taskManager.createEpic(epic2);

        subtask1 = new Subtask("First subtask", "First subtask description", startTime, duration, epic1.getId());
        taskManager.createSubtask(subtask1);
        epic1.addSubtask(subtask1.getId());

        startTime = startTime.plus(duration.plusMinutes(2));
        subtask2 = new Subtask("Second subtask", "Second subtask description", startTime, duration, epic1.getId());

        taskManager.createSubtask(subtask2);
        epic1.addSubtask(subtask2.getId());

        startTime = startTime.plus(duration.plusMinutes(2));
        subtask3 = new Subtask("Third subtask", "Third subtask description", startTime, duration, epic1.getId());
        taskManager.createSubtask(subtask3);
        epic1.addSubtask(subtask3.getId());

    }

    @AfterEach
    void halt() {
        taskManager.deleteTasks();
        taskManager.deleteSubtasks();
        taskManager.deleteEpics();
    }

    @DisplayName("Статус пустого эпика должен быть NEW")
    @Test
    void shouldNewStatusWithOutSubtasks() {
        StatusTypes expected = StatusTypes.NEW;
        Optional<Epic> epicOpt = taskManager.getEpicById(epic2.getId());

        assertTrue(epicOpt.isPresent());

        assertEquals(expected, epicOpt.get().getStatus(), "Неправильный статус Эпика");
    }

    @DisplayName("Статус непустого эпика должен быть NEW, если все его задачи NEW")
    @Test
    void shouldNewStatusWithNewSubtasks() {
        StatusTypes expected = StatusTypes.NEW;

        Optional<Epic> receivedEpic = taskManager.getEpicById(epic1.getId());

        assertTrue(receivedEpic.isPresent(), "Эпик не получен");
        assertEquals(expected, receivedEpic.get().getStatus(), "Неправильный статус Эпика");
    }

    @DisplayName("Статус непустого эпика должен быть IN_PROGRESS, если статус хотя бы одной его задачи не NEW")
    @Test
    void shouldInProgressStatusWithAtLeastOneNotNewSubtask() {
        StatusTypes expected = StatusTypes.IN_PROGRESS;

        subtask1.setStatus(StatusTypes.DONE);
        taskManager.updateSubtask(subtask1);

        Optional<Epic> receivedEpic = taskManager.getEpicById(epic1.getId());

        assertTrue(receivedEpic.isPresent(), "Эпик не получен");
        assertEquals(expected, receivedEpic.get().getStatus(), "Неправильный статус Эпика");
    }

    @DisplayName("Статус непустого эпика должен быть IN_PROGRESS, если все его задачи IN_PROGRESS")
    @Test
    void shouldInProgressStatusWithAllInProgressSubtask() {
        StatusTypes expected = StatusTypes.IN_PROGRESS;
        subtask1.setStatus(StatusTypes.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        subtask2.setStatus(StatusTypes.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);
        subtask3.setStatus(StatusTypes.IN_PROGRESS);
        taskManager.updateSubtask(subtask3);

        Optional<Epic> receivedEpic = taskManager.getEpicById(epic1.getId());

        assertTrue(receivedEpic.isPresent(), "Эпик не получен");
        assertEquals(expected, receivedEpic.get().getStatus(), "Неправильный статус Эпика");
    }

    @DisplayName("Статус непустого эпика должен быть DONE, если статус всех его задач DONE")
    @Test
    void shouldDoneStatusWithAllDoneSubtask() {
        StatusTypes expected = StatusTypes.DONE;

        subtask1.setStatus(StatusTypes.DONE);
        taskManager.updateSubtask(subtask1);
        subtask2.setStatus(StatusTypes.DONE);
        taskManager.updateSubtask(subtask2);
        subtask3.setStatus(StatusTypes.DONE);
        taskManager.updateSubtask(subtask3);

        Optional<Epic> receivedEpic = taskManager.getEpicById(epic1.getId());

        assertTrue(receivedEpic.isPresent(), "Эпик не получен");
        assertEquals(expected, receivedEpic.get().getStatus(), "Неправильный статус Эпика");
    }


    @DisplayName("Созданный и сохранённый списки подзадач эпика должны совпадать")
    @Test
    void getSubtasks() {
        List<Integer> expected = new ArrayList<>();

        expected.add(subtask1.getId());
        expected.add(subtask2.getId());
        expected.add(subtask3.getId());

        Optional<Epic> receivedEpic = taskManager.getEpicById(epic1.getId());

        assertTrue(receivedEpic.isPresent(), "Эпик не получен");
        assertEquals(expected, receivedEpic.get().getSubtaskIds(), "Списки подзадач не совпадают");
    }

    @DisplayName("Созданная и сохранённая подзадачи в эпике должны совпадать")
    @Test
    void addSubtask() {
        Subtask subtask = new Subtask("New subtask", "New subtask description", subtask3.getEndTime().plusMinutes(1),
                subtask3.getDuration(), epic2.getId());
        taskManager.createSubtask(subtask);

        epic2.addSubtask(subtask.getId());
        taskManager.updateEpic(epic2);

        Optional<Epic> receivedEpicOpt = taskManager.getEpicById(epic2.getId());

        assertTrue(receivedEpicOpt.isPresent(), "Эпик не получен");

        Epic receivedEpic = receivedEpicOpt.get();

        assertEquals(subtask.getId(), receivedEpic.getSubtaskIds().getFirst(),
                "Ожидаемая и добавленная подзадачи на совпадают");
    }

    @DisplayName("Описание подзадачи эпика должно сохраняться после сохранения эпика")
    @Test
    void updateSubtask() {
        String expected = "Modified subtask description";
        subtask2.setDescription(expected);
        epic2.updateSubtask(subtask2.getId());
        taskManager.updateEpic(epic2);

        Optional<Subtask> receivedSubtask = taskManager.getSubtaskById(subtask2.getId());

        assertTrue(receivedSubtask.isPresent(), "Подзадача не получена");
        assertEquals(expected, receivedSubtask.get().getDescription(), "Описание не изменилось");
    }

    @DisplayName("После удаление подзадачи количество подзадач в Эпике должно измениться")
    @Test
    void deleteSubtask() {
        List<Subtask> expected = new ArrayList<>();
        expected.add(subtask2);
        expected.add(subtask3);

        epic1.deleteSubtask(subtask1);
        taskManager.deleteSubtask(subtask1.getId());
        taskManager.updateEpic(epic1);
        List<Subtask> received = taskManager.getEpicSubtasks(epic1.getId());

        assertEquals(expected, received, "Списки не совпадают");
    }

    @DisplayName("Количество задач эпика не должно измениться после удаление некорректной подзадачи")
    @Test
    void shouldBeEmptyAfterDeletingWrongSubtask() {
        List<Integer> expected = new ArrayList<>();

        epic2.deleteSubtask(subtask1);
        Optional<Epic> receivedEpic = taskManager.getEpicById(epic2.getId());

        assertTrue(receivedEpic.isPresent(), "Эпик не получен");
        assertEquals(expected, receivedEpic.get().getSubtaskIds(), "Списки не совпадают");
    }

    @DisplayName("Эпики с одинаковым ИД должны совпадать")
    @Test
    void shouldBeEqualsWithSameId() {
        Optional<Epic> epic1Opt = taskManager.getEpicById(epic1.getId());

        assertTrue(epic1Opt.isPresent(), "Эпик не получен");

        epic2 = epic1Opt.get();
        epic2.setName("Modified name");

        assertEquals(epic1, epic2, "Эпики не совпадают");
    }

    @DisplayName("Статусы эпиков должны стать NEW после удаления всех подзадач")
    @Test
    void shouldBeNewStatusAfterAllSubtaskDeleting() {
        boolean isNew = false;
        taskManager.deleteSubtasks();

        for (Epic epic : taskManager.getEpics()) {

            if (epic.getStatus() != StatusTypes.NEW) {
                isNew = false;
                break;
            } else {
                isNew = true;
            }
        }

        assertTrue(isNew, "Эпики не сменили свой статус после удаления всех подзадач");
    }
}