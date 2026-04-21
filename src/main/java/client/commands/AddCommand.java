package client.commands;

import client.SimpleClient;
import client.console.ConsoleInputReader;
import common.model.HumanBeing;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

public class AddCommand implements Command {

    private final ConsoleInputReader inputReader;

    public AddCommand(ConsoleInputReader inputReader) {
        this.inputReader = inputReader;
    }

    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        System.out.println("Введите данные для нового элемента:");
        HumanBeing newHuman = inputReader.readHumanBeing();

        Request request = new Request(CommandType.ADD, new Object[]{newHuman});
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}