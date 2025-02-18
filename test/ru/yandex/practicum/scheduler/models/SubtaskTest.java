package ru.yandex.practicum.scheduler.models;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.scheduler.managers.Managers;
import ru.yandex.practicum.scheduler.managers.interfaces.TaskManager;
import ru.yandex.practicum.scheduler.models.enums.StatusTypes;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private static TaskManager taskManager;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeAll
    static void init() {
        taskManager = Managers.getDefault();
    }

    @BeforeEach
    void createEntities() {
        Epic epic1 = new Epic("First epic", "First epic description");
        Epic epic2 = new Epic("Second epic", "Second epic description");
        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        subtask1 = new Subtask("First subtask", "First subtask description", epic1);
        subtask2 = new Subtask("Second subtask", "Second subtask description", epic1);
        Subtask subtask3 = new Subtask("Third subtask", "Third subtask description", epic1);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        taskManager.addNewSubtask(subtask3);
    }

    @AfterEach
    void clearEntities() {
        taskManager.deleteEpics();
    }


    @DisplayName("Подзадачи с одинаковым ИД должны совпадать")
    @Test
    void shouldBeEqualsSubtasksWithSameId() {
        subtask1.setDescription("New description");

        assertEquals(subtask1, taskManager.getSubtask(subtask1.getId()), "Подзадачи не совпадают");
    }

    @DisplayName("Не должно быть подзадач без эпика")
    @Test
    void shouldBeNoTasksWithOutEpic() {
        boolean hasEpic = false;
        for (Subtask subtask : taskManager.getSubtasks()) {
            Epic epic = subtask.getEpic();
            if (taskManager.getEpic(epic.id) == null) {
                hasEpic = false;
                break;
            } else {
                hasEpic = true;
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

        assertEquals(subtask1.getStatus(), taskManager.getSubtask(subtask1.id).getStatus(), "Статус не изменился после сохранения");
    }

    @DisplayName("После удаления подзадачи должно возвращаться Null при поиске по Id")
    @Test
    void shouldBeNullAfterDeleting() {
        taskManager.deleteSubtask(subtask2.getId());

        assertNull(taskManager.getSubtask(subtask2.getId()));
    }
}