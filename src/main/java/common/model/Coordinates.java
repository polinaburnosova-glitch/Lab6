package common.model;

import java.io.Serializable;

/**
 * Класс, представляющий координаты.
 * Содержит координаты X и Y.
 */
public class Coordinates implements Serializable{
    private static final long serialVersionUID = 1L;

    private final Double x;
    private final Float y;

    /**
     * Конструктор координат.
     *
     * @param x координата X (не может быть null)
     * @param y координата Y (не может быть null)
     * @throws IllegalArgumentException если x или y равен null
     */
    public Coordinates(Double x, Float y) {
        if (x == null) {
            throw new IllegalArgumentException("Поле x не может быть null");
        }
        if (y == null) {
            throw new IllegalArgumentException("Поле y не может быть null");
        }
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает координату X.
     *
     * @return координата X
     */
    public Double getX() {
        return x;
    }

    /**
     * Возвращает координату Y.
     *
     * @return координата Y
     */
    public Float getY() {
        return y;
    }

    /**
     * Возвращает строковое представление координат.
     *
     * @return строка вида "(x, y)"
     */
    @Override
    public String toString() {
        return "x= " + x + ", y=" + y;
    }

}
