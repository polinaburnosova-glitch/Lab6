package server;

import common.network.*;
import server.manager.CollectionManager;
import server.manager.FileManager;
import common.model.HumanBeing;
import common.model.Mood;

/**
 * Исполнитель команд на сервере.
 *
 * <p>Получает запросы от клиента, выполняет соответствующую команду
 * над коллекцией через {@link CollectionManager} и формирует ответ
 * для отправки обратно клиенту.</p>
 *
 * <p>Класс обрабатывает все типы команд, поддерживаемые приложением,
 * включая добавление, удаление, обновление, фильтрацию и отображение
 * элементов коллекции.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see CollectionManager
 * @see FileManager
 * @see Request
 * @see Response
 */
public class CommandExecutor {

    /** Менеджер коллекции для выполнения операций над данными. */
    private final CollectionManager collectionManager;

    /** Менеджер файлов для сохранения коллекции. */
    private final FileManager fileManager;

    /**
     * Конструктор исполнителя команд.
     *
     * @param collectionManager менеджер коллекции
     * @param fileManager менеджер файлов
     */
    public CommandExecutor(CollectionManager collectionManager, FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
    }

    /**
     * Выполняет команду из запроса и формирует ответ.
     *
     * <p>В зависимости от типа команды извлекает аргументы из запроса,
     * вызывает соответствующий метод {@link CollectionManager} и
     * возвращает результат в виде {@link Response}.</p>
     *
     * @param request объект запроса с типом команды и аргументами
     * @return объект ответа с результатом выполнения команды
     */
    public Response execute(Request request) {
        CommandType type = request.getCommandType();
        Object[] args = request.getArgs();

        try {
            switch (type) {
                case INFO:
                    return new Response(true, collectionManager.info(), null);

                case SHOW:
                    return new Response(true, collectionManager.show(), null);

                case HELP:
                    return new Response(true, collectionManager.help(), null);

                case EXIT:
                    return new Response(true, "До свидания!", null);

                case ADD:
                    if (args != null && args.length > 0 && args[0] instanceof HumanBeing) {
                        HumanBeing newHuman = (HumanBeing) args[0];
                        String addResult = collectionManager.add(newHuman);
                        return new Response(true, addResult, null);
                    }
                    return new Response(false, "Ошибка: не указан объект для добавления", null);

                case ADD_IF_MIN:
                    if (args != null && args.length > 0 && args[0] instanceof HumanBeing) {
                        HumanBeing minHuman = (HumanBeing) args[0];
                        String minResult = collectionManager.addIfMin(minHuman);
                        return new Response(true, minResult, null);
                    }
                    return new Response(false, "Ошибка: не указан объект для добавления", null);

                case ADD_IF_MAX:
                    if (args != null && args.length > 0 && args[0] instanceof HumanBeing) {
                        HumanBeing maxHuman = (HumanBeing) args[0];
                        String maxResult = collectionManager.addIfMax(maxHuman);
                        return new Response(true, maxResult, null);
                    }
                    return new Response(false, "Ошибка: не указан объект для добавления", null);

                case MIN_BY_ID:
                    String minByIdResult = collectionManager.minById();
                    return new Response(true, minByIdResult, null);

                case REMOVE_BY_ID:
                    if (args != null && args.length > 0 && args[0] instanceof Long) {
                        Long idToRemove = (Long) args[0];
                        String removeResult = collectionManager.removeById(idToRemove);
                        return new Response(true, removeResult, null);
                    }
                    return new Response(false, "Ошибка: не указан id для удаления", null);

                case CLEAR:
                    String clearResult = collectionManager.clear();
                    return new Response(true, clearResult, null);

                case REMOVE_FIRST:
                    String removeFirstResult = collectionManager.removeFirst();
                    return new Response(true, removeFirstResult, null);

                case UPDATE:
                    if (args != null && args.length >= 2 && args[0] instanceof Long && args[1] instanceof HumanBeing) {
                        Long updateId = (Long) args[0];
                        HumanBeing updateHuman = (HumanBeing) args[1];
                        String updateResult = collectionManager.update(updateId, updateHuman);
                        return new Response(true, updateResult, null);
                    }
                    return new Response(false, "Ошибка: не указан id или объект для обновления", null);

                case FILTER_BY_MOOD:
                    if (args != null && args.length > 0 && args[0] instanceof Mood) {
                        Mood mood = (Mood) args[0];
                        String filterMoodResult = collectionManager.filterByMood(mood);
                        return new Response(true, filterMoodResult, null);
                    }
                    return new Response(false, "Ошибка: не указано настроение для фильтрации", null);

                case FILTER_STARTS_WITH_SOUNDTRACK_NAME:
                    if (args != null && args.length > 0 && args[0] instanceof String) {
                        String prefix = (String) args[0];
                        String filterNameResult = collectionManager.filterStartsWithSoundtrackName(prefix);
                        return new Response(true, filterNameResult, null);
                    }
                    return new Response(false, "Ошибка: не указана подстрока для фильтрации", null);

                case EXECUTE_SCRIPT:
                    return new Response(true, "Команда выполнена", null);

                default:
                    return new Response(false, "Команда не реализована: " + type, null);
            }
        } catch (Exception e) {
            return new Response(false, "Ошибка выполнения: " + e.getMessage(), null);
        }
    }

    /**
     * Сохраняет текущую коллекцию в файл.
     *
     * <p>Это специальная команда, доступная только на сервере.
     * Клиент не может отправить эту команду.</p>
     *
     * @return сообщение о результате сохранения
     */
    public String save() {
        try {
            fileManager.saveCollection(collectionManager.getCollection());
            return "Коллекция сохранена в файл";
        } catch (Exception e) {
            return "Ошибка сохранения: " + e.getMessage();
        }
    }
}