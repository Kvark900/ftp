package com.kvark900.server;

import com.kvark900.client.FTPCmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * FTP server class responsible for starting main server,
 * starting data channels on separate threads and serving clients.
 */
public class FTPServer {
    private ServerSocket serverSocket;
    private Socket clientCmdSocket;
    private PrintWriter writer;
    private int dataPort;
    private BufferedReader reader;
    private static final Logger LOGGER = Logger.getLogger("SERVER");

    /**
     * Starting the server and listening for client's commands
     *
     * @param port integer representing server's port number
     */
    public void start(int port) throws IOException {
        LOGGER.info("SERVER: Starting server...");
        serverSocket = new ServerSocket(port);

        LOGGER.info("SERVER: Waiting for clients...");
        clientCmdSocket = serverSocket.accept();
        LOGGER.info(String.format("SERVER: Accepted client %s", clientCmdSocket.getInetAddress().toString()));

        writer = new PrintWriter(clientCmdSocket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(clientCmdSocket.getInputStream()));
        String cmd;
        while ((cmd = reader.readLine()) != null) {
            LOGGER.info("SERVER: Command  Received: " + cmd);
            processCmd(cmd);
        }
    }

    /**
     * Analyzing command and sending appropriate response
     *
     * @param cmd string representing client's command
     */
    private void processCmd(String cmd) {
        if (cmd.contains(FTPCmd.USER.toString())) {
            sendLine(createResponse(FTPReturnCode.NEED_PASSWORD, ""));
        } else if (cmd.contains(FTPCmd.PASS.toString())) {
            sendLine(createResponse(FTPReturnCode.LOGGED_IN, ""));
        } else if (cmd.contains(FTPCmd.PASV.toString())) {
            String port = getRandomPort();
            String fileName = cmd.split(" ")[1];
            extractPortNumber(port);
            sendLine(createResponse(FTPReturnCode.ENTERING_PASSIVE_MODE, String.format("(127,0,0,1,%s)", port)));
            openPassiveConnection(fileName);
        } else sendLine(createResponse(FTPReturnCode.UNKNOWN_ERROR, ""));
    }

    /**
     * Opening passive data connection on the new thread
     *
     * @param filename of file received with STOR command
     */
    private void openPassiveConnection(String filename) {
        LOGGER.info(String.format("SERVER: %s. Starting server on port %s",
                    FTPReturnCode.ENTERING_PASSIVE_MODE.toString(), dataPort));
        Runnable connection = new ServerDataEndPoint(dataPort, filename);
        new Thread(connection).start();
    }

    /**
     * Generating random port in response to PASV client's command
     *
     * @return string representing server's data port
     */
    private String getRandomPort() {
        int a = ThreadLocalRandom.current().nextInt(20, 200 + 1);
        int b = ThreadLocalRandom.current().nextInt(20, 200 + 1);
        return "" + a + "," + b;
    }

    /**
     * Extracting port number for passive data connection
     *
     * @param port string representing port number
     */
    private void extractPortNumber(String port){
        StringTokenizer tokenizer = new StringTokenizer(port, ",");
        dataPort = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
    }

    /**
     * Creating server's response message
     *
     * @param code    server response code
     * @param message additional response arguments
     * @return response message
     * @see FTPReturnCode
     */
    private String createResponse(FTPReturnCode code, String message) {
        return code != null ?
                code.getCode() + " " + code.toString() + (message.isEmpty() ? "" : " " + message) :
                FTPReturnCode.UNKNOWN_ERROR.toString();
    }

    /**
     * Writing message to the client
     *
     * @param message
     */
    private void sendLine(String message) {
        if (clientCmdSocket == null) LOGGER.warning("Please connect!");
        writer.write(message + "\r\n");
        writer.flush();
    }

    /**
     * Closing sockets and streams
     */
    public void stop() throws IOException {
        reader.close();
        writer.close();
        clientCmdSocket.close();
        serverSocket.close();
    }



}
