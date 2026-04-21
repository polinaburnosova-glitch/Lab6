package client.commands;

import client.SimpleClient;
import client.console.ConsoleInputReader;
import common.model.HumanBeing;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

public class UpdateCommand implements Command {

    private final ConsoleInputReader inputReader;

    public UpdateCommand(ConsoleInputReader inputReader) {
        this.inputReader = inputReader;
    }

    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        if (argument == null) {
            System.out.println("Ошибка: не указан ID. Пример: update 5");
            return;
        }

        try {
            long id = Long.parseLong(argument);
            System.out.println("Введите новые данные для элемента с ID " + id + ":");
            HumanBeing updatedHuman = inputReader.readHumanBeing();

            Request request = new Request(CommandType.UPDATE, new Object[]{id, updatedHuman});
            client.sendRequest(request);

            Response response = client.receiveResponse();

            if (response.isSuccess()) {
                System.out.println(response.getMessage());
            } else {
                System.err.println("Ошибка: " + response.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        }
    }
}