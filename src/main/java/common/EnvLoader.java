package common;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;

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
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1");
    }
}
