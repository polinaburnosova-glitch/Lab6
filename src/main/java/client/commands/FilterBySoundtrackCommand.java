package client.commands;

import client.SimpleClient;
import common.network.CommandType;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

public class FilterBySoundtrackCommand implements Command {

    @Override
    public void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException {
        if (argument == null) {
            System.out.println("Ошибка: не указана подстрока");
            return;
        }

        Request request = new Request(CommandType.FILTER_STARTS_WITH_SOUNDTRACK_NAME, new Object[]{argument}, client.getCurrentUser());
        client.sendRequest(request);

        Response response = client.receiveResponse();

        if (response.isSuccess()) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}
