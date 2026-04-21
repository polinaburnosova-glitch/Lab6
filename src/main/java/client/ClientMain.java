package client;

import common.network.*;
import common.model.*;
import client.console.ConsoleInputReader;
import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * Главный класс клиентского приложения.
 * Обеспечивает интерактивное взаимодействие пользователя с сервером,
 * чтение команд из консоли, валидацию вводимых данных и выполнение скриптов.
 *
 * <p>Клиент подключается к серверу по TCP, отправляет сериализованные запросы
 * и обрабатывает полученные ответы. Поддерживает выполнение команд как
 * в интерактивном режиме, так и из файлов-скриптов.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see SimpleClient
 * @see Request
 * @see Response
 */
public class ClientMain {
    /** Максимальная допустимая глубина вложенности при выполнении скриптов. */
    private static final int MAX_SCRIPT_DEPTH = 5;
    /** Текущая глубина рекурсивного выполнения скриптов. */
    private static int scriptDepth = 0;

    /**
     * Точка входа в клиентское приложение.
     *
     * <p>Обрабатывает аргументы командной строки (хост и порт сервера),
     * устанавливает соединение с сервером и запускает основной цикл
     * чтения и обработки команд пользователя.</p>
     *
     * @param args аргументы командной строки:
     *             args[0] - хост сервера (по умолчанию "localhost"),
     *             args[1] - порт сервера (по умолчанию 8080)
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт, используется 8080");
            }
        }

        SimpleClient client = new SimpleClient(host, port);

        try {
            client.connect();
            System.out.println("Введите help для списка команд");

            Scanner scanner = new Scanner(System.in);
            ConsoleInputReader inputReader = new ConsoleInputReader();

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                String[] parts = input.split("\\s+", 2);
                String command = parts[0].toUpperCase();
                Request request = null;

                try {
                    switch (command) {
                        case "ADD":
                            System.out.println("Введите данные для нового элемента:");
                            HumanBeing newHuman = inputReader.readHumanBeing();
                            request = new Request(CommandType.ADD, new Object[]{newHuman});
                            break;

                        case "ADD_IF_MIN":
                            System.out.println("Введите данные для нового элемента:");
                            HumanBeing minHuman = inputReader.readHumanBeing();
                            request = new Request(CommandType.ADD_IF_MIN, new Object[]{minHuman});
                            break;

                        case "ADD_IF_MAX":
                            System.out.println("Введите данные для нового элемента:");
                            HumanBeing maxHuman = inputReader.readHumanBeing();
                            request = new Request(CommandType.ADD_IF_MAX, new Object[]{maxHuman});
                            break;

                        case "REMOVE_BY_ID":
                            if (parts.length < 2) {
                                System.out.println("Ошибка: не указан ID");
                                continue;
                            }
                            try {
                                long id = Long.parseLong(parts[1]);
                                request = new Request(CommandType.REMOVE_BY_ID, new Object[]{id});
                            } catch (NumberFormatException e) {
                                System.out.println("Ошибка: ID должен быть числом");
                                continue;
                            }
                            break;

                        case "UPDATE":
                            if (parts.length < 2) {
                                System.out.println("Ошибка: не указан ID");
                                continue;
                            }
                            try {
                                long updateId = Long.parseLong(parts[1]);
                                System.out.println("Введите новые данные для элемента с ID " + updateId + ":");
                                HumanBeing updatedHuman = inputReader.readHumanBeing();
                                request = new Request(CommandType.UPDATE, new Object[]{updateId, updatedHuman});
                            } catch (NumberFormatException e) {
                                System.out.println("Ошибка: ID должен быть числом");
                                continue;
                            }
                            break;

                        case "FILTER_BY_MOOD":
                            if (parts.length < 2) {
                                System.out.println("Ошибка: не указано настроение. Доступные: SORROW, LONGING, GLOOM, APATHY");
                                continue;
                            }
                            try {
                                Mood mood = Mood.valueOf(parts[1].toUpperCase());
                                request = new Request(CommandType.FILTER_BY_MOOD, new Object[]{mood});
                            } catch (IllegalArgumentException e) {
                                System.out.println("Ошибка: неверное настроение. Доступные: SORROW, LONGING, GLOOM, APATHY");
                                continue;
                            }
                            break;

                        case "FILTER_STARTS_WITH_SOUNDTRACK_NAME":
                            if (parts.length < 2) {
                                System.out.println("Ошибка: не указана подстрока");
                                continue;
                            }
                            String prefix = parts[1];
                            request = new Request(CommandType.FILTER_STARTS_WITH_SOUNDTRACK_NAME, new Object[]{prefix});
                            break;

                        case "EXECUTE_SCRIPT":
                            if (parts.length < 2) {
                                System.out.println("Ошибка: не указан файл. Пример: execute_script script.txt");
                                continue;
                            }
                            executeScript(parts[1], client);
                            continue;

                        case "EXIT":
                            request = new Request(CommandType.EXIT, null);
                            client.sendRequest(request);
                            Response exitResponse = client.receiveResponse();
                            System.out.println(exitResponse.getMessage());
                            client.disconnect();
                            System.out.println("Клиент завершил работу");
                            return;

                        default:
                            try {
                                CommandType commandType = CommandType.valueOf(command);
                                request = new Request(commandType, null);
                            } catch (IllegalArgumentException e) {
                                System.out.println("Неизвестная команда. Введите help");
                                continue;
                            }
                    }

                    if (request != null) {
                        client.sendRequest(request);
                        Response response = client.receiveResponse();
                        if (response.isSuccess()) {
                            System.out.println(response.getMessage());
                        } else {
                            System.err.println("Ошибка: " + response.getMessage());
                        }
                    }

                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка в аргументах: " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Ошибка связи с сервером: " + e.getMessage());
                    System.err.println("Попробуйте перезапустить клиент");
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Ошибка получения данных: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу: " + e.getMessage());
            System.err.println("Убедитесь, что сервер запущен на " + host + ":" + port);
        } finally {
            client.disconnect();
        }
    }

    /**
     * Выполняет команды из файла-скрипта.
     *
     * <p>Метод читает файл построчно, игнорирует пустые строки и комментарии,
     * начинающиеся с '#', и последовательно выполняет каждую команду.
     * Поддерживается рекурсивное выполнение скриптов с ограничением глубины.</p>
     *
     * @param filename путь к файлу скрипта
     * @param client экземпляр клиента для отправки запросов на сервер
     */
    private static void executeScript(String filename, SimpleClient client) {
        if (scriptDepth >= MAX_SCRIPT_DEPTH) {
            System.out.println("Ошибка: превышена максимальная глубина вложенности скриптов (" + MAX_SCRIPT_DEPTH + ")");
            return;
        }

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Ошибка: файл '" + filename + "' не найден");
            return;
        }

