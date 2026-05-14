package server.manager;

import common.model.HumanBeing;
import common.model.Mood;
import server.database.HumanBeingDAO;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Менеджер коллекции объектов HumanBeing на сервере.
 *
 * <p>Обеспечивает хранение, управление и операции над коллекцией объектов
 * HumanBeing. Коллекция реализована как {@link ArrayDeque} для эффективного
 * доступа к первому и последнему элементам.</p>
 *
 * <p>Все операции над коллекцией синхронизированы с помощью {@link ReentrantReadWriteLock}
 * для потокобезопасного доступа.</p>
 *
 * @author Полина
 * @version 2.0
 * @since 2026-05-14
 */
public class CollectionManager {

    private final Deque<HumanBeing> collection = new ArrayDeque<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    /**
     * Загружает все объекты из базы данных в коллекцию в памяти.
     * Вызывается при старте сервера.
     */
    public void loadFromDatabase() {
        lock.writeLock().lock();
        try {
            collection.clear();
            collection.addAll(HumanBeingDAO.loadAll());
            System.out.println("Загружено " + collection.size() + " объектов из БД");
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Добавляет новый объект в коллекцию.
     *
     * @param human объект для добавления
     * @param ownerUsername имя владельца
     * @return true если добавление успешно, false иначе
     */
    public boolean add(HumanBeing human, String ownerUsername) {
        boolean saved = HumanBeingDAO.save(human, ownerUsername);
        if (!saved) return false;

        lock.writeLock().lock();
        try {
            collection.add(human);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Обновляет существующий объект в коллекции.
     *
     * @param human объект с новыми данными
     * @param ownerUsername имя владельца
     * @return true если обновление успешно, false иначе
     */
    public boolean update(HumanBeing human, String ownerUsername) {
        if (!HumanBeingDAO.existsAndOwnedBy(human.getId(), ownerUsername)) {
            return false;
        }

        boolean updated = HumanBeingDAO.update(human, ownerUsername);
        if (!updated) return false;

        lock.writeLock().lock();
        try {
            collection.removeIf(h -> h.getId().equals(human.getId()));
            collection.add(human);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет объект по ID.
     *
     * @param id ID удаляемого объекта
     * @param ownerUsername имя владельца
     * @return true если удаление успешно, false иначе
     */
    public boolean removeById(long id, String ownerUsername) {
        if (!HumanBeingDAO.existsAndOwnedBy(id, ownerUsername)) {
            return false;
        }

        boolean deleted = HumanBeingDAO.delete(id, ownerUsername);
        if (!deleted) return false;

        lock.writeLock().lock();
        try {
            collection.removeIf(h -> h.getId() == id);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет все объекты, принадлежащие указанному пользователю.
     *
     * @param ownerUsername имя владельца
     */
    public void clearByOwner(String ownerUsername) {
        lock.writeLock().lock();
        try {
            collection.removeIf(h -> h.getOwner().equals(ownerUsername));
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Возвращает копию коллекции.
     *
     * @return копия коллекции
     */
    public Deque<HumanBeing> getCollection() {
        lock.readLock().lock();
        try {
            return new ArrayDeque<>(collection);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает размер коллекции.
     *
     * @return количество элементов в коллекции
     */
    public int size() {
        lock.readLock().lock();
        try {
            return collection.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, пуста ли коллекция.
     *
     * @return true если коллекция пуста, false иначе
     */
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return collection.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Находит элемент с минимальным ID.
     *
     * @return элемент с минимальным ID или null, если коллекция пуста
     */
    public HumanBeing findMinById() {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .min(HumanBeing::compareTo)
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, является ли impactSpeed минимальным в коллекции.
     *
     * @param impactSpeed проверяемое значение
     * @return true если значение минимальное, false иначе
     */
    public boolean isImpactSpeedMin(float impactSpeed) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .mapToDouble(HumanBeing::getImpactSpeed)
                    .min()
                    .orElse(Double.MAX_VALUE) >= impactSpeed;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, является ли impactSpeed максимальным в коллекции.
     *
     * @param impactSpeed проверяемое значение
     * @return true если значение максимальное, false иначе
     */
    public boolean isImpactSpeedMax(float impactSpeed) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .mapToDouble(HumanBeing::getImpactSpeed)
                    .max()
                    .orElse(-Double.MAX_VALUE) <= impactSpeed;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, существует ли объект с указанным ID и принадлежит ли он пользователю.
     *
     * @param id ID объекта
     * @param ownerUsername имя владельца
     * @return true если объект существует и принадлежит пользователю
     */
    public boolean existsAndOwnedBy(long id, String ownerUsername) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .anyMatch(h -> h.getId() == id && h.getOwner().equals(ownerUsername));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает первый элемент, принадлежащий указанному пользователю.
     *
     * @param ownerUsername имя владельца
     * @return первый элемент пользователя или null
     */
    public HumanBeing getFirstOwnedBy(String ownerUsername) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(h -> h.getOwner().equals(ownerUsername))
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Фильтрует коллекцию по настроению.
     *
     * @param mood настроение для фильтрации
     * @return отфильтрованная коллекция
     */
    public Deque<HumanBeing> filterByMood(Mood mood) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(h -> h.getMood() == mood)
                    .sorted()
                    .collect(Collectors.toCollection(ArrayDeque::new));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Фильтрует коллекцию по префиксу названия саундтрека.
     *
     * @param prefix префикс для фильтрации
     * @return отфильтрованная коллекция
     */
    public Deque<HumanBeing> filterBySoundtrack(String prefix) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(h -> h.getSoundtrackName().startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toCollection(ArrayDeque::new));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает информацию о коллекции.
     *
     * @return строка с информацией
     */
    public String info() {
        lock.readLock().lock();
        try {
            return "Тип коллекции: " + collection.getClass().getName() +
                    "\nКоличество элементов: " + collection.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает строковое представление всех элементов коллекции.
     *
     * @return строка со всеми элементами
     */
    public String show() {
        lock.readLock().lock();
        try {
            if (collection.isEmpty()) {
                return "Коллекция пуста";
            }
            return collection.stream()
                    .sorted()
                    .map(HumanBeing::toString)
                    .collect(Collectors.joining("\n\n"));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает справку по командам.
     *
     * @return строка со справкой
     */
    public String help() {
        return "Доступные команды:\n" +
                "  help - показать справку\n" +
                "  info - информация о коллекции\n" +
                "  show - показать все элементы\n" +
                "  add - добавить новый элемент\n" +
                "  add_if_min - добавить элемент, если его impactSpeed минимальный\n" +
                "  add_if_max - добавить элемент, если его impactSpeed максимальный\n" +
                "  min_by_id - показать элемент с минимальным ID\n" +
                "  remove_by_id <id> - удалить элемент по ID\n" +
                "  clear - очистить коллекцию (только свои элементы)\n" +
                "  remove_first - удалить первый элемент\n" +
                "  update <id> - обновить элемент\n" +
                "  filter_by_mood <mood> - фильтр по настроению\n" +
                "  filter_starts_with_soundtrack_name <prefix> - фильтр по саундтреку\n" +
                "  execute_script <file> - выполнить скрипт\n" +
                "  exit - завершить работу клиента";
    }

    /**
     * Возвращает элемент с минимальным ID в виде строки.
     *
     * @return строка с элементом
     */
    public String minById() {
        HumanBeing min = findMinById();
        if (min == null) {
            return "Коллекция пуста";
        }
        return "Элемент с минимальным ID (" + min.getId() + "):\n" + min;
    }


    /**
     * Удаляет первый элемент в коллекции.
     *
     * @return сообщение о результате
     */
    public String removeFirst() {
        lock.writeLock().lock();
        try {
            if (collection.isEmpty()) {
                return "Коллекция пуста";
            }
            HumanBeing removed = collection.removeFirst();
            return "Удалён первый элемент:\n" + removed;
        } finally {
            lock.writeLock().unlock();
        }
    }


    public String addIfMin(HumanBeing human, String ownerUsername) {
        if (isImpactSpeedMin(human.getImpactSpeed())) {
            boolean added = add(human, ownerUsername);
            if (added) {
                return "Добавлен элемент с минимальным impactSpeed, ID: " + human.getId();
            }
            return "Ошибка при добавлении элемента";
        }
        return "Элемент не добавлен: impactSpeed не является минимальным";
    }

    public String addIfMax(HumanBeing human, String ownerUsername) {
        if (isImpactSpeedMax(human.getImpactSpeed())) {
            boolean added = add(human, ownerUsername);
            if (added) {
                return "Добавлен элемент с максимальным impactSpeed, ID: " + human.getId();
            }
            return "Ошибка при добавлении элемента";
        }
        return "Элемент не добавлен: impactSpeed не является максимальным";
    }

    public String clear(String ownerUsername) {
        lock.writeLock().lock();
        try {
            int removed = (int) collection.stream()
                    .filter(h -> h.getOwner().equals(ownerUsername))
                    .count();
            collection.removeIf(h -> h.getOwner().equals(ownerUsername));
            return "Удалено элементов: " + removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean update(long id, HumanBeing human, String ownerUsername) {
        if (!existsAndOwnedBy(id, ownerUsername)) {
            return false;
        }
        human.setId(id);
        lock.writeLock().lock();
        try {
            collection.removeIf(h -> h.getId() == id);
            collection.add(human);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
