package common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Класс, представляющий человека (сущность HumanBeing).
 *
 * <p>Содержит информацию о человеке: идентификатор, имя, координаты,
 * дату создания, характеристики и связанные объекты (машина, оружие, настроение).
 * Класс реализует {@link Comparable} для сортировки по ID и {@link Serializable}
 * для передачи по сети и сохранения в файл.</p>
 *
 * <p>Поля класса валидируются при создании объекта. ID и дата создания
 * автоматически генерируются на сервере для новых объектов.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see Coordinates
 * @see Car
 * @see WeaponType
 * @see Mood
 */
public class HumanBeing implements Comparable<HumanBeing>, Serializable {
    /** Версия для сериализации. */
    private static final long serialVersionUID = 1L;

    /** Уникальный идентификатор человека (генерируется автоматически). */
    private Long id;
    /** Имя человека. Не может быть null или пустым. */
    private final String name;
    /** Координаты местоположения человека. Не могут быть null. */
    private final Coordinates coordinates;

    /** Дата и время создания записи (генерируется автоматически). */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime creationDate;

    /** Флаг, указывающий, является ли человек настоящим героем. Не может быть null. */
    private final Boolean realHero;
    /** Флаг, указывающий, есть ли у человека зубочистка. Не может быть null. */
    private final Boolean hasToothpick;
    /** Скорость удара. Максимальное значение - 657. */
    private final float impactSpeed;
    /** Название саундтрека. Не может быть null или пустым. */
    private final String soundtrackName;
    /** Тип оружия. Не может быть null. */
    private final WeaponType weaponType;
    /** Настроение. Может быть null. */
    private final Mood mood;
    /** Информация о машине. Не может быть null. */
    private final Car car;

