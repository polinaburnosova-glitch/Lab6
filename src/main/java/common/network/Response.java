package common.network;

import java.io.Serializable;
import java.util.List;

/**
 * Класс, представляющий ответ от сервера клиенту.
 *
 * <p>Содержит результат выполнения команды на сервере: флаг успешности,
 * текстовое сообщение и опциональные данные (например, коллекцию объектов
 * для команды SHOW). Объекты Response сериализуются и передаются по сети.</p>
 *
 * <p>Ответ может быть двух типов:
 * <ul>
 *   <li>Успешный (success=true) с сообщением о результате выполнения</li>
 *   <li>Ошибочный (success=false) с сообщением об ошибке</li>
 * </ul>
 * </p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see Request
 * @see common.model.HumanBeing
 */
public class Response implements Serializable {

    /** Версия для сериализации, обеспечивающая совместимость версий. */
    private static final long serialVersionUID = 1L;

    /** Флаг успешности выполнения команды на сервере. */
    private final boolean success;

    /** Текстовое сообщение с результатом выполнения или описанием ошибки. */
    private final String message;

    /**
     * Дополнительные данные, передаваемые клиенту.
     * Используется для команды SHOW для передачи отсортированной коллекции.
     * Может быть null, если данные не требуются.
     */
    private final List<?> data;

    /**
     * Конструктор ответа с данными.
     *
     * @param success флаг успешности выполнения команды
     * @param message текстовое сообщение о результате
     * @param data список данных (например, коллекция объектов для SHOW)
     */
    public Response(boolean success, String message, List<?> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Конструктор ответа без данных.
     *
     * @param success флаг успешности выполнения команды
     * @param message текстовое сообщение о результате
     */
    public Response(boolean success, String message) {
        this(success, message, null);
    }

    /**
     * Проверяет, успешно ли выполнена команда.
     *
     * @return true, если команда выполнена успешно; false в случае ошибки
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Возвращает текстовое сообщение ответа.
     *
     * @return сообщение о результате выполнения команды
     */
    public String getMessage() {
        return message;
    }

    /**
     * Возвращает дополнительные данные ответа.
     *
     * @return список данных (обычно коллекция HumanBeing) или null, если данных нет
     */
    public List<?> getData() {
        return data;
    }
}