package server;

import common.model.HumanBeing;
import common.model.Mood;
import common.model.User;
import common.network.*;
import server.database.UserDAO;
import server.manager.CollectionManager;

import java.util.Deque;

public class CommandExecutor {

    private final CollectionManager collectionManager;

    public CommandExecutor(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public Response execute(Request request) {
        CommandType type = request.getCommandType();
        Object[] args = request.getArgs();
        User user = request.getUser();

        if (user == null && type != CommandType.LOGIN && type != CommandType.REGISTER) {
            return new Response(ResponseStatus.UNAUTHORIZED, "Необходимо авторизоваться");
        }

        try {
            switch (type) {
                case LOGIN:
                    return handleLogin(args);

                case REGISTER:
                    return handleRegister(args);

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
                    boolean added = collectionManager.add(newHuman, user.getUsername());
                    if (added) {
                        return ok("Добавлен элемент с ID: " + newHuman.getId());
                    } else {
                        return serverError("Ошибка сохранения в БД");
                    }

                case ADD_IF_MIN:
                    HumanBeing minHuman = getArg(args, 0, HumanBeing.class);
                    if (minHuman == null) {
                        return validationError("не указан объект для добавления");
                    }
                    String minResult = collectionManager.addIfMin(minHuman, user.getUsername());
                    if (minResult.contains("не добавлен")) {
                        return warning(minResult);
                    }
                    return ok(minResult);

                case ADD_IF_MAX:
                    HumanBeing maxHuman = getArg(args, 0, HumanBeing.class);
                    if (maxHuman == null) {
                        return validationError("не указан объект для добавления");
                    }
                    String maxResult = collectionManager.addIfMax(maxHuman, user.getUsername());
                    if (maxResult.contains("не добавлен")) {
                        return warning(maxResult);
                    }
                    return ok(maxResult);

                case MIN_BY_ID:
                    return ok(collectionManager.minById());

                case REMOVE_BY_ID:
                    Long idToRemove = getArg(args, 0, Long.class);
                    if (idToRemove == null) {
                        return validationError("не указан id для удаления");
                    }
                    boolean deleted = collectionManager.removeById(idToRemove, user.getUsername());
                    if (deleted) {
                        return ok("Элемент с ID " + idToRemove + " удалён");
                    } else {
                        return notFound("Элемент с ID " + idToRemove + " не найден");
                    }

                case CLEAR:
                    return ok(collectionManager.clear(user.getUsername()));

                case REMOVE_FIRST:
                    return ok(collectionManager.removeFirst());

                case UPDATE:
                    Long updateId = getArg(args, 0, Long.class);
                    HumanBeing updateHuman = getArg(args, 1, HumanBeing.class);
                    if (updateId == null || updateHuman == null) {
                        return validationError("не указан id или объект для обновления");
                    }
                    boolean updated = collectionManager.update(updateId, updateHuman, user.getUsername());
                    if (updated) {
                        return ok("Элемент с ID " + updateId + " успешно обновлён");
                    } else {
                        return notFound("Элемент с ID " + updateId + " не найден");
                    }

                case FILTER_BY_MOOD:
                    Mood mood = getArg(args, 0, Mood.class);
                    if (mood == null) {
                        return validationError("не указано настроение для фильтрации");
                    }
                    Deque<HumanBeing> filteredByMood = collectionManager.filterByMood(mood);
                    if (filteredByMood.isEmpty()) {
                        return ok("Нет элементов с настроением " + mood);
                    }
                    StringBuilder moodResult = new StringBuilder();
                    for (HumanBeing h : filteredByMood) {
                        moodResult.append(h).append("\n\n");
                    }
                    return ok(moodResult.toString().trim());

                case FILTER_STARTS_WITH_SOUNDTRACK_NAME:
                    String prefix = getArg(args, 0, String.class);
                    if (prefix == null) {
                        return validationError("не указана подстрока для фильтрации");
                    }
                    Deque<HumanBeing> filteredBySoundtrack = collectionManager.filterBySoundtrack(prefix);
                    if (filteredBySoundtrack.isEmpty()) {
                        return ok("Нет элементов с саундтреком, начинающимся на '" + prefix + "'");
                    }
                    StringBuilder soundtrackResult = new StringBuilder();
                    for (HumanBeing h : filteredBySoundtrack) {
                        soundtrackResult.append(h).append("\n\n");
                    }
                    return ok(soundtrackResult.toString().trim());

                case EXECUTE_SCRIPT:
                    return ok("Команда выполнена");

                default:
                    return unknownCommand(type);
            }
        } catch (Exception e) {
            return serverError("Ошибка выполнения: " + e.getMessage());
        }
    }

    private Response handleLogin(Object[] args) {
        if (args == null || args.length < 2) {
            return validationError("Не указан логин или пароль");
        }
        String username = (String) args[0];
        String password = (String) args[1];

        User user = UserDAO.login(username, password);
        if (user == null) {
            return new Response(ResponseStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }
        return new Response(ResponseStatus.OK, "Авторизация успешна", null);
    }

    private Response handleRegister(Object[] args) {
        if (args == null || args.length < 2) {
            return validationError("Не указан логин или пароль");
        }
        String username = (String) args[0];
        String password = (String) args[1];

        if (UserDAO.userExists(username)) {
            return new Response(ResponseStatus.VALIDATION_ERROR, "Пользователь уже существует");
        }

        boolean registered = UserDAO.register(username, password);
        if (!registered) {
            return new Response(ResponseStatus.SERVER_ERROR, "Ошибка регистрации");
        }
        return ok("Регистрация успешна. Теперь войдите через LOGIN");
    }

    @SuppressWarnings("unchecked")
    private <T> T getArg(Object[] args, int index, Class<T> type) {
        if (args != null && args.length > index && type.isInstance(args[index])) {
            return (T) args[index];
        }
        return null;
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