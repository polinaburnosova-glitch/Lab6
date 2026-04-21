package server;

import common.network.*;
import server.manager.CollectionManager;
import server.manager.FileManager;
import common.model.HumanBeing;
import common.model.Mood;

public class CommandExecutor {

    private final CollectionManager collectionManager;
    private final FileManager fileManager;

    public CommandExecutor(CollectionManager collectionManager, FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
    }

    public Response execute(Request request) {
        CommandType type = request.getCommandType();
        Object[] args = request.getArgs();

        try {
            switch (type) {
                case INFO:
                    return ok(collectionManager.info());

                case SHOW:
                    return new Response(ResponseStatus.OK, collectionManager.show(),
                            collectionManager.getCollection().stream().collect(java.util.stream.Collectors.toList()));

                case HELP:
                    return ok(collectionManager.help());

                case EXIT:
                    return ok("До свидания!");

                case ADD:
                    HumanBeing newHuman = getArg(args, 0, HumanBeing.class);
                    if (newHuman == null) {
                        return validationError("не указан объект для добавления");
                    }
                    return ok(collectionManager.add(newHuman));

                case ADD_IF_MIN:
                    HumanBeing minHuman = getArg(args, 0, HumanBeing.class);
                    if (minHuman == null) {
                        return validationError("не указан объект для добавления");
                    }
                    return handleAddResult(collectionManager.addIfMin(minHuman));

                case ADD_IF_MAX:
                    HumanBeing maxHuman = getArg(args, 0, HumanBeing.class);
                    if (maxHuman == null) {
                        return validationError("не указан объект для добавления");
                    }
                    return handleAddResult(collectionManager.addIfMax(maxHuman));

                case MIN_BY_ID:
                    return ok(collectionManager.minById());

                case REMOVE_BY_ID:
                    Long idToRemove = getArg(args, 0, Long.class);
                    if (idToRemove == null) {
                        return validationError("не указан id для удаления");
                    }
                    return handleRemoveResult(collectionManager.removeById(idToRemove));

                case CLEAR:
                    return ok(collectionManager.clear());

                case REMOVE_FIRST:
                    return ok(collectionManager.removeFirst());

                case UPDATE:
                    Long updateId = getArg(args, 0, Long.class);
                    HumanBeing updateHuman = getArg(args, 1, HumanBeing.class);
                    if (updateId == null || updateHuman == null) {
                        return validationError("не указан id или объект для обновления");
                    }
                    return handleUpdateResult(collectionManager.update(updateId, updateHuman));

                case FILTER_BY_MOOD:
                    Mood mood = getArg(args, 0, Mood.class);
                    if (mood == null) {
                        return validationError("не указано настроение для фильтрации");
                    }
                    return ok(collectionManager.filterByMood(mood));

                case FILTER_STARTS_WITH_SOUNDTRACK_NAME:
                    String prefix = getArg(args, 0, String.class);
                    if (prefix == null) {
                        return validationError("не указана подстрока для фильтрации");
                    }
                    return ok(collectionManager.filterStartsWithSoundtrackName(prefix));

                case EXECUTE_SCRIPT:
                    return ok("Команда выполнена");

                default:
                    return unknownCommand(type);
            }
        } catch (Exception e) {
            return serverError("Ошибка выполнения: " + e.getMessage());
        }
    }

    public String save() {
        try {
            fileManager.saveCollection(collectionManager.getCollection());
            return "Коллекция сохранена в файл";
        } catch (Exception e) {
            return "Ошибка сохранения: " + e.getMessage();
        }
    }


    @SuppressWarnings("unchecked")
    private <T> T getArg(Object[] args, int index, Class<T> type) {
        if (args != null && args.length > index && type.isInstance(args[index])) {
            return (T) args[index];
        }
        return null;
    }


    private Response handleAddResult(String result) {
        if (result.contains("не добавлен")) {
            return warning(result);
        }
        return ok(result);
    }

    private Response handleRemoveResult(String result) {
        if (result.contains("не найден")) {
            return notFound(result);
        }
        return ok(result);
    }

    private Response handleUpdateResult(String result) {
        if (result.contains("не найден")) {
            return notFound(result);
        }
        return ok(result);
    }


    private Response ok(String message) {
        return new Response(ResponseStatus.OK, message);
    }

    private Response warning(String message) {
        return new Response(ResponseStatus.WARNING, message);
    }

    private Response notFound(String message) {
        return new Response(ResponseStatus.NOT_FOUND, message);
    }

    private Response validationError(String message) {
        return new Response(ResponseStatus.VALIDATION_ERROR, "Ошибка: " + message);
    }

    private Response serverError(String message) {
        return new Response(ResponseStatus.SERVER_ERROR, message);
    }

    private Response unknownCommand(CommandType type) {
        return new Response(ResponseStatus.UNKNOWN_COMMAND, "Команда не реализована: " + type);
    }
}