    /**
     * Конструктор для создания НОВЫХ объектов (без ID и даты создания).
     * Используется при добавлении нового элемента через команды ADD, ADD_IF_MIN, ADD_IF_MAX.
     *
     * @param name имя человека (не может быть null или пустым)
     * @param coordinates координаты (не могут быть null)
     * @param realHero флаг настоящего героя (не может быть null)
     * @param hasToothpick флаг наличия зубочистки (не может быть null)
     * @param impactSpeed скорость удара (не может превышать 657)
     * @param soundtrackName название саундтрека (не может быть null или пустым)
     * @param weaponType тип оружия (не может быть null)
     * @param mood настроение (может быть null)
     * @param car информация о машине (не может быть null)
     * @throws IllegalArgumentException если какой-либо из обязательных параметров некорректен
     */
    public HumanBeing(String name, Coordinates coordinates,
                      Boolean realHero, Boolean hasToothpick,
                      float impactSpeed, String soundtrackName,
                      WeaponType weaponType, Mood mood, Car car) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name не может быть null или пустым");
        }
        this.name = name.trim();

        if (coordinates == null) {
            throw new IllegalArgumentException("coordinates не может быть null");
        }
        this.coordinates = coordinates;

        if (realHero == null) {
            throw new IllegalArgumentException("realHero не может быть null");
        }
        this.realHero = realHero;

        if (hasToothpick == null) {
            throw new IllegalArgumentException("hasToothpick не может быть null");
        }
        this.hasToothpick = hasToothpick;

        if (impactSpeed > 657) {
            throw new IllegalArgumentException("impactSpeed не может быть больше 657");
        }
        this.impactSpeed = impactSpeed;

        if (soundtrackName == null || soundtrackName.trim().isEmpty()) {
            throw new IllegalArgumentException("soundtrackName не может быть null или пустым");
        }
        this.soundtrackName = soundtrackName.trim();

        if (weaponType == null) {
            throw new IllegalArgumentException("weaponType не может быть null");
        }
        this.weaponType = weaponType;

        this.mood = mood;

        if (car == null) {
            throw new IllegalArgumentException("car не может быть null");
        }
        this.car = car;

        this.id = null;
        this.creationDate = null;
    }

    /**
     * Конструктор для загрузки объекта из файла (с полными данными).
     * Используется при десериализации из JSON.
     *
     * @param id уникальный идентификатор (должен быть > 0)
     * @param creationDate дата создания (не может быть null)
     * @param name имя человека
     * @param coordinates координаты
     * @param realHero флаг настоящего героя
     * @param hasToothpick флаг наличия зубочистки
     * @param impactSpeed скорость удара
     * @param soundtrackName название саундтрека
     * @param weaponType тип оружия
     * @param mood настроение
     * @param car информация о машине
     * @throws IllegalArgumentException если какой-либо из параметров некорректен
     */
    @JsonCreator
    public HumanBeing(
            @JsonProperty("id") Long id,
            @JsonProperty("creationDate") LocalDateTime creationDate,
            @JsonProperty("name") String name,
            @JsonProperty("coordinates") Coordinates coordinates,
            @JsonProperty("realHero") Boolean realHero,
            @JsonProperty("hasToothpick") Boolean hasToothpick,
            @JsonProperty("impactSpeed") float impactSpeed,
            @JsonProperty("soundtrackName") String soundtrackName,
            @JsonProperty("weaponType") WeaponType weaponType,
            @JsonProperty("mood") Mood mood,
            @JsonProperty("car") Car car) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id должен быть больше 0");
        }
        this.id = id;

        if (creationDate == null) {
            throw new IllegalArgumentException("creationDate не может быть null");
        }
        this.creationDate = creationDate;

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name не может быть null или пустым");
        }
        this.name = name.trim();

        if (coordinates == null) {
            throw new IllegalArgumentException("coordinates не может быть null");
        }
        this.coordinates = coordinates;

        if (realHero == null) {
            throw new IllegalArgumentException("realHero не может быть null");
        }
        this.realHero = realHero;

        if (hasToothpick == null) {
            throw new IllegalArgumentException("hasToothpick не может быть null");
        }
        this.hasToothpick = hasToothpick;

        if (impactSpeed > 657) {
            throw new IllegalArgumentException("impactSpeed не может быть больше 657");
        }
        this.impactSpeed = impactSpeed;

        if (soundtrackName == null || soundtrackName.trim().isEmpty()) {
            throw new IllegalArgumentException("soundtrackName не может быть null или пустым");
        }
        this.soundtrackName = soundtrackName.trim();

        if (weaponType == null) {
            throw new IllegalArgumentException("weaponType не может быть null");
        }
        this.weaponType = weaponType;

        this.mood = mood;

        if (car == null) {
            throw new IllegalArgumentException("car не может быть null");
        }
        this.car = car;
    }

    /**
     * Устанавливает ID человека (вызывается на сервере).
     *
     * @param id уникальный идентификатор
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Устанавливает дату создания записи (вызывается на сервере).
     *
     * @param creationDate дата и время создания
     */
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return уникальный идентификатор человека
     */
    public Long getId() { return id; }

    /**
     * @return имя человека
     */
    public String getName() { return name; }

    /**
     * @return координаты человека
     */
    public Coordinates getCoordinates() { return coordinates; }
    /**
     * @return дата создания записи
     */
    public LocalDateTime getCreationDate() { return creationDate; }

    /**
     * @return true, если человек является настоящим героем
     */
    public Boolean getRealHero() { return realHero; }

    /**
     * @return true, если у человека есть зубочистка
     */
    public Boolean getHasToothpick() { return hasToothpick; }

    /**
     * @return скорость удара
     */
    public float getImpactSpeed() { return impactSpeed; }

    /**
     * @return название саундтрека
     */
    public String getSoundtrackName() { return soundtrackName; }

    /**
     * @return тип оружия
     */
    public WeaponType getWeaponType() { return weaponType; }

    /**
     * @return настроение (может быть null)
     */
    public Mood getMood() { return mood; }

    /**
     * @return информация о машине
     */
    public Car getCar() { return car; }

    /**
     * Сравнивает двух людей по ID.
     * Используется для сортировки коллекции по умолчанию.
     *
     * @param other другой объект HumanBeing для сравнения
     * @return отрицательное число, если текущий ID меньше другого,
     *         положительное, если больше, 0 если равны
     */
    @Override
    public int compareTo(HumanBeing other) {
        if (this.id == null || other.id == null) {
            return 0;
        }
        return this.id.compareTo(other.id);
    }

    /**
     * Возвращает строковое представление объекта HumanBeing.
     *
     * @return форматированная строка со всеми полями объекта
     */
    @Override
    public String toString() {
        return String.format(
                "HumanBeing{id=%d, name='%s', coordinates=(%s, %s), creationDate=%s, realHero=%s, hasToothpick=%s, impactSpeed=%.1f, soundtrackName='%s', weaponType=%s, mood=%s, car=%s}",
                id, name, coordinates.getX(), coordinates.getY(), creationDate,
                realHero, hasToothpick, impactSpeed, soundtrackName, weaponType,
                mood, car.getCool() ? "cool" : "not cool"
        );
    }
}