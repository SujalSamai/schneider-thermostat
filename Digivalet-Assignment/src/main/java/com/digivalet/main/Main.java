package com.digivalet.main;

import com.digivalet.client.Client;
import com.digivalet.server.Server;

public class Main {
    public static void main(String[] args) {
        Thread serverThread = new Thread(new Server());
        serverThread.start();

        Thread clientThread = new Thread(new Client());
        clientThread.start();
    }
}
