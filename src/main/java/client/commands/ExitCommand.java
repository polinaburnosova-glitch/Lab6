package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

public class ExitCommand implements Command {

    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        Request request = new Request(CommandType.EXIT, null, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();
        System.out.println(response.getMessage());

        client.disconnect();
        System.out.println("Клиент завершил работу");
    }
}
