package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

public class RemoveFirstCommand implements Command {

    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.REMOVE_FIRST, null, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}
