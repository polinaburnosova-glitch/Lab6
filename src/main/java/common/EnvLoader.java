package common;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;

/**
 * Загрузчик переменных окружения из файла .env.
 *
 * <p>Позволяет хранить конфигурацию (порт, хост, параметры БД) в отдельном файле,
 * который не попадает в Git.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 */
public class EnvLoader {

    private static Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();
        }
    }

    /**
     * Возвращает строковое значение переменной.
     *
     * @param key имя переменной
     * @param defaultValue значение по умолчанию, если переменная не найдена
     * @return значение переменной или defaultValue
     */
    public static String get(String key, String defaultValue) {
        String value = dotenv.get(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Возвращает целочисленное значение переменной.
     *
     * @param key имя переменной
     * @param defaultValue значение по умолчанию
     * @return значение переменной как int
     */
    public static int getInt(String key, int defaultValue) {
        String value = dotenv.get(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: переменная " + key + " должна быть числом. Используется значение по умолчанию: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Возвращает булево значение переменной.
     *
     * @param key имя переменной
     * @param defaultValue значение по умолчанию
     * @return true/false
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = dotenv.get(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value.equalsIgnoreCase("true") ||
                value.equalsIgnoreCase("yes") ||
                value.equals("1");
    }

    /**
     * Возвращает строковое значение переменной (без значения по умолчанию).
     *
     * @param key имя переменной
     * @return значение переменной или null
     */
    public static String get(String key) {
        return dotenv.get(key);
    }

    /**
     * Перезагружает файл .env.
     */
    public static void reload() {
        dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();
    }
}
