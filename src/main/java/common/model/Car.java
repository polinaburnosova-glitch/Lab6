package common.model;

import java.io.Serializable;

/**
 * Класс, представляющий машину.
 * Содержит информацию о том, крутая ли машина.
 */
public class Car implements Serializable{
    private static final long serialVersionUID = 1L;

    private final Boolean cool;

    /**
     * Конструктор машины.
     *
     * @param cool флаг "крутая ли машина" (не может быть null)
     * @throws IllegalArgumentException если cool равен null
     */
    public Car(Boolean cool) {
        if (cool == null) {
            throw new IllegalArgumentException("Поле cool не может быть null");
        }
        this.cool = cool;
    }

    /**
     * Возвращает флаг "крутая ли машина".
     *
     * @return true если машина крутая, иначе false
     */
    public Boolean getCool() {
        return cool;
    }

    /**
     * Возвращает строковое представление машины.
     *
     * @return "cool" если true, иначе "not cool"
     */
    @Override
    public String toString() {
        return "cool = " + cool;
    }
}
