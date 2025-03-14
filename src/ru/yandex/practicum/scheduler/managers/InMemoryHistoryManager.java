package ru.yandex.practicum.scheduler.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.models.Task;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> history = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void addToHistory(Task task) {
        // Добавляем в конец истории
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        // получим экземпляр узла по Id
        Node node = history.get(id);

        // Если узел существует
        if (node != null) {
            // Удаляем задачу из истории
            history.remove(node.task.getId());
            // Удаляем узел
            removeNode(node);
        }
    }

    private void linkLast(Task task) {
        if (task != null) {
            // Если в истории такой узел уже есть
            if (history.containsKey(task.getId())) {
                // Находим его
                Node node = history.get(task.getId());
                // Удаляем узел их хранилища
                history.remove(task.getId());
                // Удаляем узел
                removeNode(node);
            }

            // Получаем ссылки последнего узла
            Node newPrevious = tail; // хвост - это новый предыдущий элемент
            Node newNode = new Node(newPrevious, task, null); // создаём новый элемент

            // Добавляем узел в коллекцию
            history.put(task.getId(), newNode);

            tail = newNode; // новый хвост - новый созданный элемент

            // Если предыдущий узел не пуст
            if (newPrevious != null) {
                newPrevious.next = newNode; // иначе следующий узел у предыдущего элемента - новый узел
            }

            // Если первый узел пуст
            if (head == null) {
                // Значит это первое добавление
                // и первым элементом должен быть первый добавленный
                head = newNode;
            }
        }
    }

    private void removeNode(Node node) {
        // Сохраняем значение предыдущего и следующего элемента
        Node previous = node.previous;
        Node next = node.next;

        // Если предыдущий элемент пуст,
        // значит удаляемый элемент - первый
        if (previous == null) {
            // Поэтому первым элементом коллекции
            // становится следующий элемент
            head = next;
        } else {
            // Иначе удаляемый элемент не является первым.
            // Присваиваем значение следующего элемента предыдущему
            previous.next = next;

            // У указанного элемента следующим элементом будет пусто
            node.previous = null;
        }

        // Если следующий узел пуст,
        // значит удаляемый элемент - последний
        if (next == null) {
            tail = previous; // хвостом станет предыдущий элемент
        } else {
            // Иначе предыдущий узел следующего узла
            // станет сохранённым предыдущим
            next.previous = previous;
            // А следующий элемент нового узла станет пустым
            node.next = null;
        }

        node.task = null; // Очистим значение узла
    }

    private ArrayList<Task> getTasks() {
        // Создаём результирующую коллекцию
        ArrayList<Task> result = new ArrayList<>();

        // Пробегаемся по всем узлам с головы
        Node node = head;
        while (node != null) {
            result.add(node.task);
            node = node.next;
        }

        // Возвращаем результат
        return result;
    }

    static class Node {

        public Task task;
        public Node previous;
        public Node next;

        public Node(Node previous, Task task, Node next) {
            this.task = task;
            this.previous = previous;
            this.next = next;
        }
    }
}
