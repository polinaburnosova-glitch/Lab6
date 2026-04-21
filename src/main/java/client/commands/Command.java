package client.commands;

import client.SimpleClient;
import common.network.Request;
import common.network.Response;
import java.io.IOException;

public interface Command {

    void execute(SimpleClient client, String argument) throws IOException, ClassNotFoundException;
}