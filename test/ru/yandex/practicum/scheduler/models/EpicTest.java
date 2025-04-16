package ru.yandex.practicum.scheduler.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        taskManager = Managers.getDefault();

        epic1 = new Epic("First epic", "First epic description");
        epic2 = new Epic("Second epic", "Second epic description");
        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(5);

        subtask1 = new Subtask("First subtask", "First subtask description", epic1);
        subtask1.setStartTime(startTime);
        subtask1.setDuration(duration);
        subtask2 = new Subtask("Second subtask", "Second subtask description", epic1);
        startTime = startTime.plus(duration.plusMinutes(2));
        subtask2.setStartTime(startTime);
        subtask2.setDuration(duration);
        subtask3 = new Subtask("Third subtask", "Third subtask description", epic1);
        startTime = startTime.plus(duration.plusMinutes(2));
        subtask3.setStartTime(startTime);
        subtask3.setDuration(duration);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        taskManager.addNewSubtask(subtask3);
    }

    @DisplayName("Статус пустого эпика должен быть NEW")
    @Test
    void shouldNewStatusWithOutSubtasks() {
        StatusTypes expected = StatusTypes.NEW;

        assertEquals(expected, taskManager.getEpic(epic2.getId()).getStatus(), "Неправильный статус Эпика");
    }

    @DisplayName("Статус непустого эпика должен быть NEW, если все его задачи NEW")
    @Test
    void shouldNewStatusWithNewSubtasks() {
        StatusTypes expected = StatusTypes.NEW;

        assertEquals(expected, taskManager.getEpic(epic1.getId()).getStatus(), "Неправильный статус Эпика");
    }

    @DisplayName("Статус непустого эпика должен быть IN_PROGRESS, если статус хотя бы одной его задачи не NEW")
    @Test
    void shouldInProgressStatusWithAtLeastOneNotNewSubtask() {
        StatusTypes expected = StatusTypes.IN_PROGRESS;

        subtask1.setStatus(StatusTypes.DONE);
        taskManager.updateSubtask(subtask1);
        // Пересчитаем поля эпика
        epic1.calculateFields();

        assertEquals(expected, taskManager.getEpic(epic1.getId()).getStatus(), "Неправильный статус Эпика");
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

        assertEquals(expected, taskManager.getEpic(epic1.getId()).getStatus(), "Неправильный статус Эпика");
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

        assertEquals(expected, taskManager.getEpic(epic1.getId()).getStatus(), "Неправильный статус Эпика");
    }


    @DisplayName("Созданный и сохранённый списки подзадач эпика должны совпадать")
    @Test
    void getSubtasks() {
        List<Subtask> expected = new ArrayList<>();

        expected.add(subtask1);
        expected.add(subtask2);
        expected.add(subtask3);

        assertEquals(expected, taskManager.getEpic(epic1.getId()).getSubtasks(), "Списки подзадач не совпадают");
    }

    @DisplayName("Созданная и сохранённая подзадачи в эпике должны совпадать")
    @Test
    void addNewSubtask() {
        Subtask subtask = new Subtask("New subtask", "New subtask description", epic2);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofMinutes(5));
        epic2.addNewSubtask(subtask);
        taskManager.addNewSubtask(subtask);

        assertEquals(subtask, taskManager.getEpic(epic2.getId()).getSubtasks().getFirst(),
                "Ожидаемая и добавленная подзадачи на совпадают");
    }

    @DisplayName("Описание подзадачи эпика должно сохраняться после сохранения эпика")
    @Test
    void updateSubtask() {
        String expected = "Modified subtask description";
        subtask2.setDescription(expected);
        epic2.updateSubtask(subtask2);
        taskManager.updateEpic(epic2);

        assertEquals(expected, taskManager.getSubtask(subtask2.getId()).getDescription(), "Описание не изменилось");
    }

    @DisplayName("После удаление подзадачи количество подзадач в Эпике должно измениться")
    @Test
    void deleteSubtask() {
        List<Subtask> expected = new ArrayList<>();
        expected.add(subtask2);
        expected.add(subtask3);

        epic1.deleteSubtask(subtask1);
        taskManager.updateEpic(epic2);

        assertEquals(expected, taskManager.getEpic(epic1.getId()).getSubtasks(), "Списки не совпадают");
    }

    @DisplayName("Количество задач эпика не должно измениться после удаление некорректной подзадачи")
    @Test
    void shouldBeEmptyAfterDeletingWrongSubtask() {
        List<Subtask> expected = new ArrayList<>();

        epic2.deleteSubtask(subtask1);

        assertEquals(expected, taskManager.getEpic(epic2.getId()).getSubtasks(), "Списки не совпадают");
    }

    @DisplayName("Эпики с одинаковым ИД должны совпадать")
    @Test
    void shouldBeEqualsWithSameId() {
        epic2 = taskManager.getEpic(epic1.getId());
        epic2.setName("Modified name");

        assertEquals(epic2, taskManager.getEpic(epic1.getId()), "Эпики не совпадают");
    }

    @DisplayName("Статусы эпиков должны стать NEW после удаления всех подзадач")
    @Test
    void shouldBeNewStatusAfterAllSubtaskDeleting() {
        boolean isNew = false;
        taskManager.deleteSubtasks();

        for (Epic epic : taskManager.getEpics()) {
            // Пересчитаем поля эпика
            epic.calculateFields();

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