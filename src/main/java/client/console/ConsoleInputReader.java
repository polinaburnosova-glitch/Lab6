package client.console;

import common.model.*;

import java.util.Scanner;

/**
 * Реализация для чтения из консоли.
 * Обеспечивает пошаговый ввод всех полей объекта HumanBeing
 * с валидацией каждого поля и возможностью повторного ввода при ошибке.
 */
public class ConsoleInputReader implements InputReader {
    private final Scanner scanner;

    /**
     * Конструктор. Инициализирует Scanner для чтения из System.in.
     */
    public ConsoleInputReader() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Читает объект HumanBeing из консоли.
     * Каждое поле запрашивается отдельно с валидацией.
     *
     * @return созданный объект HumanBeing с null id и null creationDate
     */
    @Override
    public HumanBeing readHumanBeing() {

        String name = readName();
        Coordinates coordinates = readCoordinates();
        Boolean realHero = readBoolean("realHero (true/false):");
        Boolean hasToothpick = readBoolean("hasToothpick (true/false):");
        float impactSpeed = readImpactSpeed();
        String soundtrackName = readSoundtrackName();
        WeaponType weaponType = readWeaponType();
        Mood mood = readMood();
        Car car = readCar();

        System.out.println("Элемент успешно создан");
        return new HumanBeing(name, coordinates, realHero, hasToothpick,
                impactSpeed, soundtrackName, weaponType, mood, car);
    }

    /**
     * Читает имя человека с валидацией на непустое значение.
     *
     * @return непустое имя
     */
    private String readName() {
        String input;
        do {
            System.out.println("Введите имя: ");
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Ошибка: имя не может быть пустой строкой");
            }
        }
        while (input.isEmpty());
        return input;

    }

    /**
     * Читает координаты человека.
     *
     * @return объект Coordinates
     */
    private Coordinates readCoordinates() {
        System.out.println("Введите координаты:");
        Double x = readDouble("x:", null, null);
        Float y = readFloat("y:", null, null);
        return new Coordinates(x, y);
    }

    /**
     * Читает число типа Double с валидацией.
     *
     * @param prompt приглашение к вводу
     * @param min минимальное значение (null = без ограничения)
     * @param max максимальное значение (null = без ограничения)
     * @return введенное число
     */
    private Double readDouble(String prompt, Double min, Double max) {
        while (true) {
            try {
                System.out.print(prompt + " ");
                double value = Double.parseDouble(scanner.nextLine().trim());

                if (min != null && value < min) {
                    System.out.println("Ошибка: значение должно быть не меньше " + min);
                    continue;
                }
                if (max != null && value > max) {
                    System.out.println("Ошибка: значение должно быть не больше " + max);
                    continue;
                }
                return value;

            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число!");
            }
        }
    }

    /**
     * Читает число типа Float с валидацией.
     *
     * @param prompt приглашение к вводу
     * @param min минимальное значение (null = без ограничения)
     * @param max максимальное значение (null = без ограничения)
     * @return введенное число
     */
    private Float readFloat(String prompt, Float min, Float max) {
        while (true) {
            try {
                System.out.print(prompt + " ");
                float value = Float.parseFloat(scanner.nextLine().trim());

                if (min != null && value < min) {
                    System.out.println("Ошибка: значение должно быть не меньше " + min);
                    continue;
                }
                if (max != null && value > max) {
                    System.out.println("Ошибка: значение должно быть не больше " + max);
                    continue;
                }
                return value;

            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число!");
            }
        }
    }

    /**
     * Читает скорость воздействия с валидацией (максимум 657).
     *
     * @return скорость воздействия
     */
    private float readImpactSpeed() {
        while (true) {
            try {
                System.out.print("impactSpeed (<=657): ");
                float value = Float.parseFloat(scanner.nextLine().trim());
                if (value <= 657) {
                    return value;
                }
                System.out.println("Ошибка: значение не должно превышать 657");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число!");
            }
        }
    }

    /**
     * Читает название саундтрека с валидацией на непустое значение.
     *
     * @return непустое название саундтрека
     */
    private String readSoundtrackName() {
        while (true) {
            System.out.print("soundtrackName: ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Ошибка: название саундтрека не может быть пустым");
        }
    }

    /**
     * Читает булево значение (true/false) с валидацией.
     *
     * @param prompt приглашение к вводу
     * @return true или false
     */
    private Boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("true")) return true;
            if (input.equals("false")) return false;
            System.out.println("Ошибка: введите true или false!");
        }
    }

    /**
     * Читает тип оружия из перечисления WeaponType.
     * Выводит список доступных значений.
     *
     * @return выбранный тип оружия
     */
    private WeaponType readWeaponType() {
        while (true) {
            try {
                System.out.println("Доступные типы оружия (HAMMER, SHOTGUN, KNIFE, BAT):");
                for (WeaponType type : WeaponType.values()) {
                    System.out.println("  - " + type);
                }
                System.out.print("weaponType: ");
                String input = scanner.nextLine().trim().toUpperCase();
                return WeaponType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: введите одно из указанных значений");
            }
        }
    }

    /**
     * Читает настроение из перечисления Mood.
     * Выводит список доступных значений. Можно пропустить (Enter).
     *
     * @return выбранное настроение или null
     */
    private Mood readMood() {
        while (true) {
            System.out.println("Доступные настроения (SORROW, LONGING, GLOOM, APATHY): ");
            for (Mood m : Mood.values()) {
                System.out.println("  - " + m);
            }
            System.out.print("mood: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return null;
            }

            try {
                return Mood.valueOf(input.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: введите одно из указанных значений или Enter");
            }
        }
    }

    /**
     * Читает информацию о машине.
     *
     * @return объект Car
     */
    private Car readCar() {
        Boolean cool = readBoolean("car.cool (true/false):");
        return new Car(cool);
    }
}
