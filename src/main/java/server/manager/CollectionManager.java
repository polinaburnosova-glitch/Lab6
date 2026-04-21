package server.manager;

import common.model.HumanBeing;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import common.model.Mood;

/**
 * Менеджер коллекции объектов HumanBeing на сервере.
 *
 * <p>Обеспечивает хранение, управление и операции над коллекцией объектов
 * HumanBeing. Коллекция реализована как {@link ArrayDeque} для эффективного
 * доступа к первому и последнему элементам. Все операции над коллекцией
 * выполняются с использованием Stream API и лямбда-выражений.</p>
 *
 * <p>Класс также управляет генерацией уникальных ID для новых элементов
 * и предоставляет методы для выполнения всех команд, поддерживаемых
 * приложением.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see HumanBeing
 * @see common.model.Mood
 */
public class CollectionManager implements Serializable {

    /** Версия для сериализации. */
    private static final long serialVersionUID = 1L;

    /** Коллекция объектов HumanBeing (двусторонняя очередь). */
    private final Deque<HumanBeing> collection;

    /** Дата и время инициализации менеджера коллекции. */
    private final LocalDateTime initializationDate;

    /** Следующий доступный ID для новых элементов. */
    private long nextId = 1;

    /**
     * Конструктор менеджера коллекции.
     * Создаёт пустую коллекцию и фиксирует время инициализации.
     */
    public CollectionManager() {
        this.collection = new ArrayDeque<>();
        this.initializationDate = LocalDateTime.now();
    }

    /**
     * Инициализирует коллекцию загруженными из файла данными.
     *
     * @param loadedCollection загруженная коллекция (может быть null или пустой)
     */
    public void initializeCollection(Deque<HumanBeing> loadedCollection) {
        if (loadedCollection != null && !loadedCollection.isEmpty()) {
            collection.clear();
            collection.addAll(loadedCollection);
            nextId = collection.stream()
                    .mapToLong(HumanBeing::getId)
                    .max()
                    .orElse(0) + 1;
        }
    }

