package common.network;

import java.io.Serializable;
import java.util.Arrays;
import common.model.User;

/**
 * Класс, представляющий запрос от клиента к серверу.
 *
 * <p>Запрос содержит тип команды и массив аргументов, необходимых для её выполнения.
 * Объекты Request сериализуются и передаются по сети между клиентом и сервером.</p>
 *
 * <p>Аргументы могут быть различных типов в зависимости от команды:
 * <ul>
 *   <li>Для команд ADD, ADD_IF_MIN, ADD_IF_MAX - массив с одним объектом HumanBeing</li>
 *   <li>Для команды UPDATE - массив [Long id, HumanBeing human]</li>
 *   <li>Для команды REMOVE_BY_ID - массив с одним Long значением</li>
 *   <li>Для FILTER_BY_MOOD - массив с одним объектом Mood</li>
 *   <li>Для FILTER_STARTS_WITH_SOUNDTRACK_NAME - массив с одной строкой</li>
 *   <li>Для команд без аргументов (HELP, INFO, SHOW, CLEAR и др.) - null</li>
 * </ul>
 * </p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see CommandType
 * @see Response
 * @see common.model.HumanBeing
 */
public class Request implements Serializable {

    /** Версия для сериализации, обеспечивающая совместимость версий. */
    private static final long serialVersionUID = 1L;

    /** Тип команды, определяющий действие, которое нужно выполнить на сервере. */
    private final CommandType commandType;

    /**
     * Массив аргументов команды.
     * Содержит параметры, необходимые для выполнения команды.
     * Может быть null для команд без аргументов.
     */
    private final Object[] args;

    /**
     * Конструктор запроса.
     *
     * @param commandType тип команды (не может быть null)
     * @param args массив аргументов команды (может быть null)
     */

    private final User user;

    public Request(CommandType commandType, Object[] args, User user) {
        this.commandType = commandType;
        this.args = args;
        this.user = user;
    }

    /**
     * Возвращает тип команды.
     *
     * @return тип команды
     */
    public CommandType getCommandType() {
        return commandType;
    }

    /**
     * Возвращает массив аргументов команды.
     *
     * @return массив аргументов или null, если аргументы отсутствуют
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * Возвращает строковое представление запроса.
     *
     * @return строка с типом команды и аргументами
     */
    public User getUser() { return user; }

    @Override
    public String toString() {
        return "Request(" +
                "commandType=" + commandType +
                ", args=" + Arrays.toString(args) +
                ", user=" + (user != null ? user.getUsername() : "null") +
                "}";
    }
}