        if (!file.canRead()) {
            System.out.println("Ошибка: нет прав на чтение файла '" + filename + "'");
            return;
        }

        scriptDepth++;
        System.out.println("Выполнение скрипта: " + filename + " (глубина: " + scriptDepth + ")");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                System.out.println("\n[Скрипт:" + filename + " строка " + lineNumber + "] > " + line);

                String[] parts = line.split("\\s+", 2);
                String command = parts[0].toUpperCase();

                try {
                    processScriptCommand(command, parts.length > 1 ? parts[1] : null, client);
                } catch (Exception e) {
                    System.err.println("Ошибка в строке " + lineNumber + ": " + e.getMessage());
                    System.err.println("Выполнение скрипта прервано");
                    break;
                }
            }

            System.out.println("\nСкрипт '" + filename + "' успешно выполнен");

        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
        } finally {
            scriptDepth--;
        }
    }

    /**
     * Обрабатывает отдельную команду из скрипта.
     *
     * <p>Метод парсит аргументы команды, создает соответствующий объект Request
     * и отправляет его на сервер. Поддерживаются команды ADD, ADD_IF_MIN,
     * ADD_IF_MAX, UPDATE, REMOVE_BY_ID, FILTER_BY_MOOD,
     * FILTER_STARTS_WITH_SOUNDTRACK_NAME, EXECUTE_SCRIPT, HELP, INFO, SHOW,
     * MIN_BY_ID, CLEAR, REMOVE_FIRST, EXIT.</p>
     *
     * @param command название команды в верхнем регистре
     * @param argument строка с аргументами команды (может быть null)
     * @param client экземпляр клиента для отправки запросов
     * @throws IOException если произошла ошибка ввода-вывода при отправке запроса
     * @throws ClassNotFoundException если не удалось десериализовать ответ
     * @throws IllegalArgumentException если аргументы команды некорректны
     */
    private static void processScriptCommand(String command, String argument, SimpleClient client)
            throws IOException, ClassNotFoundException {

        Request request = null;

        switch (command) {
            case "ADD":
            case "ADD_IF_MIN":
            case "ADD_IF_MAX":
                if (argument == null || argument.trim().isEmpty()) {
                    throw new IllegalArgumentException("Недостаточно аргументов для " + command);
                }
                String[] args = argument.trim().split("\\s+");
                if (args.length < 10) {
                    throw new IllegalArgumentException("Для " + command + " нужно 10 аргументов. Получено: " + args.length);
                }

                String name = args[0];
                Double x = Double.parseDouble(args[1]);
                Float y = Float.parseFloat(args[2]);
                Boolean realHero = Boolean.parseBoolean(args[3]);
                Boolean hasToothpick = Boolean.parseBoolean(args[4]);
                float impactSpeed = Float.parseFloat(args[5]);
                String soundtrackName = args[6];
                WeaponType weaponType = WeaponType.valueOf(args[7].toUpperCase());
                Mood mood = args[8].equalsIgnoreCase("null") ? null : Mood.valueOf(args[8].toUpperCase());
                Boolean carCool = Boolean.parseBoolean(args[9]);

                Coordinates coordinates = new Coordinates(x, y);
                Car car = new Car(carCool);
                HumanBeing newHuman = new HumanBeing(name, coordinates, realHero, hasToothpick,
                        impactSpeed, soundtrackName, weaponType, mood, car);

                CommandType cmdType = CommandType.valueOf(command);
                request = new Request(cmdType, new Object[]{newHuman});
                break;

            case "UPDATE":
                if (argument == null || argument.trim().isEmpty()) {
                    throw new IllegalArgumentException("Недостаточно аргументов для UPDATE");
                }
                args = argument.trim().split("\\s+");
                if (args.length < 11) {
                    throw new IllegalArgumentException("Для UPDATE нужно 11 аргументов");
                }

                long id = Long.parseLong(args[0]);
                name = args[1];
                x = Double.parseDouble(args[2]);
                y = Float.parseFloat(args[3]);
                realHero = Boolean.parseBoolean(args[4]);
                hasToothpick = Boolean.parseBoolean(args[5]);
                impactSpeed = Float.parseFloat(args[6]);
                soundtrackName = args[7];
                weaponType = WeaponType.valueOf(args[8].toUpperCase());
                mood = args[9].equalsIgnoreCase("null") ? null : Mood.valueOf(args[9].toUpperCase());
                carCool = Boolean.parseBoolean(args[10]);

                coordinates = new Coordinates(x, y);
                car = new Car(carCool);
                HumanBeing updatedHuman = new HumanBeing(name, coordinates, realHero, hasToothpick,
                        impactSpeed, soundtrackName, weaponType, mood, car);

                request = new Request(CommandType.UPDATE, new Object[]{id, updatedHuman});
                break;

            case "REMOVE_BY_ID":
                if (argument == null) {
                    throw new IllegalArgumentException("Не указан ID");
                }
                long removeId = Long.parseLong(argument.trim());
                request = new Request(CommandType.REMOVE_BY_ID, new Object[]{removeId});
                break;

            case "FILTER_BY_MOOD":
                Mood filterMood = Mood.valueOf(argument.trim().toUpperCase());
                request = new Request(CommandType.FILTER_BY_MOOD, new Object[]{filterMood});
                break;

            case "FILTER_STARTS_WITH_SOUNDTRACK_NAME":
                request = new Request(CommandType.FILTER_STARTS_WITH_SOUNDTRACK_NAME, new Object[]{argument.trim()});
                break;

            case "EXECUTE_SCRIPT":
                executeScript(argument.trim(), client);
                return;

            case "HELP":
            case "INFO":
            case "SHOW":
            case "MIN_BY_ID":
            case "CLEAR":
            case "REMOVE_FIRST":
            case "EXIT":
                request = new Request(CommandType.valueOf(command), null);
                break;

            default:
                throw new IllegalArgumentException("Неизвестная команда: " + command);
        }

        if (request != null) {
            client.sendRequest(request);
            Response response = client.receiveResponse();
            if (response.isSuccess()) {
                System.out.println(response.getMessage());
                if (command.equals("SHOW") && response.getData() != null && !response.getData().isEmpty()) {
                    System.out.println("Элементы коллекции:");
                    for (Object obj : response.getData()) {
                        System.out.println(obj);
                    }
                }
            } else {
                System.err.println("Ошибка: " + response.getMessage());
            }
        }
    }
}