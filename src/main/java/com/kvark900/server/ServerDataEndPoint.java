package com.kvark900.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

/**
 * Server's endpoint for data connection and data transfers.
 * Running on separate thread.
 */
public class ServerDataEndPoint implements Runnable {
    private final String filename;
    private Socket clientSocket;
    private int port;
    private static final Logger LOGGER = Logger.getLogger("SERVER");
    private ServerSocket serverSocket;

    public ServerDataEndPoint(int port, String filename) {
        this.port = port;
        this.filename = filename;
    }

    /**
     * Establishing connection with client's data endpoint.
     * Receiving client's bytes.
     */
    @Override
    public void run() {
        try {
            LOGGER.info("SERVER: Starting server...");
            serverSocket = new ServerSocket(port);

            LOGGER.info("SERVER: Waiting clients for data connection...");
            clientSocket = serverSocket.accept();
            LOGGER.info(String.format("SERVER: Accepted client %s", clientSocket.getInetAddress().toString()));

            InputStream initialStream = clientSocket.getInputStream();
            File file = new File(filename);
            Files.copy(initialStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
