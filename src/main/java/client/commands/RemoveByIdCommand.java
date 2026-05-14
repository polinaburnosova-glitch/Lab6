package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

public class RemoveByIdCommand implements Command {

    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        if (argument == null) {
            System.out.println("Ошибка: не указан ID. Пример: remove_by_id 5");
            return;
        }

        try {
            long id = Long.parseLong(argument);
            Request request = new Request(CommandType.REMOVE_BY_ID, new Object[]{id}, client.getCurrentUser());
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