    /**
     * Возвращает отсортированную копию коллекции.
     *
     * @return отсортированная по умолчанию (по ID) коллекция
     */
    public Deque<HumanBeing> getCollection() {
        return collection.stream()
                .sorted()
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    /**
     * Возвращает тип коллекции.
     *
     * @return имя класса коллекции
     */
    public String getCollectionType() {
        return collection.getClass().getName();
    }

    /**
     * Возвращает дату инициализации коллекции.
     *
     * @return дата создания менеджера коллекции
     */
    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    /**
     * Возвращает количество элементов в коллекции.
     *
     * @return размер коллекции
     */
    public int size() {
        return collection.size();
    }

    /**
     * Возвращает информацию о коллекции.
     *
     * @return строка с типом коллекции, датой инициализации и количеством элементов
     */
    public String info() {
        return "Тип коллекции: " + getCollectionType() +
                "\nДата инициализации: " + initializationDate +
                "\nКоличество элементов: " + size();
    }

    /**
     * Возвращает строковое представление всех элементов коллекции.
     *
     * @return строка со всеми элементами в отсортированном порядке
     */
    public String show() {
        if (collection.isEmpty()) {
            return "Коллекция пуста";
        }
        return collection.stream()
                .sorted()
                .map(HumanBeing::toString)
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Возвращает справку по всем доступным командам.
     *
     * @return строка со списком команд и их описанием
     */
    public String help() {
        return "Доступные команды:\n" +
                "  help - показать справку\n" +
                "  info - информация о коллекции\n" +
                "  show - показать все элементы\n" +
                "  exit - завершить работу клиента\n" +
                "  add - добавить новый элемент\n" +
                "  add_if_min - добавить элемент, если его impactSpeed МИНИМАЛЬНЫЙ\n" +
                "  add_if_max - добавить элемент, если его impactSpeed МАКСИМАЛЬНЫЙ\n" +
                "  min_by_id - показать элемент с минимальным ID\n" +
                "  remove_by_id <id> - удалить элемент по ID\n" +
                "  clear - очистить коллекцию\n" +
                "  remove_first - удалить первый элемент из коллекции\n" +
                "  update <id> - обновить элемент с указанным ID\n" +
                "  filter_by_mood <mood> - вывести элементы с указанным настроением\n" +
                "  filter_starts_with_soundtrack_name <prefix> - вывести элементы, название саундтрека которых начинается с указанной подстроки\n" +
                "  execute_script <file> - выполнить скрипт из файла\n" +
                "  save - сохранить коллекцию (только на сервере)";
    }

    /**
     * Добавляет новый элемент в коллекцию.
     *
     * @param humanBeing объект HumanBeing для добавления
     * @return сообщение о результате добавления
     */
    public String add(HumanBeing humanBeing) {
        HumanBeing newHuman = new HumanBeing(
                nextId++,
                LocalDateTime.now(),
                humanBeing.getName(),
                humanBeing.getCoordinates(),
                humanBeing.getRealHero(),
                humanBeing.getHasToothpick(),
                humanBeing.getImpactSpeed(),
                humanBeing.getSoundtrackName(),
                humanBeing.getWeaponType(),
                humanBeing.getMood(),
                humanBeing.getCar()
        );
        collection.add(newHuman);
        return "Добавлен новый элемент, ID: " + newHuman.getId() + ", impactSpeed: " + newHuman.getImpactSpeed();
    }

    /**
     * Добавляет элемент, если его impactSpeed больше максимального в коллекции.
     *
     * @param humanBeing объект HumanBeing для добавления
     * @return сообщение о результате добавления
     */
    public String addIfMax(HumanBeing humanBeing) {
        Optional<HumanBeing> maxElement = collection.stream()
                .max(Comparator.comparingDouble(HumanBeing::getImpactSpeed));

        if (maxElement.isEmpty()) {
            return add(humanBeing);
        }

        if (humanBeing.getImpactSpeed() > maxElement.get().getImpactSpeed()) {
            return add(humanBeing);
        } else {
            return String.format("Элемент не добавлен. impactSpeed (%.1f) не больше максимального (%.1f)",
                    humanBeing.getImpactSpeed(), maxElement.get().getImpactSpeed());
        }
    }

    /**
     * Добавляет элемент, если его impactSpeed меньше минимального в коллекции.
     *
     * @param humanBeing объект HumanBeing для добавления
     * @return сообщение о результате добавления
     */
    public String addIfMin(HumanBeing humanBeing) {
        Optional<HumanBeing> minElement = collection.stream()
                .min(Comparator.comparingDouble(HumanBeing::getImpactSpeed));

        if (minElement.isEmpty()) {
            return add(humanBeing);
        }

        if (humanBeing.getImpactSpeed() < minElement.get().getImpactSpeed()) {
            return add(humanBeing);
        } else {
            return String.format("Элемент не добавлен. impactSpeed (%.1f) не меньше минимального (%.1f)",
                    humanBeing.getImpactSpeed(), minElement.get().getImpactSpeed());
        }
    }

    /**
     * Находит элемент с минимальным ID.
     *
     * @return строковое представление элемента с минимальным ID
     */
    public String minById() {
        return collection.stream()
                .min((h1, h2) -> h1.getId().compareTo(h2.getId()))
                .map(h -> "Элемент с минимальным ID (" + h.getId() + "):\n" + h.toString())
                .orElse("Коллекция пуста");
    }

    /**
     * Удаляет элемент по ID.
     *
     * @param id идентификатор элемента для удаления
     * @return сообщение о результате удаления
     */
    public String removeById(Long id) {
        boolean removed = collection.removeIf(h -> h.getId().equals(id));

        if (removed) {
            return "Элемент с id " + id + " успешно удален";
        }
        else {
            return "Элемент с id " + id + " не найден";
        }
    }

    /**
     * Очищает всю коллекцию и сбрасывает счётчик ID.
     *
     * @return сообщение о результате очистки
     */
    public String clear() {
        int size = collection.size();
        collection.clear();
        nextId = 1;
        return "Коллекция очищена. Удалено элементов: " + size;
    }

    /**
     * Удаляет первый элемент коллекции.
     *
     * @return сообщение о результате удаления
     */
    public String removeFirst() {
        if (collection.isEmpty()) {
            return "Коллекция пуста. Нечего удалять.";
        }
        HumanBeing removed = collection.removeFirst();
        return "Удален первый элемент:\n" + removed.toString();
    }

    /**
     * Обновляет существующий элемент по ID.
     *
     * @param id идентификатор элемента для обновления
     * @param newHuman новый объект HumanBeing с обновлёнными данными
     * @return сообщение о результате обновления
     */
    public String update(Long id, HumanBeing newHuman) {
        Optional<HumanBeing> existing = collection.stream()
                .filter(h -> h.getId().equals(id))
                .findFirst();
        if (existing.isEmpty()) {
            return "Элемент с ID " + id + " не найден";
        }

        collection.remove(existing.get());

        HumanBeing updated = new HumanBeing(
                id,
                existing.get().getCreationDate(),
                newHuman.getName(),
                newHuman.getCoordinates(),
                newHuman.getRealHero(),
                newHuman.getHasToothpick(),
                newHuman.getImpactSpeed(),
                newHuman.getSoundtrackName(),
                newHuman.getWeaponType(),
                newHuman.getMood(),
                newHuman.getCar()
        );

        collection.add(updated);
        return "Элемент с ID " + id + " успешно обновлен";
    }

    /**
     * Фильтрует элементы коллекции по настроению.
     *
     * @param mood настроение для фильтрации
     * @return строковое представление отфильтрованных элементов
     */
    public String filterByMood(Mood mood) {
        List<HumanBeing> filtered = new ArrayList<>();
        collection.stream()
                .filter(h -> h.getMood() == mood)
                .sorted()
                .forEach(filtered::add);

        if (filtered.isEmpty()) {
            return "Элементы с настроением " + mood + " не найдены";
        }

        StringBuilder sb = new StringBuilder();
        for (HumanBeing h : filtered) {
            sb.append(h.toString()).append("\n\n");
        }
        return sb.toString().trim();
    }

    /**
     * Фильтрует элементы по префиксу названия саундтрека.
     *
     * @param prefix префикс для поиска в названии саундтрека
     * @return строковое представление отфильтрованных элементов
     */
    public String filterStartsWithSoundtrackName(String prefix) {
        StringBuilder result = new StringBuilder();

        collection.stream()
                .filter(h -> h.getSoundtrackName().startsWith(prefix))
                .sorted()
                .forEach(h -> result.append(h.toString()).append("\n\n"));
        if (result.length() == 0) {
            return "Элементы, название саундтрека которых начинается с '" + prefix + "', не найдены";
        }

        return result.toString().trim();
    }
}