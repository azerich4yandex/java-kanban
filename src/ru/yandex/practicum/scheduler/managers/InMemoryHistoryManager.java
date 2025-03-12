package ru.yandex.practicum.scheduler.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.yandex.practicum.scheduler.managers.interfaces.HistoryManager;
import ru.yandex.practicum.scheduler.models.Task;

public class InMemoryHistoryManager implements HistoryManager {

    private final HistoricalLinkedList<Task> history = new HistoricalLinkedList<>();

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    @Override
    public void addToHistory(Task task) {
        history.linkLast(task);
    }

    @Override
    public void remove(int id) {
        history.remove(id);
    }

    static class Node<T> {

        public T element;
        public Node<T> previous;
        public Node<T> next;

        public Node(Node<T> previous, T element, Node<T> next) {
            this.element = element;
            this.previous = previous;
            this.next = next;
        }
    }

    static class HistoricalLinkedList<T extends Task> {

        private final Map<Integer, T> history = new HashMap<>();
        private Node<T> head;
        private Node<T> tail;
        private int size = 0;

        public void linkLast(T task) {
            if (task != null) {
                // Если коллекция содержит задачу
                if (history.containsValue(task)) {
                    // Перебираем все узлы
                    for (Node<T> node = tail; node != null; node = node.previous) {
                        if (task.equals(node.element)) {
                            // Удаляем узел
                            removeNode(node);
                        }
                    }
                    // Удаляем элемент коллекции
                    history.remove(task.getId());
                }

                // Добавляем элемент в коллекцию
                history.put(task.getId(), task);

                // Получаем ссылки последнего узла
                Node<T> newPrevious = tail; // хвост - это новый предыдущий элемент
                Node<T> newNode = new Node<>(newPrevious, task, null); // создаём новый элемент

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


                // Увеличим размер
                size++;
            }
        }

        private void removeNode(Node<T> node) {
            // Сохраняем значение предыдущего и следующего элемента
            Node<T> previous = node.previous;
            Node<T> next = node.next;

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

            node.element = null; // Очистим значение узла
            size--; // Уменьшим размер
        }

        public void remove(int id) {
            // получим экземпляр по Id
            T task = history.get(id);

            // Если экземпляр не найден
            if (task == null) {
                // Пробегаемся по всем узлам с головы
                for (Node<T> node = head; node != null; node = node.next) {
                    // Находим пустые узлы
                    if (node.element == null) {
                        // Удаляем их
                        removeNode(node);
                    }
                }
            } else { // Иначе
                // Пробегаемся по всем узлам с хвоста
                for (Node<T> node = tail; node != null; node = node.previous) {
                    // Находим такие же узлы
                    if (task.equals(node.element)) {
                        // Удаляем их
                        removeNode(node);
                    }
                }
                history.remove(task.getId()); // Удаляем задачу из истории
            }
        }

        public ArrayList<T> getTasks() {
            // Создаём результирующую коллекцию
            ArrayList<T> result = new ArrayList<>(size);

            // Пробегаемся по всем узлам с хвоста
            for (Node<T> node = tail; node != null; node = node.previous) {
                // Добавляем элемент в результирующую коллекцию
                result.add(node.element);
            }

            // Возвращаем результат
            return result;
        }

        public Node<T> getHead() {
            return head;
        }

        public Node<T> getTail() {
            return tail;
        }
    }
}
