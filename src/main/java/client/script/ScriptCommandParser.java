package client.script;

import common.model.*;
import common.network.CommandType;
import common.network.Request;
import common.EnvLoader;

public class ScriptCommandParser {

    private static final String ARGUMENT_SEPARATOR = EnvLoader.get("SCRIPT_ARGUMENT_SEPARATOR", "\\s+");
    private static final int ADD_ARGS_COUNT = EnvLoader.getInt("SCRIPT_ADD_ARGS_COUNT", 10);
    private static final int UPDATE_ARGS_COUNT = EnvLoader.getInt("SCRIPT_UPDATE_ARGS_COUNT", 11);

    public static Request parse(String command, String argument) {

        switch (command) {
            case "ADD":
            case "ADD_IF_MIN":
            case "ADD_IF_MAX":
                return parseAddCommand(command, argument);

            case "UPDATE":
                return parseUpdateCommand(argument);

            case "REMOVE_BY_ID":
                return parseRemoveByIdCommand(argument);

            case "FILTER_BY_MOOD":
                return parseFilterByMoodCommand(argument);

            case "FILTER_STARTS_WITH_SOUNDTRACK_NAME":
                return parseFilterBySoundtrackCommand(argument);

            case "HELP":
            case "INFO":
            case "SHOW":
            case "MIN_BY_ID":
            case "CLEAR":
            case "REMOVE_FIRST":
            case "EXIT":
                return new Request(CommandType.valueOf(command), null, null);

            default:
                throw new IllegalArgumentException("Неизвестная команда: " + command);
        }
    }

    private static Request parseAddCommand(String command, String argument) {
        if (argument == null || argument.trim().isEmpty()) {
            throw new IllegalArgumentException("Недостаточно аргументов для " + command);
        }

        String[] args = argument.trim().split(ARGUMENT_SEPARATOR);
        if (args.length < ADD_ARGS_COUNT) {
            throw new IllegalArgumentException("Для " + command + " нужно " + ADD_ARGS_COUNT +
                    " аргументов. Получено: " + args.length);
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
        HumanBeing human = new HumanBeing(name, coordinates, realHero, hasToothpick,
                impactSpeed, soundtrackName, weaponType, mood, car, null);

        CommandType cmdType = CommandType.valueOf(command);
        return new Request(cmdType, new Object[]{human}, null);
    }

    private static Request parseUpdateCommand(String argument) {
        if (argument == null || argument.trim().isEmpty()) {
            throw new IllegalArgumentException("Недостаточно аргументов для UPDATE");
        }

        String[] args = argument.trim().split(ARGUMENT_SEPARATOR);
        if (args.length < UPDATE_ARGS_COUNT) {
            throw new IllegalArgumentException("Для UPDATE нужно " + UPDATE_ARGS_COUNT +
                    " аргументов. Получено: " + args.length);
        }

        long id = Long.parseLong(args[0]);
        String name = args[1];
        Double x = Double.parseDouble(args[2]);
        Float y = Float.parseFloat(args[3]);
        Boolean realHero = Boolean.parseBoolean(args[4]);
        Boolean hasToothpick = Boolean.parseBoolean(args[5]);
        float impactSpeed = Float.parseFloat(args[6]);
        String soundtrackName = args[7];
        WeaponType weaponType = WeaponType.valueOf(args[8].toUpperCase());
        Mood mood = args[9].equalsIgnoreCase("null") ? null : Mood.valueOf(args[9].toUpperCase());
        Boolean carCool = Boolean.parseBoolean(args[10]);

        Coordinates coordinates = new Coordinates(x, y);
        Car car = new Car(carCool);
        HumanBeing human = new HumanBeing(name, coordinates, realHero, hasToothpick,
                impactSpeed, soundtrackName, weaponType, mood, car, null);

        return new Request(CommandType.UPDATE, new Object[]{id, human}, null);
    }

    private static Request parseRemoveByIdCommand(String argument) {
        if (argument == null) {
            throw new IllegalArgumentException("Не указан ID");
        }
        long id = Long.parseLong(argument.trim());
        return new Request(CommandType.REMOVE_BY_ID, new Object[]{id}, null);
    }

    private static Request parseFilterByMoodCommand(String argument) {
        if (argument == null) {
            throw new IllegalArgumentException("Не указано настроение");
        }
        Mood mood = Mood.valueOf(argument.trim().toUpperCase());
        return new Request(CommandType.FILTER_BY_MOOD, new Object[]{mood}, null);
    }

    private static Request parseFilterBySoundtrackCommand(String argument) {
        if (argument == null) {
            throw new IllegalArgumentException("Не указана подстрока");
        }
        return new Request(CommandType.FILTER_STARTS_WITH_SOUNDTRACK_NAME, new Object[]{argument.trim()}, null);
    }
}