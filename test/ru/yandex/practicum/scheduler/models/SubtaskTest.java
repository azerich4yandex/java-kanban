package ru.yandex.practicum.scheduler.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.Managers;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubtaskTest {

    private TaskManager taskManager;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeEach
    void createEntities() {
        taskManager = Managers.getDefault();

        Epic epic1 = new Epic("First epic", "First epic description");
        taskManager.createEpic(epic1);

        Epic epic2 = new Epic("Second epic", "Second epic description");
        taskManager.createEpic(epic2);

        subtask1 = new Subtask("First subtask", "First subtask description", LocalDateTime.now(),
                Duration.ofMinutes(30), epic1.getId());
        taskManager.createSubtask(subtask1);
        epic1.addSubtask(subtask1.getId());

        subtask2 = new Subtask("Second subtask", "Second subtask description", subtask1.getEndTime().plusMinutes(1),
                subtask1.getDuration(), epic1.getId());
        taskManager.createSubtask(subtask2);
        epic2.addSubtask(subtask2.getId());

        taskManager.updateEpic(epic1);
        taskManager.updateEpic(epic2);
    }


    @DisplayName("Подзадачи с одинаковым ИД должны совпадать")
    @Test
    void shouldBeEqualsSubtasksWithSameId() {
        Optional<Subtask> subtask2Opt = taskManager.getSubtaskById(subtask1.rowId);

        assertTrue(subtask2Opt.isPresent(), "Подзадача не получена");

        subtask2 = subtask2Opt.get();

        Optional<Subtask> subtask1Opt = taskManager.getSubtaskById(subtask1.getId());

        assertTrue(subtask1Opt.isPresent(), "Подзадача не получена");

        subtask1 = subtask1Opt.get();

        assertEquals(subtask2, subtask1, "Подзадачи c одинаковым Id не совпадают");
    }

    @DisplayName("Не должно быть подзадач без эпика")
    @Test
    void shouldBeNoTasksWithOutEpic() {
        boolean hasEpic = false;
        for (Subtask subtask : taskManager.getSubtasks()) {
            Optional<Epic> epicOpt = taskManager.getEpicById(subtask.getEpicId());
            if (epicOpt.isPresent()) {
                hasEpic = true;
            } else {
                hasEpic = false;
                break;
            }
        }
        assertTrue(hasEpic, "Найдены подзадачи без эпика");
    }

    @DisplayName("Не должно остаться подзадач после удаления всех эпиков")
    @Test
    void shouldBeNoSubtasksAfterEpicDeleting() {
        List<Subtask> expected = new ArrayList<>();

        taskManager.deleteEpics();

        assertEquals(expected, taskManager.getSubtasks(), "Остались подзадачи после удаления эпиков");
    }

    @DisplayName("Статус подзадачи должен изменяться")
    @Test
    void shouldBeInProgressStatus() {
        subtask1.setStatus(StatusTypes.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        Optional<Subtask> receivedSubtask = taskManager.getSubtaskById(subtask1.rowId);

        assertTrue(receivedSubtask.isPresent(), "Подзадача не получена");
        assertEquals(subtask1.getStatus(), receivedSubtask.get().getStatus(), "Статус не изменился после сохранения");
    }

    @DisplayName("После удаления подзадачи должно возвращаться Null при поиске по Id")
    @Test
    void shouldBeNullAfterDeleting() {
        taskManager.deleteSubtask(subtask2.getId());

        Optional<Subtask> subtask2Opt = taskManager.getSubtaskById(subtask2.getId());

        assertFalse(subtask2Opt.isPresent(), "Подзадача получена после удаления");
    }
}